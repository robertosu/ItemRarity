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

public class BlessingObject extends ItemStack {
    private static final TextColor PRIMARY_COLOR = TextColor.fromHexString("#12B8E6");
    private static final TextColor LORE_COLOR = TextColor.fromHexString("#7FD9B0");
    private static final Component DISPLAY_NAME = Component.text("Bendición")
            .color(PRIMARY_COLOR)
            .decoration(TextDecoration.ITALIC, false);

    public BlessingObject(int amount, Plugin plugin) {
        super(Material.PAPER, amount);
        ItemMeta meta = this.getItemMeta();

        // Set persistent data
        NamespacedKey key = new NamespacedKey(plugin, ItemConfig.BLESSING_OBJECT_KEY);
        meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);

        // Set display name using Adventure API
        meta.displayName(DISPLAY_NAME);

        meta.setCustomModelData(6002);

        // Set lore using Adventure API
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Arrastralo a un objeto para cambiar su")
                .color(LORE_COLOR).decoration(TextDecoration.ITALIC,false));
        lore.add(Component.text("estadística mas baja sin alterar el resto.")
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

    public static String getBlessingObjectKey() {
        return ItemConfig.BLESSING_OBJECT_KEY;
    }
}
