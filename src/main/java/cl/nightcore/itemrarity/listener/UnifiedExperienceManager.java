// UnifiedExperienceManager.java - Con experiencia dividida y verificación de distancia
package cl.nightcore.itemrarity.listener;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.loot.LevelGapManager;
import cl.nightcore.itemrarity.loot.PartyManager;
import cl.nightcore.itemrarity.loot.DamageTracker;
import cl.nightcore.mythicProjectiles.boss.BossDifficulty;
import cl.nightcore.mythicProjectiles.boss.WorldBoss;
import cl.nightcore.mythicProjectiles.util.MobUtil;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.event.skill.EntityXpGainEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Gestor unificado de experiencia que centraliza toda la lógica de XP
 * CON EXPERIENCIA DIVIDIDA Y VERIFICACIÓN DE DISTANCIA
 */
public class UnifiedExperienceManager implements Listener {

    // Configuración de multiplicadores (copiados exactamente de XpGainListener)
    private static final double MOB_XP_MULTIPLIER_BASE = 1.025;
    private static final double BOSS_XP_MULTIPLIER_BASE = 1.0425;

    // 🔥 NUEVA CONFIGURACIÓN: División de XP
    private static final boolean DIVIDE_XP_AMONG_PARTY = true; // true = dividir, false = duplicar
    private AuraSkillsApi auraSkillsApi;

    public UnifiedExperienceManager() {
        try {
            this.auraSkillsApi = AuraSkillsApi.get();
            ItemRarity.PLUGIN.getLogger().info("UnifiedExperienceManager initialized with AuraSkills support");
            ItemRarity.PLUGIN.getLogger().info("Distance checking: " +
                    (PartyManager.DISTANCE_CHECK_ENABLED ? "ENABLED (" + PartyManager.MAX_DISTANCE_FOR_REWARDS + " blocks)" : "DISABLED"));
            ItemRarity.PLUGIN.getLogger().info("XP Distribution: " +
                    (DIVIDE_XP_AMONG_PARTY ? "DIVIDED among party" : "DUPLICATED for all"));
        } catch (Exception e) {
            ItemRarity.PLUGIN.getLogger().warning("AuraSkills not found in UnifiedExperienceManager");
            this.auraSkillsApi = null;
        }
    }

    /**
     * Calcula XP para mobs normales - copia exacta de XpGainListener
     */
    public static double calculateMobXP(int mobLevel, double baseXP) {
        if (mobLevel <= 0) return baseXP;
        double xpMultiplier = Math.pow(MOB_XP_MULTIPLIER_BASE, mobLevel - 1);
        return baseXP * xpMultiplier;
    }

    /**
     * Calcula XP para bosses - copia exacta de XpGainListener
     */
    public static double calculateBossXP(int mobLevel, double baseXP) {
        if (mobLevel <= 0) return baseXP;
        double xpMultiplier = Math.pow(BOSS_XP_MULTIPLIER_BASE, mobLevel - 1);
        return baseXP * xpMultiplier;
    }

    /**
     * Método de utilidad para obtener información de cálculo de XP
     */
    public static XPCalculationInfo getXPCalculationInfo(LivingEntity entity, int baseVanillaXP) {
        int mobLevel = MobUtil.getLevel(entity);
        boolean isBoss = WorldBoss.isBoss(entity);
        BossDifficulty difficulty = WorldBoss.getBossDifficulty(entity);

        double vanillaMultiplier;
        if (isBoss) {
            vanillaMultiplier = Math.pow(BOSS_XP_MULTIPLIER_BASE, mobLevel - 1);
        } else {
            vanillaMultiplier = Math.pow(MOB_XP_MULTIPLIER_BASE, mobLevel - 1);
        }

        double auraMultiplier = Math.pow(MOB_XP_MULTIPLIER_BASE, mobLevel - 1);

        return new XPCalculationInfo(mobLevel, isBoss, difficulty,
                vanillaMultiplier, auraMultiplier, baseVanillaXP);
    }

    /**
     * Método de utilidad para debugging - muestra cálculos de XP
     */
    public static void debugXPCalculation(LivingEntity entity, int baseVanillaXP) {
        XPCalculationInfo info = getXPCalculationInfo(entity, baseVanillaXP);
        ItemRarity.PLUGIN.getLogger().info("XP Debug: " + info.toString());
    }

