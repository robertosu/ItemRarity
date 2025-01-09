package cl.nightcore.itemrarity.classes;

import cl.nightcore.itemrarity.item.Gem;
import dev.aurelium.auraskills.api.stat.Stats;
import dev.aurelium.auraskills.api.item.ModifierType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class GemManager {
    public static Gem createStrengthGem() {
        ItemStack gemItem = new ItemStack(Material.REDSTONE);
        return new Gem(gemItem,
                Stats.STRENGTH,
                5,
                ModifierType.ITEM,
                Component.text("Fuerza").color(NamedTextColor.RED));
    }

    public static Gem createToughnessGem() {
        ItemStack gemItem = new ItemStack(Material.DIAMOND);
        return new Gem(gemItem,
                Stats.TOUGHNESS,
                5,
                ModifierType.ARMOR,
                Component.text("Resistencia").color(NamedTextColor.AQUA));
    }
}