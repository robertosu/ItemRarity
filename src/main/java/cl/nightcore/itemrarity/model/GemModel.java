package cl.nightcore.itemrarity.model;

import cl.nightcore.itemrarity.ItemRarity;
import dev.aurelium.auraskills.api.stat.Stats;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class GemModel {
    private static final String GEM_STAT_KEY = "gem_stat";
    private static final String GEM_LEVEL_KEY = "gem_level";
    private final ItemStack item;



    public GemModel(ItemStack item) {
        this.item = item;
    }
    public static String getGemStatKey(){
        return GEM_STAT_KEY;
    }

    public boolean isValid() {
        if (item == null || !item.hasItemMeta()) return false;

        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        return container.has(new NamespacedKey(ItemRarity.plugin, GEM_STAT_KEY), PersistentDataType.STRING) &&
                container.has(new NamespacedKey(ItemRarity.plugin, GEM_LEVEL_KEY), PersistentDataType.INTEGER);
    }

    public Stats getStat() {
        if (!isValid()) return null;

        String statName = item.getItemMeta().getPersistentDataContainer()
                .get(new NamespacedKey(ItemRarity.plugin, GEM_STAT_KEY), PersistentDataType.STRING);

        try {
            return Stats.valueOf(statName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public int getLevel() {
        if (!isValid()) return 0;

        return item.getItemMeta().getPersistentDataContainer()
                .getOrDefault(new NamespacedKey(ItemRarity.plugin, GEM_LEVEL_KEY),
                        PersistentDataType.INTEGER, 0);
    }

    public ItemStack getItem() {
        return item;
    }

    public int getValue() {
        int level = getLevel();
        return 4 + (level - 1) * level / 2;
    }
}