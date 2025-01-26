package cl.nightcore.itemrarity.item;


import cl.nightcore.itemrarity.config.ItemConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

import static cl.nightcore.itemrarity.config.ItemConfig.ITEM_UPGRADER_KEY_NS;

public class ItemUpgrader extends ItemStack {

    private static final TextColor PRIMARY_COLOR = TextColor.fromHexString("#d9ff54");
    private static final TextColor LORE_COLOR = TextColor.fromHexString("#9fa99f");
    private static final Component DISPLAY_NAME = Component.text("Runa activa")
            .color(PRIMARY_COLOR)
            .decoration(TextDecoration.ITALIC, false);



    public ItemUpgrader(int amount, int level) {
        super(Material.PAPER, amount);
        ItemMeta meta = this.getItemMeta();
        // Set persistent data
        meta.getPersistentDataContainer().set(ITEM_UPGRADER_KEY_NS, PersistentDataType.INTEGER, level);

        // Set display name using Adventure API
        meta.displayName(DISPLAY_NAME.append(Component.text(" "+getRomanNumber(level))));

        meta.setCustomModelData(getCustomModelData(level)); // Cambia este valor según el modelo que desees

        // Set lore using Adventure API
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Arrastra esta runa al objeto que desear mejorar")
                .color(LORE_COLOR).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("si la mejora es exitosa tu objeto subirá de nivel.")
                .color(LORE_COLOR).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Esta runa tiene un " +getPercentage(level)+"% de éxito")
                .color(LORE_COLOR).decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        // Set glint effect
        meta.setEnchantmentGlintOverride(true);

        this.setItemMeta(meta);
    }

    public static int getCustomModelData(int level){
        switch (level) {
            case 1 -> { return 6007; }
            case 2 -> { return 6008; }
            case 3 -> { return 6009; }
            // Add more cases for other numbers
            default -> { return 6007;}

        }
    }

    public static TextColor getPrimaryColor() {
        return PRIMARY_COLOR;
    }

    public static TextColor getLoreColor() {
        return LORE_COLOR;
    }

    public static String getBlessingBallKey() {
        return ItemConfig.ITEM_UPGRADER_KEY;
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

    public int getLevel(){
        ItemMeta meta = this.getItemMeta();
        return meta.getPersistentDataContainer().get(ITEM_UPGRADER_KEY_NS, PersistentDataType.INTEGER);
    }
}