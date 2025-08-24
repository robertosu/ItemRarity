package cl.nightcore.itemrarity.item.potion;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.config.CombinedStats;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PotionManager {

    private static final double DEFAULT_VALUE = 10.0;
    private static final int DEFAULT_DURATION = 300; // 5 minutos

    // Enum para niveles de pociones con valores fijos
    public enum PotionLevel {
        ONE(5),
        TWO(10),
        THREE(15),
        FOUR(20),
        FIVE(25);

        private final double value;

        PotionLevel(double value) {
            this.value = value;
        }

        public double getValue() {
            return value;
        }
    }

    // Enum para duraciones con valores fijos
    public enum PotionDuration {
        LOW(90),
        MEDIUM(180),
        HIGH(270),
        HIGHEST(360);

        private final int seconds;

        PotionDuration(int seconds) {
            this.seconds = seconds;
        }

        public int getSeconds() {
            return seconds;
        }

        public int getTicks() {
            return seconds * 20;
        }

        public String getFormattedTime() {
            int totalSeconds = seconds;
            int minutes = totalSeconds / 60;
            int remainingSeconds = totalSeconds % 60;
            return String.format("%d:%02d", minutes, remainingSeconds);
        }
    }

    /**
     * Crea una poción con nivel y duración específicos usando enums
     */
    public static StatPotion createPotionByLevel(CombinedStats stat, PotionLevel level, PotionDuration duration) {
        return new StatPotion(stat, level.getValue(), duration.getSeconds());
    }

    /**
     * Crea una poción con solo nivel específico (duración media por defecto)
     */
    public static StatPotion createPotionByLevel(CombinedStats stat, PotionLevel level) {
        return createPotionByLevel(stat, level, PotionDuration.MEDIUM);
    }

    /**
     * Crea múltiples pociones del mismo nivel y duración
     */
    public static List<StatPotion> createPotionsByLevel(CombinedStats stat, PotionLevel level,
                                                        PotionDuration duration, int amount) {
        List<StatPotion> potions = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            potions.add(createPotionByLevel(stat, level, duration));
        }
        return potions;
    }

    /**
     * Crea un set completo de pociones para todos los stats con el mismo nivel y duración
     */
    public static List<StatPotion> createCompleteSetByLevel(PotionLevel level, PotionDuration duration) {
        List<StatPotion> potions = new ArrayList<>();
        for (CombinedStats stat : CombinedStats.values()) {
            potions.add(createPotionByLevel(stat, level, duration));
        }
        return potions;
    }

    /**
     * Crea un set de pociones para stats específicos con el mismo nivel y duración
     */
    public static List<StatPotion> createStatSetByLevel(CombinedStats[] stats, PotionLevel level,
                                                        PotionDuration duration) {
        List<StatPotion> potions = new ArrayList<>();
        for (CombinedStats stat : stats) {
            potions.add(createPotionByLevel(stat, level, duration));
        }
        return potions;
    }

    /**
     * Crea pociones escalonadas (diferentes niveles para el mismo stat)
     */
    public static List<StatPotion> createTieredPotions(CombinedStats stat, PotionDuration duration) {
        List<StatPotion> potions = new ArrayList<>();
        for (PotionLevel level : PotionLevel.values()) {
            potions.add(createPotionByLevel(stat, level, duration));
        }
        return potions;
    }

    /**
     * Crea todas las variaciones posibles de una stat (todos los niveles y duraciones)
     */
    public static List<StatPotion> createAllVariations(CombinedStats stat) {
        List<StatPotion> potions = new ArrayList<>();
        for (PotionLevel level : PotionLevel.values()) {
            for (PotionDuration duration : PotionDuration.values()) {
                potions.add(createPotionByLevel(stat, level, duration));
            }
        }
        return potions;
    }

    // ========== MÉTODOS EXISTENTES MANTENIDOS ==========

    /**
     * Crea una sola poción con parámetros específicos
     */
    public static StatPotion createPotion(CombinedStats stat, double value, int duration) {
        return new StatPotion(stat, value, duration);
    }

    /**
     * Crea una poción con valores por defecto
     */
    public static StatPotion createPotion(CombinedStats stat) {
        return createPotion(stat, DEFAULT_VALUE, DEFAULT_DURATION);
    }

    /**
     * Crea múltiples pociones del mismo tipo
     */
    public static List<StatPotion> createPotions(CombinedStats stat, double value, int duration, int amount) {
        List<StatPotion> potions = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            potions.add(createPotion(stat, value, duration));
        }
        return potions;
    }

    /**
     * Crea múltiples pociones con valores por defecto
     */
    public static List<StatPotion> createPotions(CombinedStats stat, int amount) {
        return createPotions(stat, DEFAULT_VALUE, DEFAULT_DURATION, amount);
    }

    /**
     * Crea una poción con valores aleatorios dentro de rangos específicos
     */
    public static StatPotion createRandomPotion(CombinedStats stat, double minValue, double maxValue,
                                                int minDuration, int maxDuration) {
        double randomValue = ThreadLocalRandom.current().nextDouble(minValue, maxValue);
        int randomDuration = ThreadLocalRandom.current().nextInt(minDuration, maxDuration + 1);
        return createPotion(stat, randomValue, randomDuration);
    }

    /**
     * Crea una poción completamente aleatoria
     */
    public static StatPotion createRandomPotion() {
        CombinedStats[] allStats = CombinedStats.values();
        CombinedStats randomStat = allStats[ThreadLocalRandom.current().nextInt(allStats.length)];
        double randomValue = ThreadLocalRandom.current().nextDouble(5.0, 25.0);
        int randomDuration = ThreadLocalRandom.current().nextInt(180, 600); // 3-10 minutos
        return createPotion(randomStat, randomValue, randomDuration);
    }

    /**
     * Crea una poción aleatoria usando los niveles predefinidos
     */
    public static StatPotion createRandomPotionByLevel() {
        CombinedStats[] allStats = CombinedStats.values();
        PotionLevel[] allLevels = PotionLevel.values();
        PotionDuration[] allDurations = PotionDuration.values();

        CombinedStats randomStat = allStats[ThreadLocalRandom.current().nextInt(allStats.length)];
        PotionLevel randomLevel = allLevels[ThreadLocalRandom.current().nextInt(allLevels.length)];
        PotionDuration randomDuration = allDurations[ThreadLocalRandom.current().nextInt(allDurations.length)];

        return createPotionByLevel(randomStat, randomLevel, randomDuration);
    }

    // ========== MÉTODOS DE ENTREGA A JUGADORES ==========

    /**
     * Da pociones por nivel al jugador
     */
    public static void givePotionsByLevel(Player player, CombinedStats stat, PotionLevel level,
                                          PotionDuration duration, int amount) {
        List<StatPotion> potions = createPotionsByLevel(stat, level, duration, amount);
        for (StatPotion potion : potions) {
            player.getInventory().addItem(potion);
        }
    }

    /**
     * Da un set completo de pociones al jugador
     */
    public static void giveCompleteSetByLevel(Player player, PotionLevel level, PotionDuration duration) {
        List<StatPotion> potions = createCompleteSetByLevel(level, duration);
        for (StatPotion potion : potions) {
            player.getInventory().addItem(potion);
        }
    }

    /**
     * Da pociones directamente al inventario del jugador
     */
    public static void givePotions(Player player, CombinedStats stat, double value, int duration, int amount) {
        List<StatPotion> potions = createPotions(stat, value, duration, amount);
        for (StatPotion potion : potions) {
            player.getInventory().addItem(potion);
        }
    }

    /**
     * Da pociones con valores por defecto al jugador
     */
    public static void givePotions(Player player, CombinedStats stat, int amount) {
        givePotions(player, stat, DEFAULT_VALUE, DEFAULT_DURATION, amount);
    }

    /**
     * Da una poción aleatoria al jugador
     */
    public static void giveRandomPotion(Player player) {
        StatPotion randomPotion = createRandomPotion();
        player.getInventory().addItem(randomPotion);
    }

    /**
     * Da una poción aleatoria por nivel al jugador
     */
    public static void giveRandomPotionByLevel(Player player) {
        StatPotion randomPotion = createRandomPotionByLevel();
        player.getInventory().addItem(randomPotion);
    }

    /**
     * Da múltiples pociones aleatorias al jugador
     */
    public static void giveRandomPotions(Player player, int amount) {
        for (int i = 0; i < amount; i++) {
            giveRandomPotion(player);
        }
    }

    /**
     * Crea un pack de pociones mixtas (diferentes stats)
     */
    public static List<StatPotion> createMixedPotionPack(double value, int duration, int amountPerStat) {
        List<StatPotion> potions = new ArrayList<>();
        CombinedStats[] importantStats = {
                CombinedStats.HEALTH,
                CombinedStats.TOUGHNESS,
                CombinedStats.STRENGTH,
                CombinedStats.DEXTERITY
        };

        for (CombinedStats stat : importantStats) {
            potions.addAll(createPotions(stat, value, duration, amountPerStat));
        }
        return potions;
    }

    /**
     * Da un pack mixto de pociones al jugador
     */
    public static void giveMixedPotionPack(Player player, double value, int duration, int amountPerStat) {
        List<StatPotion> potions = createMixedPotionPack(value, duration, amountPerStat);
        for (StatPotion potion : potions) {
            player.getInventory().addItem(potion);
        }
    }

    // ========== MÉTODOS DE UTILIDAD ==========

    /**
     * Valida si un stat string es válido
     */
    public static boolean isValidStat(String statName) {
        try {
            CombinedStats.valueOf(statName.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Obtiene un CombinedStats desde string con validación
     */
    public static CombinedStats getStatFromString(String statName) throws IllegalArgumentException {
        return CombinedStats.valueOf(statName.toUpperCase());
    }

    /**
     * Obtiene todos los nombres de stats disponibles
     */
    public static String[] getAvailableStatNames() {
        CombinedStats[] stats = CombinedStats.values();
        String[] names = new String[stats.length];
        for (int i = 0; i < stats.length; i++) {
            names[i] = stats[i].name();
        }
        return names;
    }

    /**
     * Formatea la duración en formato legible
     */
    public static String formatDuration(int seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            return (seconds / 60) + "m " + (seconds % 60) + "s";
        } else {
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            return hours + "h " + minutes + "m";
        }
    }

    /**
     * Obtiene información detallada de una poción para mensajes
     */
    public static String getPotionInfo(CombinedStats stat, double value, int duration) {
        return stat.getDisplayName(ItemRarity.AURA_LOCALE) +
                " (+" + value + ") con duración de " + formatDuration(duration);
    }

    /**
     * Obtiene información de una poción por nivel
     */
    public static String getPotionInfoByLevel(CombinedStats stat, PotionLevel level, PotionDuration duration) {
        return getPotionInfo(stat, level.getValue(), duration.getSeconds());
    }
}