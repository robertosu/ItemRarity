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

public class GemRemover extends ItemStack {
    private static final TextColor PRIMARY_COLOR = TextColor.fromHexString("#12B8E6");
    private static final TextColor LORE_COLOR = TextColor.fromHexString("#7FD9B0");
    private static final Component DISPLAY_NAME = Component.text("Removedor de gemas")
            .color(PRIMARY_COLOR)
            .decoration(TextDecoration.ITALIC, false);

    public GemRemover(int amount, Plugin plugin) {
        super(Material.PAPER, amount);
        ItemMeta meta = this.getItemMeta();

        // Set persistent data
        NamespacedKey key = new NamespacedKey(plugin, ItemConfig.GEM_REMOVER_KEY);
        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);

        // Set display name using Adventure API
        meta.displayName(DISPLAY_NAME);

        // Set Oraxen model
        //NamespacedKey itemModel = new NamespacedKey(NexoPlugin.instance(), "blessing_object");
        //meta.setItemModel(itemModel);


        meta.setCustomModelData(6005);

        // Set lore using Adventure API
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Arrastralo a un objeto para extraer sus gemas")
                .color(LORE_COLOR).decoration(TextDecoration.ITALIC,false));
        lore.add(Component.text("algunas pueden fallar.")
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

    public static String getGemRemoverKey() {
        return ItemConfig.GEM_REMOVER_KEY;
    }
}
