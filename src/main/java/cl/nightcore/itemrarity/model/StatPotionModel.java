package cl.nightcore.itemrarity.model;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.config.CombinedStats;
import cl.nightcore.itemrarity.item.potion.PotionManager;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public record StatPotionModel(ItemStack item) {

    // Keys para persistent data
    private static final NamespacedKey POTION_STAT = new NamespacedKey(ItemRarity.PLUGIN, "potion_stat");
    private static final NamespacedKey POTION_LEVEL = new NamespacedKey(ItemRarity.PLUGIN, "potion_level");
    private static final NamespacedKey POTION_DURATION = new NamespacedKey(ItemRarity.PLUGIN, "potion_duration");
    private static final NamespacedKey IS_STAT_POTION = new NamespacedKey(ItemRarity.PLUGIN, "is_stat_potion");
    
    // Keys legacy para compatibilidad
    private static final NamespacedKey POTION_VALUE = new NamespacedKey(ItemRarity.PLUGIN, "potion_value");
    private static final NamespacedKey POTION_DURATION_SECONDS = new NamespacedKey(ItemRarity.PLUGIN, "potion_duration_seconds");

    /**
     * Verifica si el ItemStack es una poción de stats
     */
    public boolean isStatPotion() {
        if (item == null || item.getItemMeta() == null) {
            return false;
        }
        
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        return container.has(IS_STAT_POTION, PersistentDataType.BOOLEAN);
    }

    /**
     * Obtiene el stat de la poción
     */
    public CombinedStats getStat() {
        if (!isStatPotion()) {
            return null;
        }

        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        String statName = container.get(POTION_STAT, PersistentDataType.STRING);
        
        if (statName == null) {
            return null;
        }

        try {
            return CombinedStats.valueOf(statName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Obtiene el nivel de la poción (nuevo formato)
     */
    public PotionManager.PotionLevel getLevel() {
        if (!isStatPotion()) {
            return null;
        }

        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        String levelName = container.get(POTION_LEVEL, PersistentDataType.STRING);
        
        if (levelName == null) {
            return null;
        }

        try {
            return PotionManager.PotionLevel.valueOf(levelName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Obtiene la duración de la poción (nuevo formato)
     */
    public PotionManager.PotionDuration getDuration() {
        if (!isStatPotion()) {
            return null;
        }

        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        String durationName = container.get(POTION_DURATION, PersistentDataType.STRING);
        
        if (durationName == null) {
            return null;
        }

        try {
            return PotionManager.PotionDuration.valueOf(durationName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Obtiene el valor de la poción (compatible con formato legacy y nuevo)
     */
    public double getValue() {
        if (!isStatPotion()) {
            return 0.0;
        }

        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        
        // Intentar obtener del formato nuevo primero
        PotionManager.PotionLevel level = getLevel();
        if (level != null) {
            return level.getValue();
        }

        // Fallback al formato legacy
        Double legacyValue = container.get(POTION_VALUE, PersistentDataType.DOUBLE);
        return legacyValue != null ? legacyValue : 0.0;
    }

    /**
     * Obtiene la duración en segundos (compatible con formato legacy y nuevo)
     */
    public int getDurationSeconds() {
        if (!isStatPotion()) {
            return 0;
        }

        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        
        // Intentar obtener del formato nuevo primero
        PotionManager.PotionDuration duration = getDuration();
        if (duration != null) {
            return duration.getSeconds();
        }

        // Fallback al formato legacy
        Integer legacyDuration = container.get(POTION_DURATION_SECONDS, PersistentDataType.INTEGER);
        return legacyDuration != null ? legacyDuration : 300; // Default 5 minutos
    }

    /**
     * Verifica si la poción usa el formato legacy
     */
    public boolean isLegacyFormat() {
        if (!isStatPotion()) {
            return false;
        }

        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        return getLevel() == null && getDuration() == null && 
               container.has(POTION_VALUE, PersistentDataType.DOUBLE);
    }

    /**
     * Obtiene la duración formateada como string
     */
    public String getFormattedDuration() {
        PotionManager.PotionDuration duration = getDuration();
        if (duration != null) {
            return duration.getFormattedTime();
        }
        
        // Formatear duración manual para formato legacy
        int seconds = getDurationSeconds();
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%d:%02d", minutes, remainingSeconds);
    }

    /**
     * Método estático de conveniencia para verificar si un item es poción
     */
    public static boolean isStatPotion(ItemStack item) {
        return new StatPotionModel(item).isStatPotion();
    }

    /**
     * Método estático para crear un modelo desde un ItemStack
     */
    public static StatPotionModel of(ItemStack item) {
        return new StatPotionModel(item);
    }
}