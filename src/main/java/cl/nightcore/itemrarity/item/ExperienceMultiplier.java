package cl.nightcore.itemrarity.item;

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

public class ExperienceMultiplier extends ItemStack {

    public static final NamespacedKey XP_MULTIPLIER_KEY_NS = new NamespacedKey(PLUGIN, "xp_multiplier");
    public static final TextColor LORE_COLOR = TextColor.fromHexString("#8bfaff");
    private static final TextColor PRIMARY_COLOR = TextColor.fromHexString("#25ff69");
    private final int multiplier;

    public ExperienceMultiplier(int amount, int multiplier) {
        super(Material.PAPER, amount);
        this.multiplier = multiplier;

        ItemMeta meta = this.getItemMeta();
        meta.getPersistentDataContainer().set(XP_MULTIPLIER_KEY_NS, PersistentDataType.INTEGER, multiplier);
        
        // Set display name
        meta.displayName(Component.text("Mult. de XP habilidades " + multiplier + "%")
                .color(PRIMARY_COLOR)
                .decoration(TextDecoration.ITALIC, false));
        // Set lore
        meta.lore(getTheLore());
        meta.setCustomModelData(6060 + (multiplier / 100 - 1)); // Modelos 6020-6024 para 100%-500%
        this.setItemMeta(meta);
    }

    private List<Component> getTheLore() {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(" ")
                .color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Arrastra este item al objeto que deseas mejorar")
                .color(LORE_COLOR).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Una vez agregado no se puede quitar")
                .color(LORE_COLOR).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(" ")
                .color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Añade un multiplicador de experiencia global de " + multiplier + "%")
                .color(LORE_COLOR).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("al usar un item con este multiplicador ganarás mas XP")
                .color(LORE_COLOR).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("en todas las habilidades (/skills).")
                .color(LORE_COLOR).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(" ")
                .color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Si el objeto ya tiene un multiplicador igual o mayor")
                .color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("este no se agregará, si agregas uno mayor no recuperarás")
                .color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("el anterior.")
                .color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        return lore;
    }

    public int getMultiplier() {
        return multiplier;
    }
}