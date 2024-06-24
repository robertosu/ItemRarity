package cl.nightcore.itemrarity;

import cl.nightcore.itemrarity.abstracted.StatProvider;
import cl.nightcore.itemrarity.command.GetBlessingCommand;
import cl.nightcore.itemrarity.command.GetMagicCommand;
import cl.nightcore.itemrarity.command.GetRedemptionCommand;
import cl.nightcore.itemrarity.command.GetScrollCommand;
import cl.nightcore.itemrarity.abstracted.IdentifiedItem;
import cl.nightcore.itemrarity.item.RedemptionObject;
import cl.nightcore.itemrarity.statprovider.ArmorStatProvider;
import cl.nightcore.itemrarity.statprovider.WeaponStatProvider;
import cl.nightcore.itemrarity.type.IdentifiedArmor;
import cl.nightcore.itemrarity.type.IdentifiedWeapon;
import cl.nightcore.itemrarity.type.RolledArmor;
import cl.nightcore.itemrarity.type.RolledWeapon;
import cl.nightcore.itemrarity.item.BlessingObject;
import cl.nightcore.itemrarity.item.IdentifyScroll;
import cl.nightcore.itemrarity.item.MagicObject;
import cl.nightcore.itemrarity.listener.IdentifyScrollListener;
import cl.nightcore.itemrarity.listener.CancelUsageInRecipesListener;
import de.tr7zw.nbtapi.NBTItem;
import dev.aurelium.auraskills.api.item.ModifierType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import dev.aurelium.auraskills.api.stat.Stats;


import java.util.*;

public class ItemRarity extends JavaPlugin implements CommandExecutor {

    private static final String PLUGIN_PREFIX = ChatColor.GOLD + "[Pergamino]: " + ChatColor.BLUE;
    private static final String REROLL_PREFIX =
            net.md_5.bungee.api.ChatColor.of(MagicObject.getColor()) + "[Objeto Mágico]: " + net.md_5.bungee.api.ChatColor.of(MagicObject.getLorecolor());
    private static final String REDEMPTION_PREFIX =
            net.md_5.bungee.api.ChatColor.of(RedemptionObject.getColor()) + "[Redención]: " + net.md_5.bungee.api.ChatColor.of(RedemptionObject.getLorecolor());
    private static final String BLESSING_PREFIX =
            net.md_5.bungee.api.ChatColor.of(BlessingObject.getColor()) + "[Bendición]: " + net.md_5.bungee.api.ChatColor.of(BlessingObject.getLorecolor());
    public static final List<Stats> STATS = Collections.unmodifiableList(Arrays.asList(Stats.CRIT_CHANCE, Stats.CRIT_DAMAGE, Stats.HEALTH, Stats.LUCK,
            Stats.REGENERATION, Stats.SPEED, Stats.STRENGTH, Stats.TOUGHNESS,
            Stats.WISDOM));



    @Override
    public void onEnable() {
        Objects.requireNonNull(getCommand("getscroll")).setExecutor(new GetScrollCommand());
        Objects.requireNonNull(getCommand("getmagic")).setExecutor(new GetMagicCommand());
        Objects.requireNonNull(getCommand("getblessing")).setExecutor(new GetBlessingCommand());
        Objects.requireNonNull(getCommand("getredemption")).setExecutor(new GetRedemptionCommand());
        getServer().getPluginManager().registerEvents(new IdentifyScrollListener(), this);
        getServer().getPluginManager().registerEvents(new CancelUsageInRecipesListener(), this);
    }

