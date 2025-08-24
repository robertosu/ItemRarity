package cl.nightcore.itemrarity.item.roller;


import cl.nightcore.itemrarity.config.ItemConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class IdentifyScroll extends ItemStack {
    private static final Component DISPLAY_NAME = Component.text("Pergamino de identificación").color(NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false);
    private static final NamedTextColor LORE_COLOR = NamedTextColor.GRAY;

    public IdentifyScroll(int amount, Plugin plugin) {
        super(Material.PAPER, amount);
        ItemMeta meta = this.getItemMeta();

        // Set persistent data
        NamespacedKey key = new NamespacedKey(plugin, ItemConfig.IDENTIFY_SCROLL_KEY);
        meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);

        // Set display name using Adventure API
        meta.displayName(DISPLAY_NAME);

        // Set Oraxen model
        meta.setCustomModelData(6000);
        // Set lore using Adventure API
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Arrastra este item a tu equipamiento para").color(LORE_COLOR).decoration(TextDecoration.ITALIC,false));
        lore.add(Component.text("desbloquear sus estadísticas ocultas.").color(LORE_COLOR).decoration(TextDecoration.ITALIC,false));
        meta.lore(lore);

        // Set glint effect
        meta.setEnchantmentGlintOverride(true);

        this.setItemMeta(meta);
    }

    public static String getIdentifyScrollKey() {
        return ItemConfig.IDENTIFY_SCROLL_KEY;
    }

    public static NamedTextColor getLoreColor() {
        return LORE_COLOR;
    }
}