    /**
     * Intercepta y modifica la experiencia de AuraSkills para mobs con nivel
     * Este evento se dispara cuando el jugador hace daño - mantenemos funcionalidad original
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAuraSkillsXpGain(EntityXpGainEvent event) {
        if (auraSkillsApi == null) return;

        LivingEntity source = event.getAttacked();
        int mobLevel = MobUtil.getLevel(source);

        if (mobLevel == 0) {
            return; // No modificar XP de mobs sin nivel
        }

        // Aplicar el mismo cálculo que XpGainListener original
        double originalXp = event.getAmount();
        double modifiedXp = calculateMobXP(mobLevel, originalXp);

        event.setAmount(modifiedXp);
    }

    /**
     * 🔥 ACTUALIZADO: Maneja la experiencia vanilla cuando muere un mob
     * Ahora con verificación de distancia y división de XP
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();

        int mobLevel = MobUtil.getLevel(entity);
        if (mobLevel == 0) {
            return; // No modificar XP vanilla de mobs sin nivel
        }

        // Obtener XP base que iba a dropear el mob
        int baseExp = event.getDroppedExp();
        if (baseExp <= 0) return;

        // Cancelar el drop de orbes para hacer distribución manual
        event.setDroppedExp(0);

        // Determinar si es boss y su dificultad
        boolean isBoss = WorldBoss.isBoss(entity);
        BossDifficulty bossDifficulty = WorldBoss.getBossDifficulty(entity);

        // Calcular XP modificada según el tipo de mob
        double modifiedXp;
        if (isBoss) {
            modifiedXp = calculateBossXP(mobLevel, baseExp);
            if (bossDifficulty != null) {
                modifiedXp *= bossDifficulty.getDamageMultiplier();
            }
        } else {
            modifiedXp = calculateMobXP(mobLevel, baseExp);
        }

        // 🔥 NUEVO: Procesar distribución con verificación de distancia
        if (killer != null) {
            processVanillaExperienceDistribution(killer, entity, mobLevel, isBoss,
                    bossDifficulty, (int) Math.round(modifiedXp), entity.getLocation());
        }
    }

    /**
     * 🔥 ACTUALIZADO: Procesa la distribución de experiencia vanilla entre jugadores elegibles
     * Ahora con verificación de distancia y división de XP
     */
    private void processVanillaExperienceDistribution(Player killer, LivingEntity entity,
                                                      int mobLevel, boolean isBoss,
                                                      BossDifficulty bossDifficulty, int modifiedExp,
                                                      Location deathLocation) {

        // Usar el sistema unificado de damage tracking
        DamageTracker.getEligiblePlayersAsync(entity)
                .thenAccept(lootReceivers -> {
                    Set<Player> eligiblePlayers = new HashSet<>();

                    if (lootReceivers.isEmpty()) {
                        // Si nadie hizo suficiente daño, usar sistema tradicional (solo el killer)
                        if (killer != null) {
                            if (isBoss) {
                                eligiblePlayers.addAll(PartyManager.getPartyMembersForBoss(killer));
                            } else {
                                eligiblePlayers.addAll(PartyManager.getPartyMembersForNormalMob(killer));
                            }
                        }
                    } else {
                        // Para cada jugador elegible, agregar su party
                        for (var receiver : lootReceivers) {
                            Player damageDealer = Bukkit.getPlayer(receiver.playerId());
                            if (damageDealer != null && damageDealer.isOnline()) {
                                if (isBoss) {
                                    eligiblePlayers.addAll(PartyManager.getPartyMembersForBoss(damageDealer));
                                } else {
                                    eligiblePlayers.addAll(PartyManager.getPartyMembersForNormalMob(damageDealer));
                                }
                            }
                        }
                    }

                    // 🔥 NUEVO: Filtrar por distancia
                    Set<Player> playersInRange = PartyManager.filterPlayersByDistance(eligiblePlayers, deathLocation);

                    // Logging de jugadores filtrados por distancia
                    if (PartyManager.DISTANCE_CHECK_ENABLED && playersInRange.size() < eligiblePlayers.size()) {
                        Set<Player> playersOutOfRange = new HashSet<>(eligiblePlayers);
                        playersOutOfRange.removeAll(playersInRange);

                        ItemRarity.PLUGIN.getLogger().fine(String.format(
                                "%s %s: %d players filtered out by distance (>%.1f blocks): %s",
                                isBoss ? "Boss" : "Mob",
                                entity.getType().name(),
                                playersOutOfRange.size(),
                                PartyManager.MAX_DISTANCE_FOR_REWARDS,
                                playersOutOfRange.stream().map(Player::getName).collect(Collectors.joining(", "))
                        ));
                    }

                    // Distribuir XP en el hilo principal
                    if (!playersInRange.isEmpty()) {
                        Bukkit.getScheduler().runTask(ItemRarity.PLUGIN, () -> {
                            distributeExperience(playersInRange, mobLevel, isBoss, modifiedExp, deathLocation);
                        });
                    }
                })
                .exceptionally(throwable -> {
                    ItemRarity.PLUGIN.getLogger().warning("Error processing XP distribution: " + throwable.getMessage());

                    // Fallback al sistema tradicional
                    if (killer != null) {
                        Set<Player> fallbackPlayers;
                        if (isBoss) {
                            fallbackPlayers = PartyManager.getPartyMembersForBoss(killer);
                        } else {
                            fallbackPlayers = PartyManager.getPartyMembersForNormalMob(killer);
                        }

                        // 🔥 NUEVO: Filtrar fallback también por distancia
                        Set<Player> fallbackInRange = PartyManager.filterPlayersByDistance(fallbackPlayers, deathLocation);

                        Bukkit.getScheduler().runTask(ItemRarity.PLUGIN, () -> {
                            distributeExperience(fallbackInRange, mobLevel, isBoss, modifiedExp, deathLocation);
                        });
                    }

                    return null;
                });
    }



