package cl.nightcore.itemrarity.item;

import cl.nightcore.itemrarity.config.ItemConfig;
import net.kyori.adventure.text.Component;
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

import static cl.nightcore.itemrarity.config.ItemConfig.SOCKET_GEM_KEY_NS;

public class SocketStone extends ItemStack {
    private static final Component DISPLAY_NAME = Component.text("Cristal Perforador").color(TextColor.color(0xA3D2FF)).decoration(TextDecoration.ITALIC, false);
    private static final TextColor LORE_COLOR = TextColor.color(0xD7FDFF);

    public SocketStone(int amount, Plugin plugin) {
        super(Material.PAPER, amount);
        ItemMeta meta = this.getItemMeta();

        // Set persistent data
        meta.getPersistentDataContainer().set(SOCKET_GEM_KEY_NS, PersistentDataType.BOOLEAN, true);

        // Set display name using Adventure API
        meta.displayName(DISPLAY_NAME);

        // Set Oraxen model
        meta.setCustomModelData(6004);
        // Set lore using Adventure API
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Arrastra este item a tu objeto para").color(LORE_COLOR).decoration(TextDecoration.ITALIC,false));
        lore.add(Component.text("agregar una nueva ranura de gemas.").color(LORE_COLOR).decoration(TextDecoration.ITALIC,false));
        meta.lore(lore);

        // Set glint effect
        meta.setEnchantmentGlintOverride(true);
        this.setItemMeta(meta);
    }

    public static NamespacedKey getSocketGemKeyNs() {
        return ItemConfig.SOCKET_GEM_KEY_NS;
    }

    public static TextColor getLoreColor() {
        return LORE_COLOR;
    }










}
