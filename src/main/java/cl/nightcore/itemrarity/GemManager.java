package cl.nightcore.itemrarity;

import cl.nightcore.itemrarity.item.GemObject;
import cl.nightcore.itemrarity.util.ItemUtil;
import dev.aurelium.auraskills.api.trait.Traits;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class GemManager {
    public GemObject createStrengthGem(int amount, int level, String trait) {
        ItemStack gemItem = new ItemStack(Material.PAPER);
        return new GemObject(
                gemItem,
                Traits.valueOf(trait),
                Component.text("Fuerza").color(TextColor.fromHexString(ItemUtil.getColorOfTrait(Traits.valueOf(trait)))),
                level, 1023, amount);
    }
}
