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

public class BlessingObject extends ItemStack {
    private static final String BLESSING_OBJECT_KEY = "BlessingObject";
    private static  final String COLOR = "#12B8E6";
    private static final String LORECOLOR = "#7FD9B0";
    private static final String BLESSING_OBJECT_DISPLAY_NAME = net.md_5.bungee.api.ChatColor.of(COLOR) + "Bendición";

    public BlessingObject(int amount) {
        super(Material.PAPER, amount);
        NBTItem nbti = new NBTItem(this);
        nbti.setBoolean(BLESSING_OBJECT_KEY, true);
        nbti.mergeNBT(this);
        ItemMeta meta = this.getItemMeta();
        Objects.requireNonNull(meta).setDisplayName(BLESSING_OBJECT_DISPLAY_NAME);
        meta.setCustomModelData(6002);
        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.add(net.md_5.bungee.api.ChatColor.of(LORECOLOR) + "" + ChatColor.ITALIC + "Arrastralo a un objeto para cambiar su ");
        lore.add(net.md_5.bungee.api.ChatColor.of(LORECOLOR) + "" + ChatColor.ITALIC + "estadística mas baja sin alterar el resto.");
        meta.setLore(lore);

        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        this.addUnsafeEnchantment(Enchantment.LUCK,1);

        this.setItemMeta(meta);
    }
    public static  String getColor(){return COLOR;}
    public static String getLorecolor(){return  LORECOLOR;}
    public static String getBlessingObjectKey() {
        return BLESSING_OBJECT_KEY;
    }

}