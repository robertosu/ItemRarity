package cl.nightcore.itemrarity.model;

import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import static cl.nightcore.itemrarity.config.ItemConfig.GEM_REMOVER_KEY_NS;
import static cl.nightcore.itemrarity.config.ItemConfig.ITEM_UPGRADER_KEY_NS;

public record GemRemoverModel(ItemStack item) {


    public int getLevel() {
        return item.getItemMeta()
                .getPersistentDataContainer()
                .getOrDefault(GEM_REMOVER_KEY_NS, PersistentDataType.INTEGER, 0);
    }

    public int getPercentage(){
        switch (this.getLevel()) {
            case 1 -> { return 25; }
            case 2 -> { return 50; }
            case 3 -> { return 75; }
            // Add more cases for other numbers
            default -> { return 25;}
        }
    }
}