package cl.nightcore.itemrarity.abstracted;

import cl.nightcore.itemrarity.rollquality.StatValueGenerator;
import cl.nightcore.itemrarity.statprovider.ModifierProvider;
import cl.nightcore.itemrarity.util.ItemUtil;
import cl.nightcore.itemrarity.util.RarityCalculator;
import dev.aurelium.auraskills.api.stat.Stat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static cl.nightcore.itemrarity.ItemRarity.AURA_LOCALE;

/**
 * Pre-cached stat roll system that maintains 1000 pre-generated stat combinations
 * for each StatProvider type (4 and 5 stats) with asynchronous regeneration.
 */
public class StatRollCache {
    
    private static final int CACHE_SIZE = 1000;
    private static final int REFILL_THRESHOLD = 300; // Regenerate when cache drops below this
    private static final int REFILL_BATCH_SIZE = 10; // How many to generate in each batch
    
    // Cache structure: ProviderType -> StatCount -> Queue of PrerolledStats
    private final Map<Class<? extends ModifierProvider>, Map<Integer, ConcurrentLinkedQueue<PrerolledStats>>> cache;
    
    // Thread pool for async generation
    private final ExecutorService executorService;
    private final Plugin plugin;
    
    // Atomic counters for monitoring
    private final Map<String, AtomicInteger> cacheHits = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> cacheRefills = new ConcurrentHashMap<>();
    
