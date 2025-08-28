package cl.nightcore.itemrarity.item.potion;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.config.CombinedStats;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PotionManager {

    // Enum para niveles de pociones con valores fijos
    public enum PotionLevel {
        ONE(5, "1"),
        TWO(10, "2"),
        THREE(15, "3"),
        FOUR(20, "4"),
        FIVE(25, "5");

        private final double value;
        private final String name;

        PotionLevel(double value, String name) {
            this.value = value;
            this.name = name;
        }

        public double getValue() {
            return value;
        }

        public String getName() {
            return name;
        }
    }

    // Enum para duraciones con valores fijos
    public enum PotionDuration {
        LOW(300, "Efímero"),    // 5 min
        MEDIUM(600, "Intermedio"), // 10 min
        HIGH(900, "Duradero"),  // 15 min
        HIGHEST(1800, "Muy Duradero"); // 30 min

        private final int seconds;
        private final String name;

        PotionDuration(int seconds, String name) {
            this.seconds = seconds;
            this.name = name;
        }

        public int getSeconds() {
            return seconds;
        }

        public String getName() {
            return name;
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

    // ========== MÉTODOS USANDO ENUMS ==========

    /**
     * Crea una poción con nivel y duración específicos usando enums
     */
    public static StatPotion createPotion(CombinedStats stat, PotionLevel level, PotionDuration duration) {
        return new StatPotion(stat, level, duration);
    }

    /**
     * Crea una poción con solo nivel específico (duración media por defecto)
     */
    public static StatPotion createPotion(CombinedStats stat, PotionLevel level) {
        return createPotion(stat, level, PotionDuration.MEDIUM);
    }

    /**
     * Crea una poción con duración específica (nivel 1 por defecto)
     */
    public static StatPotion createPotion(CombinedStats stat, PotionDuration duration) {
        return createPotion(stat, PotionLevel.ONE, duration);
    }

    /**
     * Crea una poción con valores por defecto (nivel 1, duración media)
     */
    public static StatPotion createPotion(CombinedStats stat) {
        return createPotion(stat, PotionLevel.ONE, PotionDuration.MEDIUM);
    }

    /**
     * Crea múltiples pociones del mismo tipo usando enums
     */
    public static List<StatPotion> createPotions(CombinedStats stat, PotionLevel level,
                                                 PotionDuration duration, int amount) {
        List<StatPotion> potions = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            potions.add(createPotion(stat, level, duration));
        }
        return potions;
    }

    /**
     * Crea múltiples pociones con valores por defecto
     */
    public static List<StatPotion> createPotions(CombinedStats stat, int amount) {
        return createPotions(stat, PotionLevel.ONE, PotionDuration.MEDIUM, amount);
    }

    /**
     * Crea un set completo de pociones para todos los stats con el mismo nivel y duración
     */
    public static List<StatPotion> createCompleteSet(PotionLevel level, PotionDuration duration) {
        List<StatPotion> potions = new ArrayList<>();
        for (CombinedStats stat : CombinedStats.values()) {
            potions.add(createPotion(stat, level, duration));
        }
        return potions;
    }

    /**
     * Crea un set de pociones para stats específicos con el mismo nivel y duración
     */
    public static List<StatPotion> createStatSet(CombinedStats[] stats, PotionLevel level,
                                                 PotionDuration duration) {
        List<StatPotion> potions = new ArrayList<>();
        for (CombinedStats stat : stats) {
            potions.add(createPotion(stat, level, duration));
        }
        return potions;
    }

    /**
     * Crea pociones escalonadas (diferentes niveles para el mismo stat)
     */
    public static List<StatPotion> createTieredPotions(CombinedStats stat, PotionDuration duration) {
        List<StatPotion> potions = new ArrayList<>();
        for (PotionLevel level : PotionLevel.values()) {
            potions.add(createPotion(stat, level, duration));
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
                potions.add(createPotion(stat, level, duration));
            }
        }
        return potions;
    }

    /**
     * Crea una poción completamente aleatoria usando enums
     */
    public static StatPotion createRandomPotion() {
        CombinedStats[] allStats = CombinedStats.values();
        PotionLevel[] allLevels = PotionLevel.values();
        PotionDuration[] allDurations = PotionDuration.values();

        CombinedStats randomStat = allStats[ThreadLocalRandom.current().nextInt(allStats.length)];
        PotionLevel randomLevel = allLevels[ThreadLocalRandom.current().nextInt(allLevels.length)];
        PotionDuration randomDuration = allDurations[ThreadLocalRandom.current().nextInt(allDurations.length)];

        return createPotion(randomStat, randomLevel, randomDuration);
    }

    /**
     * Crea una poción aleatoria con nivel específico
     */
    public static StatPotion createRandomPotion(PotionLevel level) {
        CombinedStats[] allStats = CombinedStats.values();
        PotionDuration[] allDurations = PotionDuration.values();

        CombinedStats randomStat = allStats[ThreadLocalRandom.current().nextInt(allStats.length)];
        PotionDuration randomDuration = allDurations[ThreadLocalRandom.current().nextInt(allDurations.length)];

        return createPotion(randomStat, level, randomDuration);
    }

    /**
     * Crea una poción aleatoria con duración específica
     */
    public static StatPotion createRandomPotion(PotionDuration duration) {
        CombinedStats[] allStats = CombinedStats.values();
        PotionLevel[] allLevels = PotionLevel.values();

        CombinedStats randomStat = allStats[ThreadLocalRandom.current().nextInt(allStats.length)];
        PotionLevel randomLevel = allLevels[ThreadLocalRandom.current().nextInt(allLevels.length)];

        return createPotion(randomStat, randomLevel, duration);
    }

    /**
     * Crea múltiples pociones aleatorias
     */
    public static List<StatPotion> createRandomPotions(int amount) {
        List<StatPotion> potions = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            potions.add(createRandomPotion());
        }
        return potions;
    }

    // ========== MÉTODOS USANDO VALORES DIRECTOS ==========

    /**
     * Crea una poción con valores específicos
     */
    public static StatPotion createPotionWithValues(CombinedStats stat, double value, int durationSeconds) {
        return new StatPotion(stat, value, durationSeconds);
    }

    /**
     * Crea una poción con valor específico y duración por defecto
     */
    public static StatPotion createPotionWithValue(CombinedStats stat, double value) {
        return new StatPotion(stat, value, 300); // 5 minutos por defecto
    }

    /**
     * Crea múltiples pociones con valores específicos
     */
    public static List<StatPotion> createPotionsWithValues(CombinedStats stat, double value, int durationSeconds, int amount) {
        List<StatPotion> potions = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            potions.add(createPotionWithValues(stat, value, durationSeconds));
        }
        return potions;
    }

    /**
     * Crea una poción con valores aleatorios dentro de rangos específicos
     */
    public static StatPotion createRandomPotionWithValues(CombinedStats stat, double minValue, double maxValue,
                                                          int minDuration, int maxDuration) {
        double randomValue = ThreadLocalRandom.current().nextDouble(minValue, maxValue);
        int randomDuration = ThreadLocalRandom.current().nextInt(minDuration, maxDuration + 1);
        return createPotionWithValues(stat, randomValue, randomDuration);
    }

    /**
     * Crea una poción completamente aleatoria con valores numéricos
     */
    public static StatPotion createRandomPotionWithValues() {
        CombinedStats[] allStats = CombinedStats.values();
        CombinedStats randomStat = allStats[ThreadLocalRandom.current().nextInt(allStats.length)];
        double randomValue = ThreadLocalRandom.current().nextDouble(5.0, 25.0);
        int randomDuration = ThreadLocalRandom.current().nextInt(180, 1800); // 3-30 minutos
        return createPotionWithValues(randomStat, randomValue, randomDuration);
    }

    // ========== MÉTODOS LEGACY (para compatibilidad) ==========

    /**
     * @deprecated Use createPotionWithValues(CombinedStats, double, int) instead
     */
    @Deprecated
    public static StatPotion createPotionLegacy(CombinedStats stat, double value, int durationSeconds) {
        return createPotionWithValues(stat, value, durationSeconds);
    }

    /**
     * @deprecated Use createPotionsWithValues(CombinedStats, double, int, int) instead
     */
    @Deprecated
    public static List<StatPotion> createPotionsLegacy(CombinedStats stat, double value, int duration, int amount) {
        return createPotionsWithValues(stat, value, duration, amount);
    }

    // ========== MÉTODOS DE ENTREGA A JUGADORES ==========

    /**
     * Da pociones al jugador usando enums
     */
    public static void givePotions(Player player, CombinedStats stat, PotionLevel level,
                                   PotionDuration duration, int amount) {
        List<StatPotion> potions = createPotions(stat, level, duration, amount);
        for (StatPotion potion : potions) {
            player.getInventory().addItem(potion);
        }
    }

    /**
     * Da pociones al jugador usando valores directos
     */
    public static void givePotionsWithValues(Player player, CombinedStats stat, double value,
                                             int durationSeconds, int amount) {
        List<StatPotion> potions = createPotionsWithValues(stat, value, durationSeconds, amount);
        for (StatPotion potion : potions) {
            player.getInventory().addItem(potion);
        }
    }

    /**
     * Da una sola poción al jugador usando enums
     */
    public static void givePotion(Player player, CombinedStats stat, PotionLevel level, PotionDuration duration) {
        StatPotion potion = createPotion(stat, level, duration);
        player.getInventory().addItem(potion);
    }

    /**
     * Da una sola poción al jugador usando valores directos
     */
    public static void givePotionWithValues(Player player, CombinedStats stat, double value, int durationSeconds) {
        StatPotion potion = createPotionWithValues(stat, value, durationSeconds);
        player.getInventory().addItem(potion);
    }

    /**
     * Da un set completo de pociones al jugador usando enums
     */
    public static void giveCompleteSet(Player player, PotionLevel level, PotionDuration duration) {
        List<StatPotion> potions = createCompleteSet(level, duration);
        for (StatPotion potion : potions) {
            player.getInventory().addItem(potion);
        }
    }

    /**
     * Da un set completo de pociones al jugador usando valores directos
     */
    public static void giveCompleteSetWithValues(Player player, double value, int durationSeconds) {
        for (CombinedStats stat : CombinedStats.values()) {
            givePotionWithValues(player, stat, value, durationSeconds);
        }
    }

    /**
     * Da una poción aleatoria al jugador (usando enums)
     */
    public static void giveRandomPotion(Player player) {
        StatPotion randomPotion = createRandomPotion();
        player.getInventory().addItem(randomPotion);
    }

    /**
     * Da una poción aleatoria al jugador (usando valores directos)
     */
    public static void giveRandomPotionWithValues(Player player) {
        StatPotion randomPotion = createRandomPotionWithValues();
        player.getInventory().addItem(randomPotion);
    }

    /**
     * Da múltiples pociones aleatorias al jugador (usando enums)
     */
    public static void giveRandomPotions(Player player, int amount) {
        List<StatPotion> potions = createRandomPotions(amount);
        for (StatPotion potion : potions) {
            player.getInventory().addItem(potion);
        }
    }

    /**
     * Crea un pack de pociones mixtas usando enums
     */
    public static List<StatPotion> createMixedPotionPack(PotionLevel level, PotionDuration duration, int amountPerStat) {
        List<StatPotion> potions = new ArrayList<>();
        CombinedStats[] importantStats = {
                CombinedStats.HEALTH,
                CombinedStats.TOUGHNESS,
                CombinedStats.STRENGTH,
                CombinedStats.DEXTERITY
        };

        for (CombinedStats stat : importantStats) {
            potions.addAll(createPotions(stat, level, duration, amountPerStat));
        }
        return potions;
    }

    /**
     * Crea un pack de pociones mixtas usando valores directos
     */
    public static List<StatPotion> createMixedPotionPackWithValues(double value, int durationSeconds, int amountPerStat) {
        List<StatPotion> potions = new ArrayList<>();
        CombinedStats[] importantStats = {
                CombinedStats.HEALTH,
                CombinedStats.TOUGHNESS,
                CombinedStats.STRENGTH,
                CombinedStats.DEXTERITY
        };

        for (CombinedStats stat : importantStats) {
            potions.addAll(createPotionsWithValues(stat, value, durationSeconds, amountPerStat));
        }
        return potions;
    }

    /**
     * Da un pack mixto de pociones al jugador usando enums
     */
    public static void giveMixedPotionPack(Player player, PotionLevel level, PotionDuration duration, int amountPerStat) {
        List<StatPotion> potions = createMixedPotionPack(level, duration, amountPerStat);
        for (StatPotion potion : potions) {
            player.getInventory().addItem(potion);
        }
    }

    /**
     * Da un pack mixto de pociones al jugador usando valores directos
     */
    public static void giveMixedPotionPackWithValues(Player player, double value, int durationSeconds, int amountPerStat) {
        List<StatPotion> potions = createMixedPotionPackWithValues(value, durationSeconds, amountPerStat);
        for (StatPotion potion : potions) {
            player.getInventory().addItem(potion);
        }
    }

    /**
     * Crea un kit de inicio con pociones básicas
     */
    public static List<StatPotion> createStarterKit() {
        return createMixedPotionPack(PotionLevel.ONE, PotionDuration.LOW, 2);
    }

    /**
     * Crea un kit intermedio con pociones mejoradas
     */
    public static List<StatPotion> createIntermediateKit() {
        return createMixedPotionPack(PotionLevel.THREE, PotionDuration.MEDIUM, 3);
    }

    /**
     * Crea un kit avanzado con pociones de alto nivel
     */
    public static List<StatPotion> createAdvancedKit() {
        return createMixedPotionPack(PotionLevel.FIVE, PotionDuration.HIGHEST, 2);
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
     * Valida si un nivel string es válido
     */
    public static boolean isValidLevel(String levelName) {
        try {
            PotionLevel.valueOf(levelName.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Valida si una duración string es válida
     */
    public static boolean isValidDuration(String durationName) {
        try {
            PotionDuration.valueOf(durationName.toUpperCase());
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
     * Obtiene un PotionLevel desde string con validación
     */
    public static PotionLevel getLevelFromString(String levelName) throws IllegalArgumentException {
        return PotionLevel.valueOf(levelName.toUpperCase());
    }

    /**
     * Obtiene un PotionDuration desde string con validación
     */
    public static PotionDuration getDurationFromString(String durationName) throws IllegalArgumentException {
        return PotionDuration.valueOf(durationName.toUpperCase());
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
     * Obtiene todos los nombres de niveles disponibles
     */
    public static String[] getAvailableLevelNames() {
        PotionLevel[] levels = PotionLevel.values();
        String[] names = new String[levels.length];
        for (int i = 0; i < levels.length; i++) {
            names[i] = levels[i].name();
        }
        return names;
    }

    /**
     * Obtiene todos los nombres de duraciones disponibles
     */
    public static String[] getAvailableDurationNames() {
        PotionDuration[] durations = PotionDuration.values();
        String[] names = new String[durations.length];
        for (int i = 0; i < durations.length; i++) {
            names[i] = durations[i].name();
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
     * Obtiene información detallada de una poción usando enums para mensajes
     */
    public static String getPotionInfo(CombinedStats stat, PotionLevel level, PotionDuration duration) {
        return stat.getDisplayName(ItemRarity.AURA_LOCALE) +
                " (+" + level.getValue() + ") con duración de " + duration.getFormattedTime();
    }

    /**
     * Obtiene información detallada de una poción usando valores directos para mensajes
     */
    public static String getPotionInfoWithValues(CombinedStats stat, double value, int duration) {
        return stat.getDisplayName(ItemRarity.AURA_LOCALE) +
                " (+" + value + ") con duración de " + formatDuration(duration);
    }
}