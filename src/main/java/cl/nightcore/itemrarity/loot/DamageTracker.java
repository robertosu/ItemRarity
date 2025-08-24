// DamageTracker.java - Sistema unificado para mobs normales y bosses
package cl.nightcore.itemrarity.loot;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.mythicProjectiles.boss.BossDifficulty;
import cl.nightcore.mythicProjectiles.boss.WorldBoss;
import cl.nightcore.mythicProjectiles.util.MobUtil;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class DamageTracker {

    // Thread pool para operaciones asÃ­ncronas
    private static final ExecutorService ASYNC_EXECUTOR = Executors.newFixedThreadPool(3);

    // Map de UUID del mob -> datos de daÃ±o
    private static final Map<UUID, MobDamageData> mobData = new ConcurrentHashMap<>();

    // Referencias a entidades para validaciÃ³n
    private static final Map<UUID, LivingEntity> entityReferences = new ConcurrentHashMap<>();

    // Task de limpieza automÃ¡tica
    private static BukkitTask cleanupTask;

    // ConfiguraciÃ³n diferenciada por tipo
    private static final double BOSS_MIN_DAMAGE_PERCENTAGE = 5.0;      // 5% para bosses
    private static final double MOB_MIN_DAMAGE_PERCENTAGE = 25.0;      // 25% para mobs normales
    private static final int MAX_LOOT_RECEIVERS = 10;

    // Tiempos de cache diferenciados
    private static final long BOSS_CACHE_EXPIRE_TIME = 300000;     // 5 minutos para bosses
    private static final long MOB_CACHE_EXPIRE_TIME = 120000;      // 2 minutos para mobs normales
    private static final long CLEANUP_INTERVAL = 20000;           // 20 segundos
    private static final long ENTITY_CHECK_INTERVAL = 45000;      // 45 segundos

    // InicializaciÃ³n estÃ¡tica
    static {
        startCleanupTask();
    }

    /**
     * Inicia la tarea de limpieza automÃ¡tica
     */
    private static void startCleanupTask() {
        if (cleanupTask != null) {
            cleanupTask.cancel();
        }

        cleanupTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                ItemRarity.PLUGIN,
                DamageTracker::performCleanup,
                CLEANUP_INTERVAL / 50,
                CLEANUP_INTERVAL / 50
        );
    }

    /**
     * Registra daÃ±o de forma asÃ­ncrona - funciona para mobs y bosses
     */
    public static void recordDamage(LivingEntity entity, Player player, double damage) {
        // Solo trackear mobs con nivel
        int mobLevel = MobUtil.getLevel(entity);
        if (mobLevel == 0) return;

        UUID entityId = entity.getUniqueId();
        UUID playerId = player.getUniqueId();

        // OperaciÃ³n asÃ­ncrona para no bloquear el hilo principal
        CompletableFuture.runAsync(() -> {
            // Almacenar referencia a la entidad
            entityReferences.put(entityId, entity);

            boolean isBoss = WorldBoss.isBoss(entity);
            BossDifficulty difficulty = WorldBoss.getBossDifficulty(entity);

            MobDamageData data = mobData.computeIfAbsent(entityId,
                    k -> new MobDamageData(entity.getAttribute(Attribute.MAX_HEALTH).getValue(), entityId, mobLevel, isBoss, difficulty));

            synchronized (data) {
                data.addDamage(playerId, damage);
            }
        }, ASYNC_EXECUTOR);
    }

    /**
     * Obtiene jugadores elegibles de forma asÃ­ncrona - unificado para mobs y bosses
     */
    public static CompletableFuture<List<LootReceiver>> getEligiblePlayersAsync(LivingEntity entity) {
        UUID entityId = entity.getUniqueId();

        return CompletableFuture.supplyAsync(() -> {
            MobDamageData data = mobData.remove(entityId);
            entityReferences.remove(entityId);

            if (data == null) {
                return new ArrayList<>();
            }

            // Usar porcentaje mÃ­nimo segÃºn el tipo de mob
            double minPercentage = data.isBoss() ? BOSS_MIN_DAMAGE_PERCENTAGE : MOB_MIN_DAMAGE_PERCENTAGE;

            return data.calculateLootReceivers(minPercentage, MAX_LOOT_RECEIVERS);
        }, ASYNC_EXECUTOR);
    }

    /**
     * VersiÃ³n sÃ­ncrona para compatibilidad
     */
    public static List<LootReceiver> getEligiblePlayers(LivingEntity entity) {
        try {
            return getEligiblePlayersAsync(entity).get();
        } catch (Exception e) {
            ItemRarity.PLUGIN.getLogger().warning("Error getting eligible players: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Limpieza inteligente del cache con tiempos diferenciados
     */
    private static void performCleanup() {
        long currentTime = System.currentTimeMillis();
        Set<UUID> toRemove = new HashSet<>();

        for (Map.Entry<UUID, MobDamageData> entry : mobData.entrySet()) {
            UUID entityId = entry.getKey();
            MobDamageData data = entry.getValue();

            // Determinar tiempo de expiraciÃ³n segÃºn tipo
            long expireTime = data.isBoss() ? BOSS_CACHE_EXPIRE_TIME : MOB_CACHE_EXPIRE_TIME;

            // Expirar por tiempo
            if (currentTime - data.getLastUpdate() > expireTime) {
                toRemove.add(entityId);
                continue;
            }

            // Verificar validez de entidad
            if (shouldCheckEntityValidity(data.getLastEntityCheck(), currentTime)) {
                LivingEntity entity = entityReferences.get(entityId);
                data.updateEntityCheck();

                if (!isEntityValid(entity)) {
                    toRemove.add(entityId);
                }
            }
        }

        // Remover entidades invÃ¡lidas
        for (UUID entityId : toRemove) {
            mobData.remove(entityId);
            entityReferences.remove(entityId);
        }

        if (!toRemove.isEmpty()) {
            long bosses = toRemove.stream()
                    .map(mobData::get)
                    .filter(Objects::nonNull)
                    .mapToLong(data -> data.isBoss() ? 1 : 0)
                    .sum();

            ItemRarity.PLUGIN.getLogger().fine(String.format(
                    "Cleaned up %d entities (%d bosses, %d mobs)",
                    toRemove.size(), bosses, toRemove.size() - bosses));
        }
    }

    /**
     * Obtiene informaciÃ³n de tracking para una entidad especÃ­fica
     */
    public static Optional<MobDamageData> getTrackingData(LivingEntity entity) {
        return Optional.ofNullable(mobData.get(entity.getUniqueId()));
    }

    /**
     * Verifica si una entidad estÃ¡ siendo trackeada
     */
    public static boolean isTracked(LivingEntity entity) {
        return mobData.containsKey(entity.getUniqueId());
    }

    /**
     * Fuerza el procesamiento de una entidad (para testing)
     */
    public static CompletableFuture<List<LootReceiver>> forceProcessEntity(LivingEntity entity) {
        return getEligiblePlayersAsync(entity);
    }

    private static boolean shouldCheckEntityValidity(long lastCheck, long currentTime) {
        return currentTime - lastCheck > ENTITY_CHECK_INTERVAL;
    }

    private static boolean isEntityValid(LivingEntity entity) {
        if (entity == null) return false;

        if (Bukkit.isPrimaryThread()) {
            return entity.isValid() && !entity.isDead();
        } else {
            try {
                return Bukkit.getScheduler().callSyncMethod(ItemRarity.PLUGIN, () ->
                        entity.isValid() && !entity.isDead()).get();
            } catch (Exception e) {
                return false;
            }
        }
    }

    /**
     * Obtiene estadÃ­sticas completas del tracker
     */
    public static TrackerStats getStats() {
        int totalEntities = mobData.size();
        long bosses = mobData.values().stream().mapToLong(data -> data.isBoss() ? 1 : 0).sum();
        long normalMobs = totalEntities - bosses;

        double avgAge = mobData.values().stream()
                .mapToLong(data -> System.currentTimeMillis() - data.getLastUpdate())
                .average()
                .orElse(0.0);

        return new TrackerStats(totalEntities, (int)bosses, (int)normalMobs,
                entityReferences.size(), avgAge);
    }

    /**
     * Debug: Lista todas las entidades siendo trackeadas
     */
    public static List<String> getTrackingInfo() {
        return mobData.values().stream()
                .map(data -> String.format("%s L%d (%s) - %d players, %.1f%% avg damage",
                        data.isBoss() ? "Boss" : "Mob",
                        data.getMobLevel(),
                        data.getDifficulty() != null ? data.getDifficulty().name() : "Normal",
                        data.getPlayerCount(),
                        data.getAverageContributionPercentage()))
                .sorted()
                .collect(Collectors.toList());
    }

    public static void shutdown() {
        if (cleanupTask != null) {
            cleanupTask.cancel();
        }

        mobData.clear();
        entityReferences.clear();

        if (!ASYNC_EXECUTOR.isShutdown()) {
            ASYNC_EXECUTOR.shutdown();
        }
    }

    public static void restart() {
        shutdown();
        startCleanupTask();
    }

    /**
     * Datos de daÃ±o unificados para mobs y bosses
     */
    public static class MobDamageData {
        private final Map<UUID, Double> playerDamage = new ConcurrentHashMap<>();
        private final double maxHealth;
        private final UUID entityId;
        private final int mobLevel;
        private final boolean isBoss;
        private final BossDifficulty difficulty;
        private double totalDamageDealt = 0.0;
        private volatile long lastUpdate;
        private volatile long lastEntityCheck;
        private final long creationTime;

        public MobDamageData(double maxHealth, UUID entityId, int mobLevel,
                             boolean isBoss, BossDifficulty difficulty) {
            this.maxHealth = maxHealth;
            this.entityId = entityId;
            this.mobLevel = mobLevel;
            this.isBoss = isBoss;
            this.difficulty = difficulty;
            this.creationTime = System.currentTimeMillis();
            this.lastUpdate = this.creationTime;
            this.lastEntityCheck = this.creationTime;
        }

        public void addDamage(UUID playerId, double damage) {
            playerDamage.merge(playerId, damage, Double::sum);
            totalDamageDealt += damage;
            lastUpdate = System.currentTimeMillis();
        }

        public void updateEntityCheck() {
            lastEntityCheck = System.currentTimeMillis();
        }

        public List<LootReceiver> calculateLootReceivers(double minPercentage, int maxReceivers) {
            return playerDamage.entrySet().stream()
                    .map(entry -> {
                        double damagePercentage = (entry.getValue() / maxHealth) * 100.0;
                        return new LootReceiver(entry.getKey(), entry.getValue(),
                                damagePercentage, mobLevel, isBoss, difficulty);
                    })
                    .filter(receiver -> receiver.damagePercentage() >= minPercentage)
                    .sorted((a, b) -> Double.compare(b.damagePercentage(), a.damagePercentage()))
                    .limit(maxReceivers)
                    .collect(Collectors.toList());
        }

        // Getters
        public double getTotalDamageDealt() { return totalDamageDealt; }
        public double getMaxHealth() { return maxHealth; }
        public long getLastUpdate() { return lastUpdate; }
        public long getLastEntityCheck() { return lastEntityCheck; }
        public long getCreationTime() { return creationTime; }
        public UUID getEntityId() { return entityId; }
        public int getMobLevel() { return mobLevel; }
        public boolean isBoss() { return isBoss; }
        public BossDifficulty getDifficulty() { return difficulty; }
        public Map<UUID, Double> getPlayerDamage() { return new HashMap<>(playerDamage); }
        public int getPlayerCount() { return playerDamage.size(); }

        public double getAverageContribution() {
            if (playerDamage.isEmpty()) return 0.0;
            return playerDamage.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);
        }

        public double getAverageContributionPercentage() {
            return (getAverageContribution() / maxHealth) * 100.0;
        }

        public double getDamagePercentage(UUID playerId) {
            Double damage = playerDamage.get(playerId);
            return damage != null ? (damage / maxHealth) * 100.0 : 0.0;
        }
    }

    /**
     * Receptor de loot con informaciÃ³n extendida
     */
    public record LootReceiver(UUID playerId, double damageDealt, double damagePercentage,
                               int mobLevel, boolean isBoss, BossDifficulty difficulty) {}

    /**
     * EstadÃ­sticas del tracker
     */
    public record TrackerStats(int totalEntities, int bosses, int normalMobs,
                               int totalReferences, double averageAge) {
        @Override
        public String toString() {
            return String.format("TrackerStats{entities: %d (bosses: %d, mobs: %d), refs: %d, avgAge: %.1fs}",
                    totalEntities, bosses, normalMobs, totalReferences, averageAge / 1000.0);
        }
    }
}