package cl.nightcore.itemrarity;

import cl.nightcore.itemrarity.command.*;
import cl.nightcore.itemrarity.customstats.*;
import cl.nightcore.itemrarity.listener.AnvilListener;
import cl.nightcore.itemrarity.listener.CancelUsageInRecipesListener;
import cl.nightcore.itemrarity.listener.IdentifyScrollListener;
import cl.nightcore.itemrarity.listener.ItemClickListener;
import cl.nightcore.itemrarity.loot.CustomDropsManager;
import cl.nightcore.itemrarity.abstracted.UpgradeableItem;
import cl.nightcore.itemrarity.util.ItemRepairManager;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.registry.NamespacedRegistry;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class ItemRarity extends JavaPlugin implements CommandExecutor {

    public static ItemRarity PLUGIN;
    public static Locale AURA_LOCALE;

    public static UpgradeableItem identifyItem(Player player, ItemStack item) {
        UpgradeableItem weapon = new UpgradeableItem(item);
        weapon.identify(player);
        weapon.initializeSocketData();
        return weapon;
    }

    public static UpgradeableItem rollStats(Player player, ItemStack item) {
        UpgradeableItem upgradeableItem;
        upgradeableItem = new UpgradeableItem(item);
        upgradeableItem.rerollStatsEnhanced(player);
        return upgradeableItem;
    }

    public static UpgradeableItem rerollLowestStat(Player player, ItemStack item) {
        UpgradeableItem moddedweapon = new UpgradeableItem(item);
        moddedweapon.rerollLowestStat(player);
        return moddedweapon;
    }

    public static UpgradeableItem rerollAllStatsExceptHighest(Player player, ItemStack item) {
        UpgradeableItem moddedweapon = new UpgradeableItem(item);
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
        Objects.requireNonNull(getCommand("getsocketstone")).setExecutor(new GetSocketStoneCommand());
        Objects.requireNonNull(getCommand("testdistr")).setExecutor(new TestCommand());

        getServer().getPluginManager().registerEvents(new ItemClickListener(), this);
        getServer().getPluginManager().registerEvents(new IdentifyScrollListener(), this);
        getServer().getPluginManager().registerEvents(new CancelUsageInRecipesListener(), this);
        getServer().getPluginManager().registerEvents(new CustomDropsManager(), this);
        AURA_LOCALE = AuraSkillsApi.get().getMessageManager().getDefaultLanguage();
        AuraSkillsApi auraSkills = AuraSkillsApi.get();
        NamespacedRegistry registry = auraSkills.useRegistry("itemrarity", getDataFolder());
        loadAuraSkillsCustoms(registry, auraSkills);
        loadRepairableItems();
    }

    private void loadAuraSkillsCustoms(NamespacedRegistry registry, AuraSkillsApi auraSkills) {
        registry.registerTrait(CustomTraits.DODGE_CHANCE);
        registry.registerStat(CustomStats.DEXTERITY);
        registry.registerTrait(CustomTraits.ATTACK_SPEED);
        registry.registerTrait(CustomTraits.HIT_CHANCE);
        registry.registerStat(CustomStats.EVASION);
        registry.registerStat(CustomStats.ACCURACY);
        auraSkills.getHandlers().registerTraitHandler(new DodgeChanceTrait(auraSkills));
        auraSkills.getHandlers().registerTraitHandler(new HitChanceTrait(auraSkills));
        auraSkills.getHandlers().registerTraitHandler(new AttackSpeedTraitHandler(auraSkills));
        saveResource("stats.yml", false);
    }

    private void loadRepairableItems(){

        ItemRepairManager repairManager = new ItemRepairManager();
        Set<String> zafiroItems = new HashSet<>();
        zafiroItems.add("sapphire_sword");
        zafiroItems.add("sapphire_chestplate");
        zafiroItems.add("sapphire_helmet");
        zafiroItems.add("sapphire_boots");
        zafiroItems.add("sapphire_leggings");
        repairManager.registerRepairGroup(zafiroItems, "sapphire");

        // Registrar el listener del yunque
        getServer().getPluginManager().registerEvents(new AnvilListener(repairManager, this), this);


    }

}