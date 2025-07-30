package cl.nightcore.itemrarity.model;

import cl.nightcore.itemrarity.item.ExperienceMultiplier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public record ExperienceMultiplierModel(ItemStack item) {

    public int getMultiplier() {
        return item.getItemMeta()
                .getPersistentDataContainer()
                .getOrDefault(ExperienceMultiplier.XP_MULTIPLIER_KEY_NS, PersistentDataType.INTEGER, 0);
    }
}