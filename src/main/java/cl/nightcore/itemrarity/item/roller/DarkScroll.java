package cl.nightcore.itemrarity.item.roller;


import cl.nightcore.itemrarity.config.ItemConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

import static cl.nightcore.itemrarity.config.ItemConfig.DARKSCROLL_KEY_NS;

public class DarkScroll extends ItemStack {
    private static final Component DISPLAY_NAME = Component.text("Pergamino prohibido").color(TextColor.color(0x4227A8)).decoration(TextDecoration.ITALIC, false);
    private static final TextColor LORE_COLOR = TextColor.color(0xAE89F4);
    private static final TextColor PRIMARY_COLOR = TextColor.color(0x4227A8);

    public DarkScroll(int amount, Plugin plugin) {
        super(Material.PAPER, amount);
        ItemMeta meta = this.getItemMeta();

        meta.getPersistentDataContainer().set(DARKSCROLL_KEY_NS, PersistentDataType.BOOLEAN, true);

        // Set display name using Adventure API
        meta.displayName(DISPLAY_NAME);

        // Set Oraxen model
        meta.setCustomModelData(6066);
        // Set lore using Adventure API
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Arrastra este item a tu objeto para").color(LORE_COLOR).decoration(TextDecoration.ITALIC,false));
        lore.add(Component.text("desbloquear nuevos bonos ocultos (6 y 7).").color(LORE_COLOR).decoration(TextDecoration.ITALIC,false));
        meta.lore(lore);

        // Set glint effect
        meta.setEnchantmentGlintOverride(true);

        this.setItemMeta(meta);
    }

    public static String getDarkScrollKey() {
        return ItemConfig.IDENTIFY_SCROLL_KEY;
    }

    public static TextColor getLoreColor() {
        return LORE_COLOR;
    }
    public static TextColor getPrimaryColor() {
        return PRIMARY_COLOR;
    }
}