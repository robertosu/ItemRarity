package cl.nightcore.itemrarity.item.roller;

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
import java.util.Objects;

public class RedemptionObject extends ItemStack {
    private static final TextColor PRIMARY_COLOR = TextColor.fromHexString("#FD645B");
    private static final TextColor LORE_COLOR = TextColor.fromHexString("#F28E89");
    private static final Component DISPLAY_NAME =
            Component.text("Redención").color(PRIMARY_COLOR).decoration(TextDecoration.ITALIC, false);

    public RedemptionObject(int amount, Plugin plugin) {
        super(Material.PAPER, amount);
        ItemMeta meta = this.getItemMeta();
        Objects.requireNonNull(meta);
        // Set persistent data
        NamespacedKey key = new NamespacedKey(plugin, ItemConfig.REDEMPTION_OBJECT_KEY);
        meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
        // Set display name using Adventure API
        meta.displayName(DISPLAY_NAME);
        // Set Nexo model
        // NamespacedKey itemModel = new NamespacedKey(NexoPlugin.instance(), "redemption_object");
        meta.setCustomModelData(6003);
        // meta.setItemModel(itemModel);
        // Set lore using Adventure API
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Arrastralo a un objeto para cambiar las")
                .color(LORE_COLOR)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("estadísticas, pero conservando la mas alta.")
                .color(LORE_COLOR)
                .decoration(TextDecoration.ITALIC, false));
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

    public static String getRedeemObjectKey() {
        return ItemConfig.REDEMPTION_OBJECT_KEY;
    }
}