package cl.nightcore.itemrarity;

import cl.nightcore.itemrarity.abstracted.SocketableItem;
import cl.nightcore.itemrarity.command.*;
import cl.nightcore.itemrarity.item.BlessingObject;
import cl.nightcore.itemrarity.item.IdentifyScroll;
import cl.nightcore.itemrarity.item.MagicObject;
import cl.nightcore.itemrarity.item.RedemptionObject;
import cl.nightcore.itemrarity.listener.CancelUsageInRecipesListener;
import cl.nightcore.itemrarity.listener.IdentifyScrollListener;
import cl.nightcore.itemrarity.listener.ItemClickListener;
import cl.nightcore.itemrarity.loot.CustomDropsManager;
import cl.nightcore.itemrarity.type.IdentifiedArmor;
import cl.nightcore.itemrarity.type.IdentifiedWeapon;
import cl.nightcore.itemrarity.type.RolledArmor;
import cl.nightcore.itemrarity.type.RolledWeapon;
import cl.nightcore.itemrarity.util.ItemUtil;
import dev.aurelium.auraskills.api.stat.Stats;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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

    public static SocketableItem identifyItem(Player player, ItemStack item) {
        if (ItemUtil.getItemType(item).equals("Weapon")) {
            IdentifiedWeapon weapon = new IdentifiedWeapon(item);
            Component message = Component.text("¡Identificaste el arma! Calidad: ", IdentifyScroll.getLoreColor()).append(weapon.getRarityKeyword().color(weapon.getRarityColor()));
            player.sendMessage(PLUGIN_PREFIX.append(message));
            return weapon;
        } else if (ItemUtil.getItemType(item).equals("Armor")) {
            IdentifiedArmor armor = new IdentifiedArmor(item);
            Component message = Component.text("¡Identificaste la armadura! Calidad: ", IdentifyScroll.getLoreColor()).append(armor.getRarityKeyword().color(armor.getRarityColor()));
            player.sendMessage(PLUGIN_PREFIX.append(message));
            return armor;
        }
        return null;
    }


    public static SocketableItem rollStats(Player player, ItemStack item) {
        RolledWeapon rolledWeapon;
        RolledArmor rolledArmor;
        if (ItemUtil.getItemType(item).equals("Weapon")) {
            rolledWeapon = new RolledWeapon(item);
            rolledWeapon.rerollStatsEnhanced();
            rolledWeapon.incrementLevel(player);
            Component message = Component.text("¡El objeto cambió! Rareza: ", MagicObject.getLoreColor());
            player.sendMessage(REROLL_PREFIX.append(message).append(rolledWeapon.getRarityKeyword().color(rolledWeapon.getRarityColor())));
            return rolledWeapon;
        } else if (ItemUtil.getItemType(item).equals("Armor")) {
            rolledArmor = new RolledArmor(item);
            rolledArmor.rerollStatsEnhanced();
            rolledArmor.incrementLevel(player);
            Component message = Component.text("¡El objeto cambió! Rareza: ", MagicObject.getLoreColor());
            player.sendMessage(REROLL_PREFIX.append(message).append(rolledArmor.getRarityKeyword().color(rolledArmor.getRarityColor())));
            return rolledArmor;
        }
        return null;
    }

    public static SocketableItem rerollLowestStat(Player player, ItemStack item) {
        if (ItemUtil.getItemType(item).equals("Weapon")) {
            IdentifiedWeapon moddedweapon = new IdentifiedWeapon(item);
            moddedweapon.rerollLowestStat(player);
            return moddedweapon;
        } else if (ItemUtil.getItemType(item).equals("Armor")) {
            IdentifiedArmor moddedarmor = new IdentifiedArmor(item);
            moddedarmor.rerollLowestStat(player);
            return moddedarmor;
        }
        return null;
    }

    public static SocketableItem rerollAllStatsExceptHighest(Player player, ItemStack item) {
        if (ItemUtil.getItemType(item).equals("Weapon")) {
            IdentifiedWeapon moddedweapon = new IdentifiedWeapon(item);
            moddedweapon.rerollExceptHighestStat(player);
            return moddedweapon;
        } else if (ItemUtil.getItemType(item).equals("Armor")) {
            IdentifiedArmor moddedarmor = new IdentifiedArmor(item);
            moddedarmor.rerollExceptHighestStat(player);
            return moddedarmor;
        }
        return null;
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
        plugin = this;
        Objects.requireNonNull(getCommand("getscroll")).setExecutor(new GetScrollCommand());
        Objects.requireNonNull(getCommand("getmagic")).setExecutor(new GetMagicCommand());
        Objects.requireNonNull(getCommand("getblessing")).setExecutor(new GetBlessingCommand());
        Objects.requireNonNull(getCommand("getredemption")).setExecutor(new GetRedemptionCommand());
        Objects.requireNonNull(getCommand("getgem")).setExecutor(new GetGemCommand());

        getServer().getPluginManager().registerEvents(new ItemClickListener(), this);
        getServer().getPluginManager().registerEvents(new IdentifyScrollListener(), this);
        getServer().getPluginManager().registerEvents(new CancelUsageInRecipesListener(), this);
        getServer().getPluginManager().registerEvents(new CustomDropsManager(), this);
    }

}