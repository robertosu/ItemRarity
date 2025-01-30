package cl.nightcore.itemrarity.model;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.config.CombinedStats;
import cl.nightcore.itemrarity.config.ItemConfig;
import cl.nightcore.itemrarity.statprovider.StatProvider;
import dev.aurelium.auraskills.api.stat.Stat;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public record GemModel(ItemStack item) {

    public static NamespacedKey getGemStatKeyNs(){
        return ItemConfig.GEM_STAT_KEY_NS;
    }

    public Stat getStat() {

        String statName = item.getItemMeta()
                .getPersistentDataContainer()
                .get(new NamespacedKey(ItemRarity.PLUGIN, ItemConfig.GEM_STAT_KEY), PersistentDataType.STRING);

        try {
            return CombinedStats.valueOf(statName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public int getLevel() {
        return item.getItemMeta()
                .getPersistentDataContainer()
                .getOrDefault(new NamespacedKey(ItemRarity.PLUGIN, ItemConfig.GEM_LEVEL_KEY), PersistentDataType.INTEGER, 0);
    }

    public int getValue() {
        int level = getLevel();
        return 4 + (level - 1) * level / 2;
    }

    public Boolean isCompatible(StatProvider statProvider) {
        System.out.println(this.getStat());
        System.out.println(this.getStat().toString());
        return statProvider.getAvailableStats().contains(this.getStat());
    }
}