    public static IdentifiedItem identifyItem(Player player, ItemStack item) {
        if (getItemType(item).equals("Weapon")) {
            IdentifiedWeapon weapon = new IdentifiedWeapon(item);
            player.sendMessage(PLUGIN_PREFIX + "¡Identificaste el arma! Calidad: " + weapon.getItemRarity());
            return weapon;
        } else if (getItemType(item).equals("Armor")) {
            IdentifiedArmor armor = new IdentifiedArmor(item);
            player.sendMessage(PLUGIN_PREFIX + "¡Identificaste la armadura! Calidad: " + armor.getItemRarity());
            return armor;
        }
        return null;
    }
    public static IdentifiedItem rollStats(Player player, ItemStack item) {
        RolledWeapon rolledWeapon;
        RolledArmor rolledArmor;
        if (getItemType(item).equals("Weapon")) {
            rolledWeapon = new RolledWeapon(item);
            rolledWeapon.rerollStats();
            rolledWeapon.incrementLevel(player);
            player.sendMessage(REROLL_PREFIX + "¡El objeto cambió! Rareza: " + rolledWeapon.getItemRarity());
            return rolledWeapon;
        }
        else if (getItemType(item).equals("Armor")) {
            rolledArmor = new RolledArmor(item);
            rolledArmor.rerollStats();
            rolledArmor.incrementLevel(player);
            player.sendMessage(REROLL_PREFIX + " ¡El objeto cambió! Rareza: " + rolledArmor.getItemRarity());
            return rolledArmor;
        }
        return null;
    }
    public static IdentifiedItem rerollLowestStat(Player player, ItemStack item){
        if (getItemType(item).equals("Weapon")) {
            IdentifiedWeapon moddedweapon= new IdentifiedWeapon(item);
            moddedweapon.rerollLowestStat(player);
            return moddedweapon;
        } else if (getItemType(item).equals("Armor")){
            IdentifiedArmor moddedarmor= new IdentifiedArmor(item);
            moddedarmor.rerollLowestStat(player);
            return moddedarmor;
        }
        return null;
    }
    public static IdentifiedItem rerollAllStatsExceptHighest(Player player, ItemStack item){
        if (getItemType(item).equals("Weapon")) {
            IdentifiedWeapon moddedweapon= new IdentifiedWeapon(item);
            moddedweapon.rerollExceptHighestStat(player);
            return moddedweapon;
        } else if (getItemType(item).equals("Armor")){
            IdentifiedArmor moddedarmor= new IdentifiedArmor(item);
            moddedarmor.rerollExceptHighestStat(player);
            return moddedarmor;
        }
        return null;
    }
    public static boolean isIdentified(ItemStack item) {
        return item != null && !item.getType().isAir() && new NBTItem(item).getBoolean(IdentifiedItem.getIdentifierKey());
    }
    public static boolean isIdentifyScroll(ItemStack item) {
        return item != null && !item.getType().isAir() && new NBTItem(item).getBoolean(IdentifyScroll.getIdentifyScrollKey());
    }
    public static boolean isRedemptionObject(ItemStack item) {
        return item != null && !item.getType().isAir() && new NBTItem(item).getBoolean(RedemptionObject.getRedeemObjectKey());
    }
    public static boolean isMagicObject(ItemStack item) {
        return item != null && !item.getType().isAir() && new NBTItem(item).getBoolean(MagicObject.getMagicObjectKey());
    }
    public static boolean isBlessingObject(ItemStack item) {
        return item != null && !item.getType().isAir() && new NBTItem(item).getBoolean(BlessingObject.getBlessingObjectKey());
    }

    public ModifierType getModifierType(ItemStack item) {
        switch (getItemType(item)) {
            case "Weapon":
                return ModifierType.ITEM;
            case "Armor":
                return ModifierType.ARMOR;
            default:
                return null;
        }
    }
    public static StatProvider getStatProvider(ItemStack item){
        switch(getItemType(item)){
            case "Weapon":return new WeaponStatProvider();
            case "Armor": return new ArmorStatProvider();
            default: return null;
        }
    }
    public static String getItemType(ItemStack item) {
        Material material = item.getType();
        // Verificar si es armadura
        if (material.name().endsWith("_HELMET") ||
                material.name().endsWith("_CHESTPLATE") ||
                material.name().endsWith("_LEGGINGS") ||
                material.name().endsWith("_BOOTS")) {
            return "Armor";
        }
        // Verificar si es arma
        else if (material.name().endsWith("_SWORD") ||
                material.name().endsWith("_AXE") ||
                material == Material.TRIDENT ||
                material == Material.BOW ||
                material == Material.CROSSBOW) {
            return "Weapon";
        } else {
            // Si no es armadura ni arma, retornar vacío.
            return "Unknown";
        }
    }

    public static Boolean isIdentifiable(ItemStack item) {
        return getItemType(item).equals("Weapon") || getItemType(item).equals("Armor");
    }

    public static String getPluginPrefix() {
        return PLUGIN_PREFIX;
    }
    public static String getRerollPrefix() {
        return REROLL_PREFIX;
    }
    public static String getBlessingPrefix() {
        return BLESSING_PREFIX;
    }
    public static String getRedemptionPrefix() {
        return REDEMPTION_PREFIX;
    }
}