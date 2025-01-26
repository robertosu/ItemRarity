package cl.nightcore.itemrarity;

import cl.nightcore.itemrarity.abstracted.SocketableItem;
import cl.nightcore.itemrarity.command.*;
import cl.nightcore.itemrarity.config.ItemConfig;
import cl.nightcore.itemrarity.customstats.CustomStats;
import cl.nightcore.itemrarity.customstats.CustomTraits;
import cl.nightcore.itemrarity.customstats.DodgeChanceTrait;
import cl.nightcore.itemrarity.customstats.HitChanceTrait;
import cl.nightcore.itemrarity.item.IdentifyScroll;
import cl.nightcore.itemrarity.item.MagicObject;
import cl.nightcore.itemrarity.listener.CancelUsageInRecipesListener;
import cl.nightcore.itemrarity.listener.IdentifyScrollListener;
import cl.nightcore.itemrarity.listener.ItemClickListener;
import cl.nightcore.itemrarity.loot.CustomDropsManager;
import cl.nightcore.itemrarity.type.IdentifiedAbstract;
import cl.nightcore.itemrarity.type.RolledAbstract;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.registry.NamespacedRegistry;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;
import java.util.Objects;

public class ItemRarity extends JavaPlugin implements CommandExecutor {

    public static ItemRarity PLUGIN;
    public static Locale AURA_LOCALE;

    public static SocketableItem identifyItem(Player player, ItemStack item) {
        IdentifiedAbstract weapon = new IdentifiedAbstract(item);
        Component message = Component.text("¡Identificaste el arma! Calidad: ", IdentifyScroll.getLoreColor()).append(weapon.getRarityComponent());
        player.sendMessage(ItemConfig.PLUGIN_PREFIX.append(message));
        return weapon;
    }

    public static SocketableItem rollStats(Player player, ItemStack item) {
        RolledAbstract rolledAbstract;
        rolledAbstract = new RolledAbstract(item);
        rolledAbstract.incrementRollCount(player);
        rolledAbstract.rerollStatsEnhanced();
        Component message = Component.text("¡El objeto cambió! Rareza: ", MagicObject.getLoreColor());
        player.sendMessage(ItemConfig.REROLL_PREFIX.append(message).append(rolledAbstract.getRarityComponent()));
        return rolledAbstract;
    }

    public static SocketableItem rerollLowestStat(Player player, ItemStack item) {
        IdentifiedAbstract moddedweapon = new IdentifiedAbstract(item);
        moddedweapon.rerollLowestStat(player);
        return moddedweapon;
    }

    public static SocketableItem rerollAllStatsExceptHighest(Player player, ItemStack item) {
        IdentifiedAbstract moddedweapon = new IdentifiedAbstract(item);
        moddedweapon.rerollExceptHighestStat(player);
        return moddedweapon;
    }

    @Override
    public void onEnable() {
        PLUGIN = this;
        Objects.requireNonNull(getCommand("getscroll")).setExecutor(new GetScrollCommand());
        Objects.requireNonNull(getCommand("getmagic")).setExecutor(new GetMagicCommand());
        Objects.requireNonNull(getCommand("getblessing")).setExecutor(new GetBlessingCommand());
        Objects.requireNonNull(getCommand("getredemption")).setExecutor(new GetRedemptionCommand());
        Objects.requireNonNull(getCommand("getgem")).setExecutor(new GetGemCommand());
        Objects.requireNonNull(getCommand("getremover")).setExecutor(new GetGemRemoverCommand());
        Objects.requireNonNull(getCommand("getblessingball")).setExecutor(new GetBlessingBallCommand());
        Objects.requireNonNull(getCommand("getitemupgrader")).setExecutor(new GetItemUpgraderCommand());

        getServer().getPluginManager().registerEvents(new ItemClickListener(), this);
        getServer().getPluginManager().registerEvents(new IdentifyScrollListener(), this);
        getServer().getPluginManager().registerEvents(new CancelUsageInRecipesListener(), this);
        getServer().getPluginManager().registerEvents(new CustomDropsManager(), this);
        AURA_LOCALE = AuraSkillsApi.get().getMessageManager().getDefaultLanguage();
        AuraSkillsApi auraSkills = AuraSkillsApi.get();
        NamespacedRegistry registry = auraSkills.useRegistry("itemrarity", getDataFolder());
        loadAuraSkillsCustoms(registry, auraSkills);
    }

    private void loadAuraSkillsCustoms(NamespacedRegistry registry, AuraSkillsApi auraSkills) {
        registry.registerTrait(CustomTraits.DODGE_CHANCE);
        registry.registerStat(CustomStats.DEXTERITY);
        registry.registerTrait(CustomTraits.HIT_CHANCE);
        registry.registerStat(CustomStats.ACCURACY);
        auraSkills.getHandlers().registerTraitHandler(new DodgeChanceTrait(auraSkills));
        auraSkills.getHandlers().registerTraitHandler(new HitChanceTrait(auraSkills));
        saveResource("stats.yml", false);
    }

}