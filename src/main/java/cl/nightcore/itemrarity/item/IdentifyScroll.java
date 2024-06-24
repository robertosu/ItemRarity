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

public class IdentifyScroll extends ItemStack {
    private static final String IDENTIFY_SCROLL_KEY ="IdentifyScroll";
    private static final String IDENTIFY_SCROLL_DISPLAYNAME =ChatColor.GOLD + "Pergamino de identificación";
    public IdentifyScroll(int amount) {
        super(Material.PAPER, amount);
        NBTItem nbti = new NBTItem(this);
        nbti.setBoolean(IDENTIFY_SCROLL_KEY, true);
        nbti.mergeNBT(this);
        ItemMeta meta = this.getItemMeta();
        assert meta != null;
        meta.setDisplayName(IDENTIFY_SCROLL_DISPLAYNAME);
        meta.setCustomModelData(6000);
        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "Arrastra este item a tu equipamiento para");
        lore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "desbloquear sus estadísticas adicionales.");
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        this.addUnsafeEnchantment(Enchantment.LUCK,1);
        this.setItemMeta(meta);
    }
    public static String getIdentifyScrollKey(){
        return IDENTIFY_SCROLL_KEY;
    }
}