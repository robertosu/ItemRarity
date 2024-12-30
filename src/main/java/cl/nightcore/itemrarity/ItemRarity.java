package cl.nightcore.itemrarity;

import cl.nightcore.itemrarity.abstracted.IdentifiedItem;
import cl.nightcore.itemrarity.abstracted.StatProvider;
import cl.nightcore.itemrarity.command.GetBlessingCommand;
import cl.nightcore.itemrarity.command.GetMagicCommand;
import cl.nightcore.itemrarity.command.GetRedemptionCommand;
import cl.nightcore.itemrarity.command.GetScrollCommand;
import cl.nightcore.itemrarity.item.BlessingObject;
import cl.nightcore.itemrarity.item.IdentifyScroll;
import cl.nightcore.itemrarity.item.MagicObject;
import cl.nightcore.itemrarity.item.RedemptionObject;
import cl.nightcore.itemrarity.listener.CancelUsageInRecipesListener;
import cl.nightcore.itemrarity.listener.IdentifyScrollListener;
import cl.nightcore.itemrarity.listener.ItemClickListener;
import cl.nightcore.itemrarity.loot.CustomDropsManager;
import cl.nightcore.itemrarity.statprovider.ArmorStatProvider;
import cl.nightcore.itemrarity.statprovider.WeaponStatProvider;
import cl.nightcore.itemrarity.type.IdentifiedArmor;
import cl.nightcore.itemrarity.type.IdentifiedWeapon;
import cl.nightcore.itemrarity.type.RolledArmor;
import cl.nightcore.itemrarity.type.RolledWeapon;
import dev.aurelium.auraskills.api.item.ModifierType;
import dev.aurelium.auraskills.api.stat.Stats;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;

public class ItemRarity extends JavaPlugin implements CommandExecutor {
    public static final List<Stats> STATS = List.of(Stats.CRIT_CHANCE, Stats.CRIT_DAMAGE, Stats.HEALTH, Stats.LUCK, Stats.REGENERATION, Stats.SPEED, Stats.STRENGTH, Stats.TOUGHNESS, Stats.WISDOM);
    private static final Component PLUGIN_PREFIX = Component.text("[Pergamino]: ").color(NamedTextColor.GOLD);
    private static final Component REROLL_PREFIX = Component.text("[Objeto Mágico]: ").color(MagicObject.getPrimaryColor());
    private static final Component REDEMPTION_PREFIX = Component.text("[Redención]: ").color(RedemptionObject.getPrimaryColor());
    private static final Component BLESSING_PREFIX = Component.text("[Bendición]: ").color(BlessingObject.getPrimaryColor());
    public static Plugin plugin;

    public static IdentifiedItem identifyItem(Player player, ItemStack item) {
        if (getItemType(item).equals("Weapon")) {
            IdentifiedWeapon weapon = new IdentifiedWeapon(item);
            Component message = Component.text("¡Identificaste el arma! Calidad: ", IdentifyScroll.getLoreColor()).append(weapon.getItemRarity());
            player.sendMessage(PLUGIN_PREFIX.append(message));
            return weapon;
        } else if (getItemType(item).equals("Armor")) {
            IdentifiedArmor armor = new IdentifiedArmor(item);
            Component message = Component.text("¡Identificaste la armadura! Calidad: ", IdentifyScroll.getLoreColor()).append(armor.getItemRarity());
            player.sendMessage(PLUGIN_PREFIX.append(message));
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
            Component message = Component.text("¡El objeto cambió! Rareza: ", MagicObject.getLoreColor());
            player.sendMessage(REROLL_PREFIX.append(message).append(rolledWeapon.getItemRarity()));
            return rolledWeapon;
        } else if (getItemType(item).equals("Armor")) {
            rolledArmor = new RolledArmor(item);
            rolledArmor.rerollStats();
            rolledArmor.incrementLevel(player);
            Component message = Component.text("¡El objeto cambió! Rareza: ", MagicObject.getLoreColor());
            player.sendMessage(REROLL_PREFIX.append(message).append(rolledArmor.getItemRarity()));
            return rolledArmor;
        }
        return null;
    }

    public static IdentifiedItem rerollLowestStat(Player player, ItemStack item) {
        if (getItemType(item).equals("Weapon")) {
            IdentifiedWeapon moddedweapon = new IdentifiedWeapon(item);
            moddedweapon.rerollLowestStat(player);
            return moddedweapon;
        } else if (getItemType(item).equals("Armor")) {
            IdentifiedArmor moddedarmor = new IdentifiedArmor(item);
            moddedarmor.rerollLowestStat(player);
            return moddedarmor;
        }
        return null;
    }

