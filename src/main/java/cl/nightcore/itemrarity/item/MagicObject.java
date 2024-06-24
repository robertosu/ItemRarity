package cl.nightcore.itemrarity.item;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MagicObject extends ItemStack {
    private static final String MAGIC_OBJECT_KEY = "MagicObject";
    private static  final String COLOR = "#6E6FF8";
    private  static final String LORECOLOR = "#CA9CDE";
    private static final String MAGIC_OBJECT_DISPLAY_NAME = net.md_5.bungee.api.ChatColor.of(COLOR) + "Objeto Mágico";


    public MagicObject(int amount) {
        super(Material.PAPER, amount);
        NBTItem nbti = new NBTItem(this);
        nbti.setBoolean(MAGIC_OBJECT_KEY, true);
        nbti.mergeNBT(this);
        ItemMeta meta = this.getItemMeta();
        Objects.requireNonNull(meta).setDisplayName(MAGIC_OBJECT_DISPLAY_NAME);
        meta.setCustomModelData(6001);
        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.add(net.md_5.bungee.api.ChatColor.of(LORECOLOR) + "" + ChatColor.ITALIC + "Elimina los bonos de uno de tus objetos");
        lore.add(net.md_5.bungee.api.ChatColor.of(LORECOLOR) + "" + ChatColor.ITALIC + "y añade nuevos. Aumenta la magia de tu objeto");
        meta.setLore(lore);

        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        this.addUnsafeEnchantment(Enchantment.LUCK,1);
        this.setItemMeta(meta);
    }
    public static  String getColor(){return COLOR;}
    public static String getLorecolor(){return  LORECOLOR;}
    public static String getMagicObjectKey() {
        return MAGIC_OBJECT_KEY;
    }

}