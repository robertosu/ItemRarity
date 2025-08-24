package cl.nightcore.itemrarity.loot.chest;

import org.bukkit.inventory.ItemStack;

public record ChestDropConfig(ItemStack item, double chance, int minAmount, int maxAmount) {
    @Override
    public double chance() {
        return chance;
    }

    @Override
    public int minAmount() {
        return minAmount;
    }

    @Override
    public int maxAmount() {
        return maxAmount;
    }

    @Override
    public ItemStack item() {
        return item;
    }
}