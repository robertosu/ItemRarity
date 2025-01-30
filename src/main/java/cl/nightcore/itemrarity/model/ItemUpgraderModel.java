package cl.nightcore.itemrarity.model;

import cl.nightcore.itemrarity.item.ItemUpgrader;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import static cl.nightcore.itemrarity.config.ItemConfig.ITEM_UPGRADER_KEY_NS;
import static cl.nightcore.itemrarity.item.ItemUpgrader.ITEM_UPGRADER_TYPE_KEY_NS;

public record ItemUpgraderModel(ItemStack item) {



    public static TextColor getPrimaryColor(int type){
        switch (type){
            case 1 -> {return ItemUpgrader.UNSTABLE_COLOR;}
            case 2 -> {return ItemUpgrader.ACTIVE_COLOR;}
            case 3 -> {return ItemUpgrader.STABLE_COLOR;}
            default -> {
                return null;
            }
        }
    }

    public int getLevel() {
        return item.getItemMeta()
                .getPersistentDataContainer()
                .getOrDefault(ITEM_UPGRADER_KEY_NS, PersistentDataType.INTEGER, 0);
    }

    public int getType() {
        return item.getItemMeta()
                .getPersistentDataContainer()
                .getOrDefault(ITEM_UPGRADER_TYPE_KEY_NS, PersistentDataType.INTEGER, 0);
    }

    public int getPercentage(){
        switch (this.getLevel()) {
            case 1 -> { return 65; }
            case 2 -> { return 85; }
            // Add more cases for other numbers
            default -> { return 0;}
        }
    }
}