    /**
     * 🔥 ACTUALIZADO: Distribuye experiencia entre jugadores elegibles
     * Ahora con división de XP real entre miembros de party
     */
    private void distributeExperience(Set<Player> eligiblePlayers, int mobLevel, boolean isBoss,
                                      int modifiedExp, Location deathLocation) {

        if (eligiblePlayers.isEmpty()) return;

        // 🔥 NUEVA LÓGICA: Dividir XP total entre jugadores válidos
        int validPlayersCount = 0;

        // Primer pase: contar jugadores válidos
        for (Player player : eligiblePlayers) {
            if (LevelGapManager.shouldCancelDrop(player, mobLevel)) {
                continue; // Excluir por level gap
            }


            double levelPenalty = LevelGapManager.getLevelPenaltyMultiplier(player, mobLevel);
            if (levelPenalty <= 0) {
                continue; // Excluir por penalización
            }

            validPlayersCount++;
        }

        if (validPlayersCount == 0) return;

        // 🔥 CALCULAR XP BASE POR JUGADOR
        int baseExpPerPlayer;
        if (DIVIDE_XP_AMONG_PARTY) {
            // MODO DIVISIÓN: XP total dividida entre miembros válidos
            baseExpPerPlayer = modifiedExp / validPlayersCount;
        } else {
            // MODO DUPLICACIÓN: XP completa para todos (comportamiento anterior)
            baseExpPerPlayer = modifiedExp;
        }

        // Segundo pase: distribuir XP
        for (Player player : eligiblePlayers) {
            // Verificar diferencia de niveles
            if (LevelGapManager.shouldCancelDrop(player, mobLevel)) {
                continue;
            }

            double levelPenalty = LevelGapManager.getLevelPenaltyMultiplier(player, mobLevel);
            if (levelPenalty <= 0) continue;

            // 🔥 MODIFICADO: No aplicar party bonus cuando dividimos XP
            // (el bonus ya está implícito en estar en party y recibir drops)
            double partyBonus = DIVIDE_XP_AMONG_PARTY ? 1.0 : PartyManager.getPartyExpBonus(player.getUniqueId());

            int finalExp = (int) Math.round(baseExpPerPlayer * levelPenalty * partyBonus);

            if (finalExp > 0) {
                player.giveExp(finalExp);

                // 🔥 IMPROVED DEBUG: Mostrar información de división
                if (eligiblePlayers.size() > 1 || PartyManager.DISTANCE_CHECK_ENABLED) {
                    ItemRarity.PLUGIN.getLogger().fine(String.format(
                            "%s XP: %s received %d XP (%s: %d total ÷ %d players, penalty: %.2f, party: %.2f, dist: %.1f)",
                            isBoss ? "Boss" : "Mob",
                            player.getName(),
                            finalExp,
                            DIVIDE_XP_AMONG_PARTY ? "Divided" : "Duplicated",
                            modifiedExp,
                            validPlayersCount,
                            levelPenalty,
                            partyBonus,
                            player.getLocation().distance(deathLocation)
                    ));
                }
            }
        }

     /*   // 🔥 LOGGING RESUMEN
        if (isBoss || (eligiblePlayers.size() > 1)) {
            ItemRarity.PLUGIN.getLogger().info(String.format(
                    "[XP-DISTRIBUTED] %s %s L%d: %d total XP %s among %d players (%d base per player)",
                    isBoss ? "Boss" : "Mob",
                    entity.getType().name(),
                    mobLevel,
                    modifiedExp,
                    DIVIDE_XP_AMONG_PARTY ? "divided" : "duplicated",
                    validPlayersCount,
                    baseExpPerPlayer
            ));
        }*/
    }