    public StatRollCache(Plugin plugin) {
        this.plugin = plugin;
        this.cache = new ConcurrentHashMap<>();
        this.executorService = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "StatRollCache-Generator");
            t.setDaemon(true);
            return t;
        });
        
        initializeCache();
    }
    
    /**
     * Gets a pre-rolled stat combination and triggers refill if needed
     */
    public PrerolledStats getPrerolledStats(ModifierProvider provider, int statCount) {
        Class<? extends ModifierProvider> providerClass = provider.getClass();
        String cacheKey = providerClass.getSimpleName() + "-" + statCount;
        
        Map<Integer, ConcurrentLinkedQueue<PrerolledStats>> providerCache = 
            cache.get(providerClass);
        
        if (providerCache == null) {
            // Fallback: generate on demand if provider not cached
            return generateStatsSync(provider, statCount);
        }
        
        ConcurrentLinkedQueue<PrerolledStats> queue = providerCache.get(statCount);
        if (queue == null || queue.isEmpty()) {
            // Fallback: generate on demand if queue empty
            return generateStatsSync(provider, statCount);
        }
        
        PrerolledStats result = queue.poll();
        cacheHits.computeIfAbsent(cacheKey, k -> new AtomicInteger(0)).incrementAndGet();
        
        // Trigger async refill if below threshold
        if (queue.size() < REFILL_THRESHOLD) {
            scheduleRefill(provider, statCount);
        }
        
        return result;
    }
    
    /**
     * Initialize cache for all known provider types
     */
    private void initializeCache() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // You'll need to register your providers here
            registerProvider(cl.nightcore.itemrarity.statprovider.WeaponModifierProvider.class, 4, 5);
            registerProvider(cl.nightcore.itemrarity.statprovider.HelmetModifierProvider.class, 4, 5);
            registerProvider(cl.nightcore.itemrarity.statprovider.ChestplateModifierProvider.class, 4, 5);
            registerProvider(cl.nightcore.itemrarity.statprovider.LeggingsModifierProvider.class, 4, 5);
            registerProvider(cl.nightcore.itemrarity.statprovider.BootsModifierProvider.class, 4, 5);
        });
    }
    
    /**
     * Register a provider type and fill its initial cache
     */
    private void registerProvider(Class<? extends ModifierProvider> providerClass, int... statCounts) {
        try {
            ModifierProvider provider = providerClass.getDeclaredConstructor().newInstance();
            Map<Integer, ConcurrentLinkedQueue<PrerolledStats>> providerCache = new ConcurrentHashMap<>();
            
            for (int statCount : statCounts) {
                ConcurrentLinkedQueue<PrerolledStats> queue = new ConcurrentLinkedQueue<>();
                
                // Fill initial cache
                for (int i = 0; i < CACHE_SIZE; i++) {
                    queue.offer(generateStatsSync(provider, statCount));
                }
                
                providerCache.put(statCount, queue);
                plugin.getLogger().info("Initialized cache for " + providerClass.getSimpleName() + 
                                      " with " + statCount + " stats: " + queue.size() + " entries");
            }
            
            cache.put(providerClass, providerCache);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to initialize cache for " + providerClass.getSimpleName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Schedule async refill of cache
     */
    private void scheduleRefill(ModifierProvider provider, int statCount) {
        Class<? extends ModifierProvider> providerClass = provider.getClass();
        String cacheKey = providerClass.getSimpleName() + "-" + statCount;
        
        executorService.submit(() -> {
            try {
                ConcurrentLinkedQueue<PrerolledStats> queue = 
                    cache.get(providerClass).get(statCount);
                
                int currentSize = queue.size();
                int toGenerate = Math.min(REFILL_BATCH_SIZE, CACHE_SIZE - currentSize);
                
                for (int i = 0; i < toGenerate; i++) {
                    queue.offer(generateStatsSync(provider, statCount));
                }
                
                cacheRefills.computeIfAbsent(cacheKey, k -> new AtomicInteger(0))
                           .incrementAndGet();
                
                plugin.getLogger().fine("Refilled cache for " + cacheKey + ": +" + toGenerate + 
                                      " (total: " + queue.size() + ")");
                
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to refill cache for " + cacheKey + ": " + e.getMessage());
            }
        });
    }
    
    /**
     * Synchronously generate a complete stat roll
     */
    private PrerolledStats generateStatsSync(ModifierProvider provider, int statCount) {
        List<Stat> availableStats = provider.getAvailableStats();
        List<ModifierPojoExtend> statsPojos = new ArrayList<>();
        List<Component> statLines = new ArrayList<>();
        
        double sum = 0.0;
        
        // Generate Gaussian stats first
        for (Stat stat : provider.getGaussStats()) {
            if (statsPojos.size() >= statCount) break;
            
            int value = StatValueGenerator.generateValueForStat(true);
            sum += value;
            
            statsPojos.add(createStatModifierPojo(stat, value, true));
            statLines.add(createStatLoreLine(stat, value));
        }
        
        // Fill remaining slots with random stats
        while (statsPojos.size() < statCount) {
            Stat stat;
            do {
                stat = availableStats.get(ThreadLocalRandom.current().nextInt(availableStats.size()));
            } while (containsStat(statsPojos, stat));
            
            int value = StatValueGenerator.generateValueForStat(false);
            sum += value;
            
            statsPojos.add(createStatModifierPojo(stat, value, false));
            statLines.add(createStatLoreLine(stat, value));
        }
        
        // Calculate rarity
        double average = sum / statCount;
        if (statCount == 4) {
            average *= 0.85; // Early game penalty
        }
        
        Component rarity = RarityCalculator.calculateRarity(average);
        
        // Build pre-calculated stat lines
        List<Component> preCalculatedLines = new ArrayList<>();
        preCalculatedLines.add(Component.empty());
        preCalculatedLines.add(Component.text("Bonos 1-5: ", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .append(rarity));
        preCalculatedLines.addAll(statLines);
        
        return new PrerolledStats(
            statsPojos.toArray(new ModifierPojoExtend[0]),
            preCalculatedLines,
            rarity,
            average
        );
    }
    
    /**
     * Helper method to create stat modifier pojo
     */
    private ModifierPojoExtend createStatModifierPojo(Stat stat, double value, boolean gaussian) {
        // You'll need to adapt this based on your actual ModifierPojoExtend constructor
        // This is a placeholder based on the pattern I see in your code
        return new ModifierPojoExtend(
            null, // modifierType - you'll need to pass this or determine it
            stat, 
            value, 
            dev.aurelium.auraskills.api.util.AuraSkillsModifier.Operation.ADD,
            "native", 
            false, 
            gaussian
        );
    }
    
    /**
     * Helper method to create stat lore lines
     */
    private Component createStatLoreLine(Stat stat, int value) {
        return Component.text(" +" + value, ItemUtil.getColorOfStat(stat))
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(" " + stat.getDisplayName(AURA_LOCALE), TextColor.color(0x636363)))
                .decoration(TextDecoration.ITALIC, false);
    }
    
    /**
     * Helper to check if stat already exists
     */
    private boolean containsStat(List<ModifierPojoExtend> pojos, Stat stat) {
        return pojos.stream().anyMatch(pojo -> pojo.stat.equals(stat));
    }

    /**
         * Pre-rolled stats container
         */
        public record PrerolledStats(ModifierPojoExtend[] statsPojos, List<Component> preCalculatedStatLines, Component rarity, double average) {
    }
}