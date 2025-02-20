package cl.nightcore.itemrarity.item;


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

import java.util.ArrayList;
import java.util.List;

import static cl.nightcore.itemrarity.ItemRarity.PLUGIN;
import static cl.nightcore.itemrarity.config.ItemConfig.ITEM_UPGRADER_KEY_NS;

@SuppressWarnings("unused")
public class ItemUpgrader extends ItemStack {

    public final static String ITEM_UPGRADER_TYPE_KEY = "upgradertype";
    public final static NamespacedKey ITEM_UPGRADER_TYPE_KEY_NS = new NamespacedKey(PLUGIN,ITEM_UPGRADER_TYPE_KEY);
    public static final TextColor ACTIVE_COLOR = TextColor.fromHexString("#d9ff54");
    public static final TextColor UNSTABLE_COLOR = TextColor.fromHexString("#FF1012");
    public static final TextColor STABLE_COLOR = TextColor.fromHexString("#38FF45");
    public static final TextColor LORE_COLOR = TextColor.fromHexString("#9fa99f");

    private final TextColor primaryColor;
    private final int level;
    private final int type;
    private final String suffix;

    public ItemUpgrader(int amount, int level, int type) {
        super(Material.PAPER, amount);
        this.level = level;
        this.type = type;
        this.suffix = getSuffix(level);

        ItemMeta meta = this.getItemMeta();
        // Set persistent data
        meta.getPersistentDataContainer().set(ITEM_UPGRADER_KEY_NS, PersistentDataType.INTEGER, level);
        meta.getPersistentDataContainer().set(ITEM_UPGRADER_TYPE_KEY_NS, PersistentDataType.INTEGER, type);
        this.primaryColor = getPrimaryColor(type);
        // Set display name using Adventure API
        meta.displayName(getDisplayName());
        meta.setCustomModelData(getCustomModelData());

        // Set lore using Adventure API
        List<Component> lore = getLore(type, level);
        if(level==2){
            lore.addFirst(Component.text("Nivel superior: Esta forja tiene mayor tasa de éxito").color(NamedTextColor.WHITE));
        }
        meta.lore(lore);

        // Set glint effect
        meta.setEnchantmentGlintOverride(true);

        this.setItemMeta(meta);
    }

    public TextColor getPrimaryColor(int type){
        switch (type){
            case 1-> {return UNSTABLE_COLOR;}
            case 2 ->{return ACTIVE_COLOR;}
            case 3 ->{return STABLE_COLOR;}
            default -> {return null;}
        }
    }

    public int getCustomModelData(){
        if(type==1){
            switch (level) {
                case 1 -> { return 6007; }
                case 2 -> { return 6009; }
                // Add more cases for other numbers
                default -> { return 0;}

            }
        }else if(type==2){
            switch (level) {
                case 1 -> { return 6011; }
                case 2 -> { return 6013; }
                // Add more cases for other numbers
                default -> { return 0;}

            }
        }else if(type==3){
            switch (level) {
                case 1 -> { return 6015; }
                case 2 -> { return 6017; }
                // Add more cases for other numbers
                default -> { return 0;}
            }
        }
        return 0;
    }



    public Component getDisplayName(){
         switch (type){
            case 1->{return Component.text("Forja Inestable " + suffix)
                    .color(primaryColor)
                    .decoration(TextDecoration.ITALIC, false);
            }
            case 2 ->{return Component.text("Forja Activa " + suffix)
                    .color(primaryColor)
                    .decoration(TextDecoration.ITALIC, false);
            }
            case 3 ->{return Component.text("Forja Estable " + suffix)
                     .color(primaryColor)
                     .decoration(TextDecoration.ITALIC, false);
            }
            default ->{
                return null;
            }
         }
    }

    public List<Component>getLore(int type, int level){
        switch (type){
            case 1->{
                return loreUnstable();
            }
            case 2->{
                return loreActive();
            }
            case 3->{
                return loreStable();
            }
            default -> {
                return null;
            }
        }
    }

    private static List<Component> loreStable(){
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Arrastra esta runa al objeto que deseas mejorar")
                .color(LORE_COLOR).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(" ")
                .color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Si la mejora es exitosa tu objeto sube de nivel")
                .color(LORE_COLOR).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(" ")
                .color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Al fallar tu objeto no se rompe ni baja de nivel")
                .color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
        return lore;
    }

    private static List<Component> loreActive(){
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Arrastra esta runa al objeto que deseas mejorar")
                .color(LORE_COLOR).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(" ")
                .color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Si la mejora es exitosa tu objeto sube de nivel")
                .color(LORE_COLOR).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(" ")
                .color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Al fallar tu objeto bajará de nivel pero no se romperá")
                .color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        return lore;
    }

    private static List<Component> loreUnstable(){
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Arrastra esta runa al objeto que deseas mejorar")
                .color(LORE_COLOR).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(" ")
                .color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Si la mejora es exitosa tu objeto sube de nivel")
                .color(LORE_COLOR).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(" ")
                .color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Al fallar, tu objeto se romperá")
                .color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        return lore;
    }

    public String getSuffix(int level) {
        switch (level) {
            case 1 -> { return ""; }
            case 2 -> { return "de nivel superior"; }
            // Add more cases for other numbers
            default -> { return "ERROR ERROR"; }
        }
    }

    public static TextColor getActiveColor() {
        return ACTIVE_COLOR;
    }

    public static TextColor getLoreColor() {
        return LORE_COLOR;
    }

    public static String getItemUpgraderKey() {
        return ItemConfig.ITEM_UPGRADER_KEY;
    }

    public static NamespacedKey getItemUpgraderKeyNs() {
        return ItemConfig.ITEM_UPGRADER_KEY_NS;
    }
}