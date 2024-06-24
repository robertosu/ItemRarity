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

public class RedemptionObject extends ItemStack {
    private static final String REDEEM_OBJECT_KEY = "RedemptionObject";
    private static  final String COLOR = "#FD645B";
    private static final String LORECOLOR = "#F28E89";
    private static final String REDEEM_OBJECT_DISPLAY_NAME = net.md_5.bungee.api.ChatColor.of(COLOR) + "Redención";

    public RedemptionObject(int amount) {
        super(Material.PAPER, amount);
        NBTItem nbti = new NBTItem(this);
        nbti.setBoolean(REDEEM_OBJECT_KEY, true);
        nbti.mergeNBT(this);
        ItemMeta meta = this.getItemMeta();
        Objects.requireNonNull(meta).setDisplayName(REDEEM_OBJECT_DISPLAY_NAME);
        meta.setCustomModelData(6003);
        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.add(net.md_5.bungee.api.ChatColor.of(LORECOLOR) + "" + ChatColor.ITALIC + "Arrastralo a un objeto para cambiar las ");
        lore.add(net.md_5.bungee.api.ChatColor.of(LORECOLOR) + "" + ChatColor.ITALIC + "estadísticas sin alterar las mas alta.");
        meta.setLore(lore);

        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        this.addUnsafeEnchantment(Enchantment.LUCK,1);
        this.setItemMeta(meta);
    }
    public static  String getColor(){return COLOR;}
    public static String getLorecolor(){return  LORECOLOR;}
    public static String getRedeemObjectKey() {
        return REDEEM_OBJECT_KEY;
    }

}