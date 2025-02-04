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

@SuppressWarnings("unused")
public class BlessingBall extends ItemStack {
    private static final TextColor PRIMARY_COLOR = TextColor.fromHexString("#22E656");
    private static final TextColor LORE_COLOR = TextColor.fromHexString("#519578");
    private static final Component DISPLAY_NAME = Component.text("Bola Bendición")
            .color(PRIMARY_COLOR)
            .decoration(TextDecoration.ITALIC, false);

    public BlessingBall(int amount, Plugin plugin) {
        super(Material.PAPER, amount);
        ItemMeta meta = this.getItemMeta();

        // Set persistent data
        NamespacedKey key = new NamespacedKey(plugin, ItemConfig.BLESSING_BALL_KEY);
        meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);

        // Set display name using Adventure API
        meta.displayName(DISPLAY_NAME);

        meta.setCustomModelData(6006); // Cambia este valor según el modelo que desees

        // Set lore using Adventure API
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Arrastra esta bola a un objeto identificado")
                .color(LORE_COLOR).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("para añadir una nueva bonificación.")
                .color(LORE_COLOR).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Máximo de 6 bonificaciones.")
                .color(LORE_COLOR).decoration(TextDecoration.ITALIC, false));
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

    public static String getBlessingBallKey() {
        return ItemConfig.BLESSING_BALL_KEY;
    }
}