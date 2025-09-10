package cl.nightcore.itemrarity.item.potion;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.config.CombinedStats;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PotionManager {

    /**
     * Crea una poción con nivel y duración específicos usando enums
     */
    public static StatPotion createPotion(CombinedStats stat, PotionLevel level, PotionDuration duration) {
        return new StatPotion(stat, level, duration);
    }

    /**
     * Crea una poción con valores específicos
     */
    public static StatPotion createPotionWithValues(CombinedStats stat, double value, int durationSeconds) {
        return new StatPotion(stat, value, durationSeconds);
    }


    public static List<StatPotion> createPotionsWithValues(CombinedStats stat, double value, int durationSeconds, int amount) {
        List<StatPotion> potions = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            potions.add(createPotionWithValues(stat, value, durationSeconds));
        }
        return potions;
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
     * Obtiene información detallada de una poción usando valores directos para mensajes
     */
    public static String getPotionInfoWithValues(CombinedStats stat, double value, int duration) {
        return stat.getDisplayName(ItemRarity.AURA_LOCALE) +
                " (+" + value + ") con duración de " + formatDuration(duration);
    }

    // Enum para niveles de pociones con valores fijos
    public enum PotionLevel {
        ONE(5, "1"),
        TWO(10, "2"),
        THREE(15, "3"),
        FOUR(20, "4"),
        FIVE(25, "5");

        private final int value;
        private final String name;

        PotionLevel(int value, String name) {
            this.value = value;
            this.name = name;
        }

        public int getValue() {
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
}