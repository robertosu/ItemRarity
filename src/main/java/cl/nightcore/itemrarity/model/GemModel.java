package cl.nightcore.itemrarity.model;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.statprovider.StatProvider;
import dev.aurelium.auraskills.api.stat.Stats;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public record GemModel(ItemStack item) {
    private static final String GEM_STAT_KEY = "gem_stat";
    private static final String GEM_LEVEL_KEY = "gem_level";

    public static String getGemStatKey() {
        return GEM_STAT_KEY;
    }

    public Stats getStat() {

        String statName = item.getItemMeta()
                .getPersistentDataContainer()
                .get(new NamespacedKey(ItemRarity.plugin, GEM_STAT_KEY), PersistentDataType.STRING);

        try {
            return Stats.valueOf(statName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public int getLevel() {
        return item.getItemMeta()
                .getPersistentDataContainer()
                .getOrDefault(new NamespacedKey(ItemRarity.plugin, GEM_LEVEL_KEY), PersistentDataType.INTEGER, 0);
    }

    public int getValue() {
        int level = getLevel();
        return 4 + (level - 1) * level / 2;
    }

    public Boolean isCompatible(StatProvider statProvider) {
        return statProvider.getAvailableStats().contains(this.getStat());
    }
}