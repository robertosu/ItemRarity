package cl.nightcore.itemrarity.loot;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.item.BlessingObject;
import cl.nightcore.itemrarity.item.IdentifyScroll;
import cl.nightcore.itemrarity.item.MagicObject;
import cl.nightcore.itemrarity.item.RedemptionObject;
import cl.nightcore.itemrarity.util.MobUtil;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class CustomDropsManager implements Listener {

    private final Random random = new Random();
    private final List<DropConfig> globalDrops = new ArrayList<>();

    private final BlessingObject blessingObject = new BlessingObject(1, ItemRarity.PLUGIN);
    private final IdentifyScroll identifyScroll = new IdentifyScroll(1, ItemRarity.PLUGIN);
    private final MagicObject magicObject = new MagicObject(1, ItemRarity.PLUGIN);
    private final RedemptionObject redemptionObject = new RedemptionObject(1, ItemRarity.PLUGIN);

    private final Map<EntityType, List<DropConfig>> mobDrops = new HashMap<>();
    private final Map<String, List<DropConfig>> chestDrops = new HashMap<>();

    // Configuración para el escalado de probabilidad por nivel
    private static final double LEVEL_MULTIPLIER = 0.02; // 2% adicional por nivel
    private static final double MAX_BONUS_MULTIPLIER = 2.0; // Máximo 200% del drop base
    private static final int LEVEL_CAP = 50; // Nivel máximo para bonificación

    public CustomDropsManager() {
        loadDropConfigurations();
    }

    private void loadDropConfigurations() {
        // Obtener la lista de mobs hostiles
        List<EntityType> hostileMobs = enumToEntityTypes();

        // Configurar drops para todos los mobs hostiles
        addDropToEntities(hostileMobs, blessingObject, 0.01, 1, 2);
        addDropToEntities(hostileMobs, identifyScroll, 0.05, 1, 2);
        addDropToEntities(hostileMobs, magicObject, 0.1, 1, 2);
        addDropToEntities(hostileMobs, redemptionObject, 0.01, 1, 2);

        // Mantener la configuración de drops de cofres
        addGlobalChestDrop(blessingObject, 0.01, 1, 2);
        addGlobalChestDrop(identifyScroll, 0.01, 1, 2);
        addGlobalChestDrop(magicObject, 0.05, 1, 3);
        addGlobalChestDrop(redemptionObject, 0.05, 1, 2);
    }

    public void addDropToEntities(
            List<EntityType> entities, ItemStack item, double chance, int minAmount, int maxAmount) {
        for (EntityType entityType : entities) {
            addMobDrop(entityType, item, chance, minAmount, maxAmount);
        }
    }

    private List<EntityType> enumToEntityTypes() {
        return Arrays.stream(HostileMob.values())
                .map(HostileMob::getEntityType)
                .collect(Collectors.toList());
    }

    public void addGlobalChestDrop(ItemStack item, double chance, int minAmount, int maxAmount) {
        globalDrops.add(new DropConfig(item, chance, minAmount, maxAmount));
    }

    public void addMobDrop(EntityType entityType, ItemStack item, double chance, int minAmount, int maxAmount) {
        DropConfig dropConfig = new DropConfig(item, chance, minAmount, maxAmount);
        mobDrops.computeIfAbsent(entityType, k -> new ArrayList<>()).add(dropConfig);
    }

    public void addChestDrop(String lootTableKey, ItemStack item, double chance, int minAmount, int maxAmount) {
        DropConfig dropConfig = new DropConfig(item, chance, minAmount, maxAmount);
        chestDrops.computeIfAbsent(lootTableKey, k -> new ArrayList<>()).add(dropConfig);
    }

    private int getRandomAmount(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    /**
     * Calcula la probabilidad ajustada basada en el nivel del mob
     * @param baseChance Probabilidad base del drop
     * @param mobLevel Nivel del mob
     * @return Probabilidad ajustada
     */
    private double calculateLevelAdjustedChance(double baseChance, int mobLevel) {
        if (mobLevel <= 0) return baseChance;

        // Limitar el nivel al máximo configurado
        int effectiveLevel = Math.min(mobLevel, LEVEL_CAP);

        // Calcular el multiplicador basado en el nivel
        double levelBonus = 1.0 + (effectiveLevel * LEVEL_MULTIPLIER);

        // Limitar el multiplicador máximo
        levelBonus = Math.min(levelBonus, MAX_BONUS_MULTIPLIER);

        // Aplicar el bonus y asegurar que no exceda 1.0 (100%)
        return Math.min(baseChance * levelBonus, 1.0);
    }

    /**
     * Versión alternativa: escalado exponencial para niveles altos
     */
    private double calculateExponentialChance(double baseChance, int mobLevel) {
        if (mobLevel <= 0) return baseChance;

        int effectiveLevel = Math.min(mobLevel, LEVEL_CAP);

        // Fórmula exponencial: baseChance * (1 + level^1.2 * 0.01)
        double exponentialBonus = 1.0 + (Math.pow(effectiveLevel, 1.2) * 0.01);
        exponentialBonus = Math.min(exponentialBonus, MAX_BONUS_MULTIPLIER);

        return Math.min(baseChance * exponentialBonus, 1.0);
    }

    /**
     * Versión por rangos de niveles con bonificaciones específicas
     */
    private double calculateTieredChance(double baseChance, int mobLevel) {
        if (mobLevel <= 0) return baseChance;

        double multiplier = 1.0;

        if (mobLevel >= 1 && mobLevel <= 10) {
            multiplier = 1.2; // +20%
        } else if (mobLevel >= 11 && mobLevel <= 25) {
            multiplier = 1.5; // +50%
        } else if (mobLevel >= 26 && mobLevel <= 40) {
            multiplier = 2.0; // +100%
        } else if (mobLevel >= 41) {
            multiplier = 2.5; // +150%
        }

        return Math.min(baseChance * multiplier, 1.0);
    }

    /**
     * Calcula la cantidad ajustada basada en el nivel (opcional)
     */
    private int calculateLevelAdjustedAmount(int baseMin, int baseMax, int mobLevel) {
        if (mobLevel <= 0) return getRandomAmount(baseMin, baseMax);

        // Bonus de cantidad basado en nivel (cada 10 niveles +1 item extra posible)
        int levelBonus = mobLevel / 10;
        int adjustedMax = baseMax + levelBonus;

        return getRandomAmount(baseMin, adjustedMax);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getDamageSource().getCausingEntity() instanceof Player) return;

        LivingEntity entity = event.getEntity();
        int mobLevel = MobUtil.getLevel(entity);

        List<DropConfig> possibleDrops = mobDrops.get(entity.getType());

        if (possibleDrops != null) {
            for (DropConfig dropConfig : possibleDrops) {
                // Usar la probabilidad ajustada por nivel
                double adjustedChance = calculateLevelAdjustedChance(dropConfig.chance, mobLevel);

                if (random.nextDouble() < adjustedChance) {
                    ItemStack itemToDrop = dropConfig.item.clone();

                    // Opcionalmente, también ajustar la cantidad por nivel
                    int amount = calculateLevelAdjustedAmount(
                            dropConfig.minAmount,
                            dropConfig.maxAmount,
                            mobLevel
                    );

                    itemToDrop.setAmount(amount);
                    entity.getWorld().dropItemNaturally(entity.getLocation(), itemToDrop);

                    // Debug opcional
                    if (mobLevel > 0) {
                        System.out.println(String.format(
                                "Mob nivel %d droppeó %s (chance: %.2f%% -> %.2f%%)",
                                mobLevel,
                                itemToDrop.getType().name(),
                                dropConfig.chance * 100,
                                adjustedChance * 100
                        ));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onLootGenerate(LootGenerateEvent event) {
        LootTable lootTable = event.getLootTable();
        String lootTableKey = lootTable.getKey().toString();
        List<ItemStack> loot = event.getLoot();

        // Procesar drops globales (sin ajuste de nivel para cofres)
        for (DropConfig dropConfig : globalDrops) {
            if (random.nextDouble() < dropConfig.chance) {
                ItemStack itemToAdd = dropConfig.item.clone();
                int amount = getRandomAmount(dropConfig.minAmount, dropConfig.maxAmount);
                itemToAdd.setAmount(amount);
                loot.add(itemToAdd);
            }
        }

        // Procesar drops específicos para esta loottable
        List<DropConfig> specificDrops = chestDrops.get(lootTableKey);
        if (specificDrops != null) {
            for (DropConfig dropConfig : specificDrops) {
                if (random.nextDouble() < dropConfig.chance) {
                    ItemStack itemToAdd = dropConfig.item.clone();
                    int amount = getRandomAmount(dropConfig.minAmount, dropConfig.maxAmount);
                    itemToAdd.setAmount(amount);
                    loot.add(itemToAdd);
                }
            }
        }
    }

    private record DropConfig(ItemStack item, double chance, int minAmount, int maxAmount) {}
}