    /**
     * Verifica si AuraSkills está disponible
     */
    public boolean isAuraSkillsAvailable() {
        return auraSkillsApi != null;
    }

    /**
     * 🔥 NUEVOS MÉTODOS DE CONFIGURACIÓN
     */
    public static double getMaxRewardDistance() {
        return PartyManager.MAX_DISTANCE_FOR_REWARDS;
    }

    public static boolean isDistanceCheckEnabled() {
        return PartyManager.DISTANCE_CHECK_ENABLED;
    }

    public static boolean isDivideXpAmongParty() {
        return DIVIDE_XP_AMONG_PARTY;
    }

    /**
     * Clase de información para debugging y cálculos de XP
     */
    public static class XPCalculationInfo {
        private final int mobLevel;
        private final boolean isBoss;
        private final BossDifficulty bossDifficulty;
        private final double vanillaXpMultiplier;
        private final double auraSkillsXpMultiplier;
        private final int baseVanillaXP;

        public XPCalculationInfo(int mobLevel, boolean isBoss, BossDifficulty bossDifficulty,
                                 double vanillaXpMultiplier, double auraSkillsXpMultiplier, int baseVanillaXP) {
            this.mobLevel = mobLevel;
            this.isBoss = isBoss;
            this.bossDifficulty = bossDifficulty;
            this.vanillaXpMultiplier = vanillaXpMultiplier;
            this.auraSkillsXpMultiplier = auraSkillsXpMultiplier;
            this.baseVanillaXP = baseVanillaXP;
        }

        public int calculateFinalVanillaXP() {
            double finalXP = baseVanillaXP * vanillaXpMultiplier;
            if (isBoss && bossDifficulty != null) {
                finalXP *= bossDifficulty.getDamageMultiplier();
            }
            return (int) Math.round(finalXP);
        }

        public double calculateFinalAuraSkillsXP(double baseAuraXP) {
            return baseAuraXP * auraSkillsXpMultiplier;
        }

        // Getters
        public int getMobLevel() { return mobLevel; }
        public boolean isBoss() { return isBoss; }
        public double getVanillaXpMultiplier() { return vanillaXpMultiplier; }
        public double getAuraSkillsXpMultiplier() { return auraSkillsXpMultiplier; }
        public BossDifficulty getBossDifficulty() { return bossDifficulty; }
        public int getBaseVanillaXP() { return baseVanillaXP; }

        @Override
        public String toString() {
            return String.format("XPInfo{level: %d, boss: %s, vanillaMulti: %.3f, auraMulti: %.3f, " +
                            "baseVanilla: %d, finalVanilla: %d, difficulty: %s}",
                    mobLevel, isBoss, vanillaXpMultiplier, auraSkillsXpMultiplier,
                    baseVanillaXP, calculateFinalVanillaXP(),
                    bossDifficulty != null ? bossDifficulty.name() : "None");
        }
    }
}