    public static IdentifiedItem rerollAllStatsExceptHighest(Player player, ItemStack item) {
        if (getItemType(item).equals("Weapon")) {
            IdentifiedWeapon moddedweapon = new IdentifiedWeapon(item);
            moddedweapon.rerollExceptHighestStat(player);
            return moddedweapon;
        } else if (getItemType(item).equals("Armor")) {
            IdentifiedArmor moddedarmor = new IdentifiedArmor(item);
            moddedarmor.rerollExceptHighestStat(player);
            return moddedarmor;
        }
        return null;
    }

    public static boolean isNotEmpty(ItemStack item) {
        return item != null && !item.getType().isAir();
    }

    public static boolean isIdentified(ItemStack item) {
        return checkBooleanTag(item, IdentifiedItem.getIdentifierKey());
    }

    public static boolean isIdentifyScroll(ItemStack item) {
        return checkBooleanTag(item, IdentifyScroll.getIdentifyScrollKey());
    }

    public static boolean isRedemptionObject(ItemStack item) {
        return checkBooleanTag(item, RedemptionObject.getRedeemObjectKey());
    }

    public static boolean isMagicObject(ItemStack item) {
        return checkBooleanTag(item, MagicObject.getMagicObjectKey());
    }

    public static boolean isBlessingObject(ItemStack item) {
        return checkBooleanTag(item, BlessingObject.getBlessingObjectKey());
    }

    private static boolean checkBooleanTag(ItemStack item, String key) {
        if (item == null || item.getType().isAir() || plugin == null) {
            return false;
        }
        NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
        return item.getItemMeta() != null && item.getItemMeta().getPersistentDataContainer().has(namespacedKey, PersistentDataType.BOOLEAN) && item.getItemMeta().getPersistentDataContainer().get(namespacedKey, PersistentDataType.BOOLEAN) == Boolean.TRUE;
    }

    public static StatProvider getStatProvider(ItemStack item) {
        return switch (getItemType(item)) {
            case "Weapon" -> new WeaponStatProvider();
            case "Armor" -> new ArmorStatProvider();
            default -> null;
        };
    }

    public static String getItemType(ItemStack item) {
        Material material = item.getType();
        // Verificar si es armadura
        if (material.name().endsWith("_HELMET") || material.name().endsWith("_CHESTPLATE") || material.name().endsWith("_LEGGINGS") || material.name().endsWith("_BOOTS")) {
            return "Armor";
        }
        // Verificar si es arma
        else if (material.name().endsWith("_SWORD") || material.name().endsWith("_AXE") || material == Material.TRIDENT || material == Material.BOW || material == Material.CROSSBOW) {
            return "Weapon";
        } else {
            // Si no es armadura ni arma, retornar vacío.
            return "Unknown";
        }
    }

    public static Boolean isIdentifiable(ItemStack item) {
        return getItemType(item).equals("Weapon") || getItemType(item).equals("Armor");
    }

    public static Component getPluginPrefix() {
        return PLUGIN_PREFIX;
    }

    public static Component getRerollPrefix() {
        return REROLL_PREFIX;
    }

    public static Component getBlessingPrefix() {
        return BLESSING_PREFIX;
    }

    public static Component getRedemptionPrefix() {
        return REDEMPTION_PREFIX;
    }

    @Override
    public void onEnable() {
        Objects.requireNonNull(getCommand("getscroll")).setExecutor(new GetScrollCommand());
        Objects.requireNonNull(getCommand("getmagic")).setExecutor(new GetMagicCommand());
        Objects.requireNonNull(getCommand("getblessing")).setExecutor(new GetBlessingCommand());
        Objects.requireNonNull(getCommand("getredemption")).setExecutor(new GetRedemptionCommand());
        getServer().getPluginManager().registerEvents(new ItemClickListener(), this);
        getServer().getPluginManager().registerEvents(new IdentifyScrollListener(), this);
        getServer().getPluginManager().registerEvents(new CancelUsageInRecipesListener(), this);
        plugin = ItemRarity.getPlugin(ItemRarity.class);
        getServer().getPluginManager().registerEvents(new CustomDropsManager(), this);
    }

    public ModifierType getModifierType(ItemStack item) {
        return switch (getItemType(item)) {
            case "Weapon" -> ModifierType.ITEM;
            case "Armor" -> ModifierType.ARMOR;
            default -> null;
        };
    }
}