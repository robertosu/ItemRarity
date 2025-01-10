package cl.nightcore.itemrarity.item;

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

public class MagicObject extends ItemStack {
    private static final String MAGIC_OBJECT_KEY = "MagicObject";
    private static final TextColor PRIMARY_COLOR = TextColor.fromHexString("#6E6FF8");
    private static final TextColor LORE_COLOR = TextColor.fromHexString("#CA9CDE");
    private static final Component DISPLAY_NAME = Component.text("Objeto Mágico")
            .color(PRIMARY_COLOR)
            .decoration(TextDecoration.ITALIC, false);

    public MagicObject(int amount, Plugin plugin) {
        super(Material.PAPER, amount);
        ItemMeta meta = this.getItemMeta();

        // Set persistent data
        NamespacedKey key = new NamespacedKey(plugin, MAGIC_OBJECT_KEY);
        meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);

        // Set display name using Adventure API
        meta.displayName(DISPLAY_NAME);

        // Set Oraxen model
        /*NamespacedKey itemModel = new NamespacedKey(NexoPlugin.instance(), "enchanted_object");
        meta.setItemModel(itemModel);*/

        meta.setCustomModelData(6001);
        // Set lore using Adventure API
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Elimina los bonos de uno de tus objetos")
                .color(LORE_COLOR).decoration(TextDecoration.ITALIC,false));
        lore.add(Component.text("y añade nuevos. Aumenta la magia de tu objeto")
                .color(LORE_COLOR).decoration(TextDecoration.ITALIC,false));
        meta.lore(lore);

        // Set glint effect
        meta.setEnchantmentGlintOverride(true);

        this.setItemMeta(meta);
    }

    public static TextColor getPrimaryColor() {
        return PRIMARY_COLOR;
    }

    public static TextColor getLoreColor() {
        return LORE_COLOR;
    }

    public static String getMagicObjectKey() {
        return MAGIC_OBJECT_KEY;
    }
}