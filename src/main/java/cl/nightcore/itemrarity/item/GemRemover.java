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

import java.util.ArrayList;
import java.util.List;

import static cl.nightcore.itemrarity.config.ItemConfig.GEM_REMOVER_KEY_NS;

@SuppressWarnings("unused")
public class GemRemover extends ItemStack {
    private static final TextColor PRIMARY_COLOR = TextColor.fromHexString("#e67415");
    private static final TextColor LORE_COLOR = TextColor.fromHexString("#d9a253");
    private static final Component DISPLAY_NAME = Component.text("Removedor de gemas")
            .color(PRIMARY_COLOR)
            .decoration(TextDecoration.ITALIC, false);

    public GemRemover(int amount, int level) {
        super(Material.PAPER, amount);
        ItemMeta meta = this.getItemMeta();

        // Set persistent data

        meta.getPersistentDataContainer().set(GEM_REMOVER_KEY_NS, PersistentDataType.INTEGER, level);

        // Set display name using Adventure API
        meta.displayName(DISPLAY_NAME.append(Component.text(" "+getRomanNumber(level)).color(PRIMARY_COLOR)));


        meta.setCustomModelData(getCustomModelData(level));

        // Set lore using Adventure API
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Arrastralo a un objeto para extraer sus gemas")
                .color(LORE_COLOR).decoration(TextDecoration.ITALIC,false));
        lore.add(Component.text("se cuidadoso al remover, algunas pueden romperse.")
                .color(LORE_COLOR).decoration(TextDecoration.ITALIC,false));
        lore.add(Component.text("Este removedor tiene un " +getPercentage(level)+"% de éxito")
                .color(LORE_COLOR).decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        // Set glint effect
        meta.setEnchantmentGlintOverride(true);

        this.setItemMeta(meta);
    }

    public static int getCustomModelData(int level){
        switch (level) {
            case 1 -> { return 6020; }
            case 2 -> { return 6021; }
            case 3 -> { return 6022; }
            // Add more cases for other numbers
            default -> { return 6005;}

        }
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

    public static NamespacedKey getGemRemoverKeyNs() {
        return GEM_REMOVER_KEY_NS;
    }

    public int getPercentage( int level){
        switch (level) {
            case 1 -> { return 25; }
            case 2 -> { return 50; }
            case 3 -> { return 75; }
            // Add more cases for other numbers
            default -> { return 25;}
        }
    }

    public String getRomanNumber(int level) {
        switch (level) {
            case 1 -> { return "I"; }
            case 2 -> { return "II"; }
            case 3 -> { return "III"; }
            // Add more cases for other numbers
            default -> { return "I ERROR"; }
        }
    }
}
