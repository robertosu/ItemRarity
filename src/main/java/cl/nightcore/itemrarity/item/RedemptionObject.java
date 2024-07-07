package cl.nightcore.itemrarity.item;

import org.bukkit.ChatColor;
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
import java.util.Objects;

public class RedemptionObject extends ItemStack {
    private static final String REDEEM_OBJECT_KEY = "RedemptionObject";
    private static  final String COLOR = "#FD645B";
    private static final String LORECOLOR = "#F28E89";
    private static final String REDEEM_OBJECT_DISPLAY_NAME = net.md_5.bungee.api.ChatColor.of(COLOR) + "Redención";

    public RedemptionObject(int amount, Plugin plugin) {
        super(Material.PAPER, amount);
        ItemMeta meta = this.getItemMeta();
        Objects.requireNonNull(meta);

        // Paso 1: Establecer el dato persistente
        NamespacedKey key = new NamespacedKey(plugin, REDEEM_OBJECT_KEY);
        meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);

        // Paso 2: Configurar el nombre de visualización
        meta.setDisplayName(REDEEM_OBJECT_DISPLAY_NAME);

        // Paso 3: Establecer el CustomModelData
        meta.setCustomModelData(6003);

        // Paso 4: Configurar el lore
        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.add(net.md_5.bungee.api.ChatColor.of(LORECOLOR) + "" + ChatColor.ITALIC + "Arrastralo a un objeto para cambiar las ");
        lore.add(net.md_5.bungee.api.ChatColor.of(LORECOLOR) + "" + ChatColor.ITALIC + "estadísticas sin alterar las mas alta.");
        meta.setLore(lore);

        // Paso 5: Añadir ItemFlags
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        // Paso 6: Aplicar el ItemMeta al ItemStack
        this.setItemMeta(meta);

        // Paso 7: Añadir encantamiento
        this.addUnsafeEnchantment(Enchantment.FORTUNE, 1);
    }

    public static  String getColor(){return COLOR;}
    public static String getLorecolor(){return  LORECOLOR;}
    public static String getRedeemObjectKey() {
        return REDEEM_OBJECT_KEY;
    }

}