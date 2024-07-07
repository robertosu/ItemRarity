package cl.nightcore.itemrarity.item;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
public class MagicObject extends ItemStack {
    private static final String MAGIC_OBJECT_KEY = "MagicObject";
    private static final String COLOR = "#6E6FF8";
    private static final String LORECOLOR = "#CA9CDE";
    private static final String MAGIC_OBJECT_DISPLAY_NAME = ChatColor.of(COLOR) + "Objeto Mágico";

    public MagicObject(int amount, Plugin plugin) {
        super(Material.PAPER, amount);
        ItemMeta meta = this.getItemMeta();

        NamespacedKey key = new NamespacedKey(plugin, MAGIC_OBJECT_KEY);
        meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);

        meta.setDisplayName(MAGIC_OBJECT_DISPLAY_NAME);
        meta.setCustomModelData(6001);

        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.add(ChatColor.of(LORECOLOR) + "" + ChatColor.ITALIC + "Elimina los bonos de uno de tus objetos");
        lore.add(ChatColor.of(LORECOLOR) + "" + ChatColor.ITALIC + "y añade nuevos. Aumenta la magia de tu objeto");
        meta.setLore(lore);

        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        this.addUnsafeEnchantment(Enchantment.FORTUNE, 1);

        this.setItemMeta(meta);
    }

    public static String getColor() {
        return COLOR;
    }

    public static String getLorecolor() {
        return LORECOLOR;
    }

    public static String getMagicObjectKey() {
        return MAGIC_OBJECT_KEY;
    }
}
