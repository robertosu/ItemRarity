package cl.nightcore.itemrarity.abstracted;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.config.CombinedStats;
import cl.nightcore.itemrarity.config.CombinedTraits;
import cl.nightcore.itemrarity.config.ItemConfig;
import cl.nightcore.itemrarity.item.roller.BlessingObject;
import cl.nightcore.itemrarity.item.roller.RedemptionObject;
import cl.nightcore.itemrarity.rollquality.StatValueGenerator;
import cl.nightcore.itemrarity.statprovider.ModifierProvider;
import cl.nightcore.itemrarity.util.ItemType;
import cl.nightcore.itemrarity.util.ItemUtil;
import cl.nightcore.itemrarity.util.RarityCalculator;
import com.nexomc.nexo.api.NexoItems;
import dev.aurelium.auraskills.api.AuraSkillsBukkit;
import dev.aurelium.auraskills.api.item.ItemManager;
import dev.aurelium.auraskills.api.item.ModifierType;
import dev.aurelium.auraskills.api.skill.Multiplier;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.StatModifier;
import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.trait.TraitModifier;
import dev.aurelium.auraskills.api.util.AuraSkillsModifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import su.nightexpress.excellentenchants.enchantment.EnchantRegistry;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cl.nightcore.itemrarity.ItemRarity.AURA_LOCALE;

public abstract class IdentifiedItem extends ItemStack {

    private static final ItemManager ITEM_MANAGER = AuraSkillsBukkit.get().getItemManager();
    protected static final String NATIVE_STATMODIFIER = "native";
    protected static final String MONOLITIC_TRAITMODIFIER = "monolitic";
    protected static final String GEM_STATMODIFIER = "gema";
    protected static final int MAX_EXTRA_BONUSES = 2;
    protected static final int MAX_LEVEL = 9;

    private static StatRollCache statRollCache;

    protected static final boolean randomOperations = false;

    private static final Component GEMS_HEADER =
            Component.text("Gemas:").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false);
    private static final Component EMPTY_SOCKET = Component.text(" ⛶ ")
            .color(NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text("Vacío").color(TextColor.color(0x636363)).decoration(TextDecoration.ITALIC, false));

    protected final ModifierType modifierType;
    protected final ItemType itemType;
    protected final ModifierProvider statProvider;

    private final List<ModifierPojoExtend> statsPojos;

    // Nueva estructura para pre-calcular lore de stats
    private final List<Component> preCalculatedStatLines;
    // private Component preCalculatedRarity;

    protected Component rarity;
    List<Component> newLore;

    public static void initializeStatCache(Plugin plugin) {
        statRollCache = new StatRollCache(plugin);
    }

    public IdentifiedItem(ItemStack item) {
        super(item);
        this.itemType = ItemUtil.getItemType(item);
        this.statProvider = ItemUtil.getProvider(this.itemType);
        this.modifierType = ItemUtil.getModifierType(this.itemType);
        this.preCalculatedStatLines = new ArrayList<>();
        this.statsPojos = new ArrayList<>();
    }

    public static double calculateTotalDamage(ItemStack item, double baseDamage) {
        double sharpnessDamage = calculateSharpnessDamage(item);
        return baseDamage + sharpnessDamage;
    }

    private static double calculateSharpnessDamage(ItemStack item) {
        int sharpnessLevel = item.getEnchantmentLevel(Enchantment.SHARPNESS);
        return sharpnessLevel > 0 ? (0.5 * sharpnessLevel + 0.5) : 0;
    }

    public static double calculateAttackSpeed(ItemStack item) {
        double baseSpeed = 0;

        Collection<AttributeModifier> baseModifiers =
                item.getType().getDefaultAttributeModifiers().get(Attribute.ATTACK_SPEED);

        if (NexoItems.idFromItem(item) == null) {
            for (AttributeModifier modifier : baseModifiers) {
                baseSpeed = modifier.getAmount();
            }
            return baseSpeed + 4;
        }

        ItemMeta meta = item.getItemMeta();
        if (NexoItems.idFromItem(item) != null) {
            if (meta.getAttributeModifiers(Attribute.ATTACK_SPEED) != null) {
                for (AttributeModifier modifier :
                        Objects.requireNonNull(meta.getAttributeModifiers(Attribute.ATTACK_SPEED))) {
                    baseSpeed = modifier.getAmount() + 4;
                    return baseSpeed;
                }
            } else if (meta.getAttributeModifiers(Attribute.ATTACK_SPEED) == null) {
                for (AttributeModifier modifier : baseModifiers) {
                    baseSpeed = modifier.getAmount();
                }
                return baseSpeed + 4;
            }
        }
        return baseSpeed;
    }

    public Component getRarityComponent() {
        return rarity;
    }

    public void identify() {
        this.setIdentifiedAndLevelNBT(1);
        this.setMaxBonuses(4);
        this.generateStats();
        this.applyStatsToItem();
        this.setMonoliticStats(1);
        this.setNewLore();
    }

    protected List<Component> generateStatsCached() {
        int statsCount = getMaxBonuses();

        // Get pre-rolled stats from cache
        StatRollCache.PrerolledStats prerolled = statRollCache.getPrerolledStats(statProvider, statsCount);

        if (prerolled != null) {
            // Use cached data
            this.statsPojos.clear();
            Collections.addAll(this.statsPojos, prerolled.statsPojos());

            // Update modifier type for each pojo (since cache can't know the specific modifier type)
            for (ModifierPojoExtend pojo : this.statsPojos) {
                pojo.type = this.modifierType; // Assuming this field is accessible
            }
            this.rarity = prerolled.rarity();
        }
        return prerolled.preCalculatedStatLines();
    }

    public void rerollStatsEnhancedCached() {
        // Single operation to remove all modifiers matching ID
        this.setItemMeta(ITEM_MANAGER
                .removeStatModifiersByIdentifier(this, modifierType, NATIVE_STATMODIFIER)
                .getItemMeta());

        // Generate stats using cache
        // generateStatsCached();

        // Apply stats and update lore efficiently
        updateLoreDirectCached(generateStatsCached(), applyStatsToItemGetMeta());
    }

    private void updateLoreDirectCached(List<Component> lines, ItemMeta meta) {
        List<Component> currentLore = meta.lore();

        if (currentLore == null || currentLore.isEmpty()) {
            setNewLore();
            return;
        }

        // Find second empty component efficiently
        int emptyCount = 0;
        int secondEmptyIndex = -1;

        for (int i = 0; i < currentLore.size() && emptyCount < 2; i++) {
            if (currentLore.get(i).equals(Component.empty())) {
                emptyCount++;
                if (emptyCount == 2) {
                    secondEmptyIndex = i;
                    break;
                }
            }
        }

        if (secondEmptyIndex == -1) {
            setNewLore();
            return;
        }

        // Build new lore using cached stat lines
        int remainingSize = currentLore.size() - secondEmptyIndex;
        List<Component> updatedLore = new ArrayList<>(lines.size() + remainingSize);

        // Add cached stat lines
        updatedLore.addAll(lines);

        // Add remaining lore efficiently using subList
        updatedLore.addAll(currentLore.subList(secondEmptyIndex, currentLore.size()));

        // Single lore update
        meta.lore(updatedLore);

        // Update name only if rarity changed
        handleCustomName(meta, false);
    }

    private ModifierPojoExtend getStatModifierPojo(Stat stat, double value, boolean gaussian) {
        return new ModifierPojoExtend(
                modifierType, stat, value, AuraSkillsModifier.Operation.ADD, NATIVE_STATMODIFIER, false, gaussian);
    }

    private ItemManager.ModifierPojo getRandomStatModifierPojo(Stat stat, double value) {

        AuraSkillsModifier.Operation operation = ThreadLocalRandom.current().nextBoolean()
                ? AuraSkillsModifier.Operation.ADD
                : AuraSkillsModifier.Operation.ADD_PERCENT;

        return new ItemManager.ModifierPojo(modifierType, stat, value, operation, NATIVE_STATMODIFIER, false);
    }

    protected void generateStats() {
        int statsCount = getMaxBonuses();
        List<Stat> availableStats = statProvider.getAvailableStats();

        // Variables para el cálculo de rareza
        double sum = 0.0;
        List<Component> tempStatLines = new ArrayList<>();

        // 1. Agregar stats gaussianas
        for (Stat stat : statProvider.getGaussStats()) {
            int value = StatValueGenerator.generateValueForStat(true);

            // Agregar al POJO
            statsPojos.add(getStatModifierPojo(stat, value, true));

            // Generar línea de lore inmediatamente
            sum += value;
            Component statLine = createStatLoreLine(stat, value);
            tempStatLines.add(statLine);
        }

        // 2. Agregar stats restantes
        int remainingStats = statsCount - statsPojos.size();
        for (int i = 0; i < remainingStats; i++) {
            Stat stat;
            do {
                stat = availableStats.get(ThreadLocalRandom.current().nextInt(availableStats.size()));
            } while (containsStat(stat));

            int value = StatValueGenerator.generateValueForStat(false);

            // Agregar al POJO
            statsPojos.add(getStatModifierPojo(stat, value, false));

            // Generar línea de lore inmediatamente
            sum += value;
            Component statLine = createStatLoreLine(stat, value);
            tempStatLines.add(statLine);
        }

        // 3. Calcular rareza una sola vez
        int totalStats = statsPojos.size();
        double average = sum / totalStats;
        if (totalStats == 4) {
            average *= 0.85; // Aplicar penalización early game
        }

        this.rarity = RarityCalculator.calculateRarity(average);

        // 4. Construir el lore final
        preCalculatedStatLines.add(Component.empty());
        preCalculatedStatLines.add(Component.text("Bonos 1-5: ", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .append(this.rarity));
        preCalculatedStatLines.addAll(tempStatLines);
    }

    protected ItemMeta generateStatsOptimized() {
        int statsCount = getMaxBonuses();
        List<Stat> availableStats = statProvider.getAvailableStats();

        double sum = 0.0;

        // Pre-allocate arrays for batch operations
        ModifierPojoExtend[] pojoArray = new ModifierPojoExtend[statsCount];
        Component[] statLines = new Component[statsCount];

        int index = 0;

        // Generate Gaussian stats first
        for (Stat stat : statProvider.getGaussStats()) {
            if (index >= statsCount) break;

            int value = StatValueGenerator.generateValueForStat(true);
            sum += value;

            pojoArray[index] = getStatModifierPojo(stat, value, true);
            statLines[index] = createStatLineFast(stat, value);
            index++;
        }

        // Fill remaining slots
        while (index < statsCount) {
            Stat stat;
            do {
                stat = availableStats.get(ThreadLocalRandom.current().nextInt(availableStats.size()));
            } while (containsStatInArray(stat, pojoArray, index));

            int value = StatValueGenerator.generateValueForStat(false);
            sum += value;

            pojoArray[index] = getStatModifierPojo(stat, value, false);
            statLines[index] = createStatLineFast(stat, value);
            index++;
        }

        // Batch add to collections
        Collections.addAll(statsPojos, pojoArray);

        // Calculate rarity once
        double average = sum / statsCount;
        if (statsCount == 4) {
            average *= 0.85;
        }
        this.rarity = RarityCalculator.calculateRarity(average);

        // Build pre-calculated lines efficiently
        preCalculatedStatLines.add(Component.empty());
        preCalculatedStatLines.add(Component.text("Bonos 1-5: ", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .append(this.rarity));
        Collections.addAll(preCalculatedStatLines, statLines);

        // Apply stats in single batch operation
        return ITEM_MANAGER.addStatModifierArray(this, pojoArray).getItemMeta();
    }

    private boolean containsStatInArray(Stat stat, ModifierPojoExtend[] array, int currentIndex) {
        for (int i = 0; i < currentIndex; i++) {
            if (array[i].stat.equals(stat)) {
                return true;
            }
        }
        return false;
    }

    private Component createStatLineFast(Stat stat, int value) {
        return Component.text(" +" + value, ItemUtil.getColorOfStat(stat))
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(" " + stat.getDisplayName(AURA_LOCALE), TextColor.color(0x636363)))
                .decoration(TextDecoration.ITALIC, false);
    }

    protected void generateStatsExceptHighest(StatModifier highestModifier) {
        int statsCount = getMaxBonuses();
        List<Stat> availableStats = statProvider.getAvailableStats();
        // Limpiar colecciones previas
        statsPojos.clear();
        preCalculatedStatLines.clear();

        statsPojos.add(getStatModifierPojo(
                highestModifier.stat(), highestModifier.value(), statProvider.isThisStatGauss(highestModifier.stat())));

        // Variables para el cálculo de rareza
        double sum = 0.0;
        List<Component> tempStatLines = new ArrayList<>();

        // 1. Agregar stats gaussianas
        for (Stat stat : statProvider.getGaussStats()) {
            if (stat.name().equals(highestModifier.stat().name())) {
                continue;
            }
            int value = StatValueGenerator.generateValueForStat(statProvider.isThisStatGauss(stat));

            // Agregar al POJO
            statsPojos.add(getStatModifierPojo(stat, value, true));

            // Generar línea de lore inmediatamente
            sum += value;
            Component statLine = createStatLoreLine(stat, value);
            tempStatLines.add(statLine);
        }

        // 2. Agregar stats restantes
        int remainingStats = statsCount - statsPojos.size();
        for (int i = 0; i < remainingStats; i++) {
            Stat stat;
            do {
                stat = availableStats.get(ThreadLocalRandom.current().nextInt(availableStats.size()));
                if (stat.name().equals(highestModifier.stat().name())) {
                    continue;
                }
            } while (containsStat(stat));

            int value = StatValueGenerator.generateValueForStat(statProvider.isThisStatGauss(stat));

            // Agregar al POJO
            statsPojos.add(getStatModifierPojo(stat, value, false));

            // Generar línea de lore inmediatamente
            sum += value;
            Component statLine = createStatLoreLine(stat, value);
            tempStatLines.add(statLine);
        }

        // 3. Calcular rareza una sola vez
        int totalStats = statsPojos.size();
        double average = sum / totalStats;
        if (totalStats == 4) {
            average *= 0.85; // Aplicar penalización early game
        }

        this.rarity = RarityCalculator.calculateRarity(average);

        // 4. Construir el lore final
        preCalculatedStatLines.add(Component.empty());
        preCalculatedStatLines.add(Component.text("Bonos 1-5: ", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .append(this.rarity));
        preCalculatedStatLines.addAll(tempStatLines);
    }

    private boolean containsStat(Stat stat) {
        return statsPojos.stream().anyMatch(pojo -> pojo.stat.equals(stat));
    }

    // Método helper para crear líneas de lore de stats
    private Component createStatLoreLine(Stat stat, int value) {
        String valueText = " +" + value;
        return Component.text(valueText, ItemUtil.getColorOfStat(stat))
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(" " + stat.getDisplayName(AURA_LOCALE), TextColor.color(0x636363)))
                .decoration(TextDecoration.ITALIC, false);
    }

    private boolean containsGaussianStat(Stat stat) {
        return statsPojos.stream().anyMatch(pojo -> pojo.stat.equals(stat) && pojo.gaussian);
    }

    private boolean containsNormalStat(Stat stat) {
        return statsPojos.stream().anyMatch(pojo -> pojo.stat.equals(stat) && !pojo.gaussian);
    }

    protected void removeTraitModifierByName(ItemStack item, Trait trait, String name) {
        this.setItemMeta(AuraSkillsBukkit.get()
                .getItemManager()
                .removeTraitModifier(item, modifierType, trait, name)
                .getItemMeta());
    }

    protected void removeStatModifierByName(Stat stat, String name) {
        this.setItemMeta(AuraSkillsBukkit.get()
                .getItemManager()
                .removeStatModifier(this, modifierType, stat, name)
                .getItemMeta());
    }

    public StatModifier getHighestStatModifier() {
        // Obtener todos los modificadores de estadísticas nativas
        List<StatModifier> nativeModifiers = ITEM_MANAGER.getStatModifiersById(this, modifierType, NATIVE_STATMODIFIER);

        StatModifier highestModifier = null;
        double highestValue = Double.MIN_VALUE;

        // Encontrar el modificador con el valor más alto
        for (StatModifier modifier : nativeModifiers) {
            if (modifier.value() > highestValue) {
                highestValue = modifier.value();
                highestModifier = modifier;
            }
        }

        return highestModifier;
    }

    protected void generateStatsExceptHighestStat(Stat excludedStat, double excludedvalue) {
        CombinedStats excludedCombinedStat =
                CombinedStats.valueOf(excludedStat.getId().getKey().toUpperCase());
        int statsCount = getMaxBonuses();
        List<Stat> availableStats = statProvider.getAvailableStats();

        // Limpiar todas las listas
        statsPojos.clear();

        // 1. Procesar stats Gauss
        for (Stat stat : statProvider.getGaussStats()) {
            CombinedStats currentCombinedStat =
                    CombinedStats.valueOf(stat.getId().getKey().toUpperCase());
            if (!currentCombinedStat.equals(excludedCombinedStat)) {
                int value = StatValueGenerator.generateValueForStat(statProvider.isThisStatGauss(stat));
                statsPojos.add(getStatModifierPojo(stat, value, true));
            }
        }

        // Agregar el stat excluido a la lista apropiada
        if (statProvider.isThisStatGauss(excludedStat)) {
            statsPojos.add(getStatModifierPojo(excludedStat, (int) excludedvalue, true));
        } else {
            statsPojos.add(getStatModifierPojo(excludedStat, (int) excludedvalue, false));
        }

        // 2. Calcular stats adicionales necesarias
        int currentTotalStats = statsPojos.size();
        int remainingStats = statsCount - currentTotalStats;

        // 3. Agregar stats aleatorias hasta alcanzar el límite
        for (int i = 0; i < remainingStats; i++) {
            Stat stat;
            do {
                stat = availableStats.get(ThreadLocalRandom.current().nextInt(availableStats.size()));
            } while (containsStatOrExcluded(stat, excludedCombinedStat));

            int value = StatValueGenerator.generateValueForStat(statProvider.isThisStatGauss(stat));
            statsPojos.add(getStatModifierPojo(stat, value, false));
        }
    }

    private boolean containsStatOrExcluded(Stat stat, CombinedStats excludedCombinedStat) {
        // Verificar si ya está en las listas de POJOs
        boolean alreadyExists = containsStat(stat);

        // Verificar si es la stat excluida
        CombinedStats currentCombinedStat =
                CombinedStats.valueOf(stat.getId().getKey().toUpperCase());
        boolean isExcluded = currentCombinedStat.equals(excludedCombinedStat);

        return alreadyExists || isExcluded;
    }

    protected void removeAllModifierStatsByName(String name) {
        for (StatModifier modifier : getNativeStatModifiers()) {
            removeStatModifierByName(modifier.type(), name);
        }
    }

    protected void applyStatsToItem() {
        this.setItemMeta(ITEM_MANAGER
                .addStatModifierArray(this, statsPojos.toArray(new ItemManager.ModifierPojo[0]))
                .getItemMeta());
    }

    protected ItemMeta applyStatsToItemGetMeta() {
        return ITEM_MANAGER
                .addStatModifierArray(this, statsPojos.toArray(new ItemManager.ModifierPojo[0]))
                .getItemMeta();
    }

    public void removeModifiers() {
        this.setItemMeta(ITEM_MANAGER
                .removeStatModifiersByIdentifier(this, modifierType, NATIVE_STATMODIFIER)
                .getItemMeta());
    }

    public double calculateAverage() {
        // Si tenemos stats pre-calculadas, usar esas
        if (!preCalculatedStatLines.isEmpty() && !statsPojos.isEmpty()) {
            var sum = 0.0;
            int totalStats = 0;

            // Sumar todos los stats de la lista unificada
            for (ModifierPojoExtend pojo : statsPojos) {
                sum += pojo.value;
                totalStats++;
            }

            if (totalStats == 0) {
                throw new IllegalStateException("No hay estadísticas para calcular el promedio.");
            }

            double baseAverage = sum / totalStats;

            // Aplicar penalización solo si tiene 4 stats (early game)
            if (totalStats == 4) {
                return baseAverage * 0.85;
            }

            return baseAverage; // 5 stats, sin penalización
        }

        // Fallback al método original si no hay stats pre-calculadas
        List<StatModifier> nativeModifiers = ITEM_MANAGER.getStatModifiersById(this, modifierType, NATIVE_STATMODIFIER);
        if (nativeModifiers.isEmpty()) {
            throw new IllegalStateException("No hay estadísticas nativas para calcular el promedio.");
        }

        double totalValue = 0;
        int statCount = 0;

        // Sumar los valores de las estadísticas nativas
        for (StatModifier modifier : nativeModifiers) {
            totalValue += modifier.value();
            statCount++;
        }

        // Calcular el promedio base
        double baseAverage = totalValue / statCount;

        // Aplicar penalización solo si tiene 4 stats (early game)
        if (statCount == 4) {
            return baseAverage * 0.85;
        }

        return baseAverage; // 5 stats, sin penalización
    }

    public TextColor getRarityColor() {
        return this.rarity.color();
    }

    public int getLevel() {
        PersistentDataContainer container = this.getItemMeta().getPersistentDataContainer();
        return container.get(ItemConfig.LEVEL_KEY_NS, PersistentDataType.INTEGER);
    }

    public void setIdentifiedAndLevelNBT(int level) {
        ItemMeta meta = this.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(ItemConfig.SCROLLED_IDENTIFIER_KEY_NS, PersistentDataType.BOOLEAN, true);
        container.set(ItemConfig.LEVEL_KEY_NS, PersistentDataType.INTEGER, level);
        this.setItemMeta(meta);
    }

    protected int getMaxBonuses() {
        ItemMeta meta = this.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.get(ItemConfig.MAX_BONUSES_KEY_NS, PersistentDataType.INTEGER);
    }

    protected void setMaxBonuses(int amount) {
        ItemMeta meta = this.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(ItemConfig.MAX_BONUSES_KEY_NS, PersistentDataType.INTEGER, amount);
        this.setItemMeta(meta);
    }

    protected void getExtraBonusesLines() {
        if (this.getExtraBonuses().isPresent() && !this.getExtraBonuses().get().isEmpty()) {
            newLore.add(Component.empty());
            newLore.add(
                    (Component.text("Bonos 6-7:").color(NamedTextColor.GRAY)).decoration(TextDecoration.ITALIC, false));
            for (StatModifier statModifier : this.getExtraBonuses().get()) {
                String valueText = " +" + (int) statModifier.value();
                if (statModifier.operation() == AuraSkillsModifier.Operation.ADD_PERCENT) {
                    valueText = valueText + "%";
                }
                newLore.add(Component.text(valueText, ItemUtil.getColorOfStat(statModifier.stat()))
                        .decoration(TextDecoration.ITALIC, false)
                        .append(Component.text(
                                        " " + statModifier.stat().getDisplayName(ItemRarity.AURA_LOCALE),
                                        TextColor.color(0x636363))
                                .decoration(TextDecoration.ITALIC, false)));
            }
        }
    }

    public int addExtraBonus() {
        int actualSize = 0;
        if (this.getExtraBonuses().isPresent()) {
            actualSize = this.getExtraBonuses().get().size();
        }
        if (actualSize < MAX_EXTRA_BONUSES) {
            List<Stat> availableStats = this.statProvider.getAvailableStats();
            List currentExtraStats = this.getExtraBonuses()
                    .map(bonuses -> bonuses.stream()
                            .map(modifier -> modifier.type().name())
                            .toList())
                    .orElse(new ArrayList());
            List<Stat> availableNewStats = availableStats.stream()
                    .filter(stat -> !currentExtraStats.contains(stat.name()))
                    .toList();
            if (availableNewStats.isEmpty()) {
                return 0;
            }
            Stat stat2 = availableNewStats.get(ThreadLocalRandom.current().nextInt(availableNewStats.size()));
            this.addExtraStat(stat2);
            this.setNewLore();
            return actualSize + 1;
        }
        return 0;
    }

    private void addExtraStat(Stat stat) {
        this.setItemMeta(ITEM_MANAGER
                .addStatModifier(
                        this,
                        this.modifierType,
                        stat,
                        StatValueGenerator.generateValueForStat(false),
                        AuraSkillsModifier.Operation.ADD_PERCENT,
                        "extra",
                        false)
                .getItemMeta());
    }

    public int rerollExtraBonuses() {
        int actualSize = 0;
        Optional<List<StatModifier>> statModifiers = this.getExtraBonuses();
        if (statModifiers.isPresent()) {
            actualSize = statModifiers.get().size();
        }
        if (actualSize > 0) {
            for (CombinedStats stat : CombinedStats.values()) {
                this.removeStatModifierByName(stat.getDelegateStat(), "extra");
            }
            List<Stat> availableStats = this.statProvider.getAvailableStats();
            ArrayList<Stat> statsPool = new ArrayList<Stat>(availableStats);
            for (int i = 0; i < actualSize && !statsPool.isEmpty(); ++i) {
                int randomIndex = ThreadLocalRandom.current().nextInt(statsPool.size());
                Stat selectedStat = statsPool.get(randomIndex);
                this.addExtraStat(selectedStat);
                statsPool.remove(randomIndex);
            }
            this.setNewLore();
            return actualSize;
        }
        return 0;
    }

    private List<Stat> getMissingExtraStats() {
        List<Stat> availableStats = this.statProvider.getAvailableStats();
        List currentExtraStats = this.getExtraBonuses()
                .map(bonuses -> bonuses.stream()
                        .map(modifier -> (modifier.type()).name())
                        .toList())
                .orElse(new ArrayList<>());
        return availableStats.stream()
                .filter(stat -> !currentExtraStats.contains(stat.name()))
                .toList();
    }

    protected Optional<List<StatModifier>> getExtraBonuses() {
        return Optional.ofNullable(ITEM_MANAGER.getStatModifiersById(this, this.modifierType, "extra"));
    }

    public void setNewLore() {
        setNewLore(false);
    }



    public void setNewLore(boolean hardWipeName) {
        newLore = new ArrayList<>();
        // Si no tenemos rareza pre-calculada, calcularla normalmente
        if (this.rarity == null) {
            this.rarity = RarityCalculator.calculateRarity(calculateAverage());
        }
        // getMonoliticLines();
        getStatLines();
        getExtraBonusesLines();
        // getRarityLines();
        getRefinedLines();
        getGemsLines();
        getMultipliersLines();
        getEnchantmentLines();

        this.getItemMeta().addItemFlags(ItemFlag.HIDE_ENCHANTS);

        if (!itemType.isArmor()) {
            getItemMeta().addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            getAttributeLines();
        }
        this.lore(newLore);
        handleCustomName(this.getItemMeta(), hardWipeName);
    }

    // Método fusionado para líneas de refinado (estadísticas base + rareza)
    protected void getRefinedLines() {
        int currentLevel = this.getLevel();

        // Línea de encabezado con nivel de refinado
        Component refinedHeader;
        if (currentLevel >= MAX_LEVEL) {
            // Si está al máximo
            refinedHeader = Component.text("Refinado: ")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text("[+" + currentLevel + "] ")
                            .color(getLevelColor(currentLevel))
                            .decoration(TextDecoration.ITALIC, false))
                    .append(Component.text("(Máx.)")
                            .color(NamedTextColor.DARK_GRAY)
                            .decoration(TextDecoration.ITALIC, false));
        } else {
            // Si no está al máximo
            refinedHeader = Component.text("Refinado: ")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text("[+" + currentLevel + "] ")
                            .color(getLevelColor(currentLevel))
                            .decoration(TextDecoration.ITALIC, false))
                    .append(Component.text("→ ")
                            .color(NamedTextColor.DARK_GRAY)
                            .decoration(TextDecoration.ITALIC, false))
                    .append(Component.text("[+" + (currentLevel + 1) + "]")
                            .color(getLevelColor(currentLevel + 1))
                            .decoration(TextDecoration.ITALIC, false));
        }

        newLore.add(Component.empty());
        newLore.add(refinedHeader);

        // Debug: Verificar cuántos traits tenemos
        List<TraitModifier> traitModifiers = ITEM_MANAGER.getTraitModifiers(this, modifierType);

        // Agregar estadísticas base (traits monolíticos) apiladas verticalmente
        int traitCount = 0;
        for (TraitModifier traitModifier : traitModifiers) {
            traitCount++;

            var currentValue = traitModifier.value();
            var increasePerLevel = determineValueIncreasePerLevelForTrait(traitModifier.trait());
            var nextLevelValue = currentValue + increasePerLevel;

            Component traitLine = Component.text(" " + traitModifier.trait().getDisplayName(AURA_LOCALE))
                    .color(TextColor.color(0x636363))
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text(" +" + getFormattedValue(currentValue, traitModifier.trait()) + " ")
                            .color(getTraitColor(traitModifier.trait()))
                            .decoration(TextDecoration.ITALIC, false));

            // Agregar indicador del siguiente nivel solo si no está al máximo
            if (currentLevel < MAX_LEVEL) {
                Component nextLevelComponent = Component.text(" → ")
                        .color(NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false)
                        .append(Component.text("+" + getFormattedValue(nextLevelValue, traitModifier.trait()))
                                .color(NamedTextColor.DARK_GRAY)
                                .decoration(TextDecoration.ITALIC, false));
                traitLine = traitLine.append(nextLevelComponent);
            }

            newLore.add(traitLine);
        }
    }

    // Método auxiliar para obtener color según el nivel
    private TextColor getLevelColor(int level) {
        return switch (level) {
            case 1 -> TextColor.color(0x9A8772);
            case 2 -> TextColor.color(0x92704E);
            case 3 -> TextColor.color(0x996A28);
            case 4 -> TextColor.color(0xA87924);
            case 5 -> TextColor.color(0xB68921);
            case 6 -> TextColor.color(0xC5991E);
            case 7 -> TextColor.color(0xD3A91B);
            case 8 -> TextColor.color(0xE2B817);
            case 9 -> TextColor.color(0xF0C814);
            default -> NamedTextColor.RED;
        };
    }

    protected void getStatLines() {
        // Verificar si tenemos líneas de stats pre-calculadas
        if (!preCalculatedStatLines.isEmpty()) {
            newLore.addAll(preCalculatedStatLines);
            return;
        }

        // Fallback al método original con lookup de AuraSkills
        newLore.add(Component.empty());
        newLore.add(Component.text("Bonos 1-5: ", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .append(rarity));

        for (StatModifier statModifier : ITEM_MANAGER.getStatModifiersById(this, modifierType, NATIVE_STATMODIFIER)) {
            // Determinar si necesita el símbolo de porcentaje
            String valueText = " +" + (int) statModifier.value();
            if (statModifier.operation() == AuraSkillsModifier.Operation.ADD_PERCENT) {
                valueText += "%";
            }

            newLore.add(Component.text(valueText, ItemUtil.getColorOfStat(statModifier.stat()))
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text(
                            " " + statModifier.stat().getDisplayName(AURA_LOCALE), TextColor.color(0x636363)))
                    .decoration(TextDecoration.ITALIC, false));
        }
    }

    protected void getGemsLines() {

        // Add socket header
        newLore.add(Component.empty());
        newLore.add(GEMS_HEADER);
        // Add installed gems
        var installedGems = getInstalledGems();
        int availableSockets = getMaxSockets() - installedGems.size();
        for (StatModifier statModifier : installedGems) {
            TextColor statColor = ItemUtil.getColorOfStat(statModifier.stat());
            // Construcción del texto con colores aplicados explícitamente, otherwise ExcellentEnchants bugea el lore
            Component gemLine = Component.text(" \uD83D\uDC8E ", statColor) // Color explícito para el símbolo
                    .append(Component.text(statModifier.stat().getDisplayName(AURA_LOCALE))
                            .color(statColor)) // Color explícito para el nombre de la stat
                    .append(Component.text(String.format(" +%d", (int) statModifier.value()))
                            .color(statColor)) // Color explícito para el valor
                    .decoration(TextDecoration.ITALIC, false); // Quitar cursiva
            newLore.add(gemLine);
        }
        // Add empty sockets
        for (int i = 0; i < availableSockets; i++) {
            newLore.add(EMPTY_SOCKET);
        }
    }

    protected abstract int calculateGemValue(int value);

    protected abstract int getMaxSockets();

    protected abstract int getAvailableSockets();

    protected void getMultipliersLines() {

        List<Multiplier> multipliers = ITEM_MANAGER.getMultipliers(this, modifierType);

        if (multipliers.isEmpty()) {
            return;
        } else {
            newLore.add(Component.empty());
            newLore.add(Component.text("Multiplicadores XP:", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }

        for (Multiplier multiplier : ITEM_MANAGER.getMultipliers(this, modifierType)) {
            if (multiplier.skill() != null) {
                newLore.add(Component.text(" +" + (int) multiplier.value() + "%", TextColor.color(0x62FFF8))
                        .append(Component.text(
                                " XP de " + multiplier.skill().getDisplayName(AURA_LOCALE), TextColor.color(0x62FFF8)))
                        .decoration(TextDecoration.ITALIC, false));

            } else {
                newLore.add(Component.text(" +" + (int) multiplier.value() + "%", TextColor.color(0x16A46E))
                        .append(Component.text(" XP de Habilidades", TextColor.color(0x16A46E)))
                        .decoration(TextDecoration.ITALIC, false));
            }
        }
    }

    protected void getEnchantmentLines() {
        List<Component> lines = new ArrayList<>();
        var enchants = this.getEnchantments();
        if (!enchants.isEmpty()) {
            lines.add(Component.empty());
            lines.add(Component.text("Encantamientos:", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        }

        TextColor enchantColor;

        for (var enchant : enchants.keySet()) {

            enchantColor = !enchant.isCursed() ? TextColor.color(0x994999) : TextColor.color(0xC4322D);

            if (EnchantRegistry.getByKey(enchant.getKey()) != null) {

                CustomEnchantment custom = EnchantRegistry.getByKey(enchant.getKey());
                var line = custom.getBukkitEnchantment()
                        .displayName(enchants.get(enchant));

                String fullDescription = String.join("\n", custom.getDescription(enchants.get(enchant)));
                String descriptionOnly = fullDescription.substring(fullDescription.indexOf(":") + 1).trim();



                lines.add(Component.text(" ").decoration(TextDecoration.ITALIC,false)
                        .append(custom.getBukkitEnchantment()
                                .displayName(enchants.get(enchant))
                                .color(enchantColor)
                                .decoration(TextDecoration.ITALIC, false)
                                .append(Component.text("")
                                        .color(enchantColor)
                                        .decoration(TextDecoration.ITALIC, false))));
                //lines.add(parseDescriptionWithTranslations(descriptionOnly));
                lines.add(Component.text(" ➥ ",NamedTextColor.GRAY).decoration(TextDecoration.ITALIC,false)
                        .append(parseDescriptionWithMiniMessage(descriptionOnly).decoration(TextDecoration.ITALIC,false)));
            } else {

                lines.add(Component.text(" ").decoration(TextDecoration.ITALIC,false)
                        .append(enchant.displayName(enchants.get(enchant))
                                .color(enchantColor)
                                .append(Component.text("")))
                        .color(enchantColor)
                        .decoration(TextDecoration.ITALIC, false));
                lines.add(Component.text(" ➥ ",NamedTextColor.GRAY).decoration(TextDecoration.ITALIC,false)
                        .append(EnchantmentDescriptionManager.getVanillaEnchantDescription(enchant)
                                .color(TextColor.color(0x636363))
                                .decoration(TextDecoration.ITALIC, false)));
            }
        }
        newLore.addAll(lines);
    }

    // Método para procesar descripciones que pueden contener translation keys
    private Component parseDescriptionWithTranslations(String description) {
        // Patrón para encontrar translation keys en formato <lang:key>
        Pattern pattern = Pattern.compile("<lang:([^>]+)>");
        Matcher matcher = pattern.matcher(description);

        if (!matcher.find()) {
            // No hay translation keys, devolver como texto normal
            return Component.text(description);
        }

        // Hay translation keys, procesarlas
        Component result = Component.empty();
        int lastEnd = 0;

        // Reset matcher para volver a procesar desde el inicio
        matcher.reset();

        while (matcher.find()) {
            // Agregar texto antes de la translation key
            if (matcher.start() > lastEnd) {
                String beforeText = description.substring(lastEnd, matcher.start());
                result = result.append(Component.text(beforeText));
            }

            // Agregar la translation key como componente traducible
            String translationKey = matcher.group(1);
            result = result.append(Component.translatable(translationKey));

            lastEnd = matcher.end();
        }

        // Agregar cualquier texto restante después de la última translation key
        if (lastEnd < description.length()) {
            String remainingText = description.substring(lastEnd);
            result = result.append(Component.text(remainingText));
        }

        return result;
    }

    // Versión alternativa usando MiniMessage si lo tienes disponible
    private Component parseDescriptionWithMiniMessage(String description) {
        // Convertir <lang:key> a formato MiniMessage <lang:key>
        // MiniMessage ya maneja automáticamente las translation keys
        MiniMessage miniMessage = MiniMessage.miniMessage();

        // Reemplazar el formato para que MiniMessage lo entienda
       // String processedDescription = description.replaceAll("<lang:([^>]+)>", "<lang:$1>");

        return miniMessage.deserialize(description);
    }


    protected void getAttributeLines() {

        var meta = this.getItemMeta();

        if (getItemMeta().getAttributeModifiers() == null) {
            // trick to disable vanilla green attribute lines
            var defaultModifiers = this.getType().getDefaultAttributeModifiers();
            meta.setAttributeModifiers(defaultModifiers);
            this.setItemMeta(meta);
        }

        double baseDamage = 1;

        Collection<AttributeModifier> modifiers = meta.getAttributeModifiers(Attribute.ATTACK_DAMAGE);
        if (modifiers != null) {
            for (AttributeModifier modifier : modifiers) {
                if (modifier.getOperation() == AttributeModifier.Operation.ADD_NUMBER) {
                    baseDamage = baseDamage + modifier.getAmount(); // Usar el valor del modificador
                }
            }
        }

        double totalDamage = calculateTotalDamage(this, baseDamage);
        double attackSpeed = calculateAttackSpeed(this);
        String attackSpeedDisplay = String.format("%.1f", attackSpeed);

        newLore.add(Component.text("          "));
        newLore.add(
                Component.text("En la mano principal:", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        newLore.add(Component.text(
                        " " + ItemUtil.AttributesDF.format(totalDamage) + " Daño por ataque", NamedTextColor.BLUE)
                .decoration(TextDecoration.ITALIC, false));
        newLore.add(Component.text(" " + attackSpeedDisplay + " Velocidad de ataque", NamedTextColor.BLUE)
                .decoration(TextDecoration.ITALIC, false));
    }

    private TextColor getTraitColor(Trait trait) {
        // Primero convertimos el CombinedTrait a su equivalente en Traits si es necesario
        Trait originalTrait = (trait instanceof CombinedTraits) ? ((CombinedTraits) trait).getDelegateTrait() : trait;

        var stats = ITEM_MANAGER.getLinkedStats(originalTrait);
        if (stats.stream().findAny().isPresent()) {
            return ItemUtil.getColorOfStat(stats.stream().findAny().get());
        }
        return NamedTextColor.WHITE;
    }

    private String getFormattedValue(double value, Trait trait) {
        Trait originalTrait = (trait instanceof CombinedTraits) ? ((CombinedTraits) trait).getDelegateTrait() : trait;

        return ITEM_MANAGER.getFormattedTraitValue(value, originalTrait);
    }

    protected static double determineValueIncreasePerLevelForTrait(Trait trait) {
        switch (trait.name()) {
            case "ATTACK_DAMAGE" -> {
                return 1.0;
            }
            case "ATTACK_SPEED" -> {
                return 0.1;
            }
            case "DAMAGE_REDUCTION", "HP" -> {
                return 0.5;
            }
        }
        return 0;
    }

    public void handleCustomName(ItemMeta meta, boolean hard) {
        if (hard) {
            this.rarity = RarityCalculator.calculateRarity(calculateAverage());
        }

        int currentLevel = getLevel();
        TextColor levelColor = getLevelColor(currentLevel);
        Component levelSuffix =
                Component.text(" [+" + currentLevel + "]").color(levelColor).decoration(TextDecoration.ITALIC, false);

        if (NexoItems.exists(this)) {
            if (meta.hasCustomName()) {
                String plainText = PlainTextComponentSerializer.plainText().serialize(meta.customName());
                // Remover cualquier patrón de refinado existente
                String cleanedText = removeRefinementPattern(plainText, hard);

                Component component = Component.text(cleanedText, getRarityColor())
                        .decoration(TextDecoration.ITALIC, false)
                        .append(levelSuffix);
                meta.customName(component);
            } else {
                String plainText = PlainTextComponentSerializer.plainText().serialize(meta.itemName());
                // Remover cualquier patrón de refinado existente
                String cleanedText = removeRefinementPattern(plainText, hard);

                Component component = Component.text(cleanedText, getRarityColor())
                        .decoration(TextDecoration.ITALIC, false)
                        .append(levelSuffix);
                meta.itemName(component);
            }
        } else {
            if (meta.hasCustomName()) {
                String plainText = PlainTextComponentSerializer.plainText().serialize(meta.customName());
                // Remover cualquier patrón de refinado existente
                String cleanedText = removeRefinementPattern(plainText, hard);

                Component component = Component.text(cleanedText, getRarityColor())
                        .decoration(TextDecoration.ITALIC, false)
                        .append(levelSuffix);
                meta.customName(component);
            } else {
                String itemTranslationKey = this.translationKey();
                TranslatableComponent translatedName = Component.translatable(itemTranslationKey)
                        .color(getRarityColor())
                        .decoration(TextDecoration.ITALIC, false);

                Component component = translatedName.append(levelSuffix);
                meta.itemName(component);
            }
        }
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setTooltipStyle(RarityCalculator.getTooltip(rarity));
        this.setItemMeta(meta);
    }

    /**
     * Remueve cualquier patrón de refinado existente del texto del nombre del ítem.
     * Busca patrones como "[+1] ", "[+9] ", etc. al inicio del texto.
     *
     * @param text El texto original del nombre del ítem
     * @return El texto limpio sin patrones de refinado
     */
    private String removeRefinementPattern(String text, boolean hard) {
        // Patrón regex para detectar espacio seguido de [+número] al final del string
        // Ejemplo: " [+1]", " [+9]", etc.
        String pattern;
        if (hard) {
            pattern = "\\s*\\[\\+\\d+\\]";
            return text.replaceAll(pattern, "");
        } else {
            pattern = "\\s+\\[\\+\\d+\\]$";
            return text.replaceFirst(pattern, "");
        }
    }

    protected void addNativeStatModifier(Stat stat, double value, boolean randomOperations) {
        if (randomOperations && !statProvider.isThisStatGauss(stat)) {
            // Elegir aleatoriamente entre ADD y ADD_PERCENT
            AuraSkillsModifier.Operation operation = ThreadLocalRandom.current().nextBoolean()
                    ? AuraSkillsModifier.Operation.ADD
                    : AuraSkillsModifier.Operation.ADD_PERCENT;

            this.setItemMeta(AuraSkillsBukkit.get()
                    .getItemManager()
                    .addStatModifier(this, modifierType, stat, value, operation, NATIVE_STATMODIFIER, false)
                    .getItemMeta());
        } else {
            // Usar ADD por defecto cuando randomOperations es false
            this.setItemMeta(AuraSkillsBukkit.get()
                    .getItemManager()
                    .addStatModifier(
                            this,
                            modifierType,
                            stat,
                            value,
                            AuraSkillsModifier.Operation.ADD,
                            NATIVE_STATMODIFIER,
                            false)
                    .getItemMeta());
        }
    }

    protected void addMonoliticTraitModifier(ItemStack item, Trait trait, double value) {
        this.setItemMeta(AuraSkillsBukkit.get()
                .getItemManager()
                .addTraitModifier(
                        item,
                        modifierType,
                        trait,
                        value,
                        AuraSkillsModifier.Operation.ADD,
                        MONOLITIC_TRAITMODIFIER,
                        false)
                .getItemMeta());
    }

    public void rerollStatsEnhanced() {
        // Single operation to remove all modifiers matching ID
        this.setItemMeta(ITEM_MANAGER
                .removeStatModifiersByIdentifier(this, modifierType, NATIVE_STATMODIFIER)
                .getItemMeta());
        // Generate stats and apply in one go
        // Direct lore update without full reconstruction
        updateLoreDirect(generateStatsOptimized());
    }

    public void rerollLowestStat(Player player) {
        var statModifiers = getNativeStatModifiers();
        StatModifier lowestModifier = null;
        double lowestValue = Double.MAX_VALUE;

        // Encontrar el modificador con el valor más bajo
        for (StatModifier modifier : statModifiers) {
            if (modifier.value() < lowestValue) {
                lowestValue = modifier.value();
                lowestModifier = modifier;
            }
        }
        Stat stat = lowestModifier.stat();

        // Generar nuevo valor
        int newValue = StatValueGenerator.generateValueForStat(statProvider.isThisStatGauss(stat));

        // Solo remover y re-agregar el modificador específico
        removeStatModifierByName(stat, NATIVE_STATMODIFIER);
        addNativeStatModifier(stat, newValue, randomOperations);

        this.setNewLore();

        // Notificar al jugador
        Component message = Component.text("Nuevo bonus: ", BlessingObject.getLoreColor())
                .append(Component.text(stat.getDisplayName(AURA_LOCALE)).color(ItemUtil.getColorOfStat(stat)))
                .append(Component.text(" +").append(Component.text(newValue)).color(ItemUtil.getColorOfStat(stat)));
        player.sendMessage(ItemConfig.BLESSING_PREFIX.append(message));
    }

    private void updateLoreDirect(ItemMeta meta) {
        List<Component> currentLore = meta.lore();

        if (currentLore == null || currentLore.isEmpty()) {
            setNewLore();
            return;
        }

        // Find second empty component efficiently
        int emptyCount = 0;
        int secondEmptyIndex = -1;

        for (int i = 0; i < currentLore.size() && emptyCount < 2; i++) {
            if (currentLore.get(i).equals(Component.empty())) {
                emptyCount++;
                if (emptyCount == 2) {
                    secondEmptyIndex = i;
                    break;
                }
            }
        }

        if (secondEmptyIndex == -1) {
            setNewLore();
            return;
        }

        // Build new lore using ArrayList with known capacity
        int remainingSize = currentLore.size() - secondEmptyIndex;
        List<Component> updatedLore = new ArrayList<>(preCalculatedStatLines.size() + remainingSize);

        // Add pre-calculated stats lines
        updatedLore.addAll(preCalculatedStatLines);

        // Add remaining lore efficiently using subList
        updatedLore.addAll(currentLore.subList(secondEmptyIndex, currentLore.size()));

        // Single lore update
        meta.lore(updatedLore);

        // Update name only if rarity changed (check if needed)
        handleCustomName(meta, false);
    }

    public void rerollExceptHighestStat(Player player) {
        // Obtener la stat más alta válida
        StatModifier highestMod = getHighestStatModifier();
        Stat stat = highestMod.type();
        double value = highestMod.value();
        // generar las stat deseadas
        generateStatsExceptHighest(highestMod);
        // Remover todos los modificadores existentes
        removeModifiers();
        // Generar nuevas stats excepto la más alta
        applyStatsToItem();
        // Actualizar el lore
        this.setNewLore();

        // Notificar al jugador
        Component message = Component.text("¡Se conservó ")
                .color(RedemptionObject.getLoreColor())
                .append(Component.text("+" + value + " " + stat.getDisplayName(AURA_LOCALE))
                        .color(ItemUtil.getColorOfStat(stat)))
                .append(Component.text(" Rareza: ").append(rarity));
        player.sendMessage(ItemConfig.REDEMPTION_PREFIX.append(message));
    }

    public List<StatModifier> getNativeStatModifiers() {
        return ITEM_MANAGER.getStatModifiersById(this, modifierType, NATIVE_STATMODIFIER);
    }

    protected abstract List<StatModifier> getInstalledGems();

    protected abstract void setMonoliticStats(int level);
}