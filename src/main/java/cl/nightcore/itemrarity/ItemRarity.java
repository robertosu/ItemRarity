package cl.nightcore.itemrarity;

import cl.nightcore.itemrarity.abstracted.UpgradeableItem;
import cl.nightcore.itemrarity.command.*;
import cl.nightcore.itemrarity.customstats.*;
import cl.nightcore.itemrarity.listener.*;
import cl.nightcore.itemrarity.loot.CustomDropsManager;
import cl.nightcore.itemrarity.statprovider.StatProviderManager;
import cl.nightcore.itemrarity.util.AnvilRepairUtil.ItemRepairManager;
import cl.nightcore.itemrarity.util.RateLimiter;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.registry.NamespacedRegistry;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ItemRarity extends JavaPlugin implements CommandExecutor {

    public static ItemRarity PLUGIN;
    public static Locale AURA_LOCALE;
    private final StatProviderManager statProviderManager = new StatProviderManager();

    public StatProviderManager getStatProviderManager(){
        return statProviderManager;
    }


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
        Objects.requireNonNull(getCommand("getxpmultiplier")).setExecutor(new GetExperienceMultiplierCommand());
        Objects.requireNonNull(getCommand("getstatpotion")).setExecutor(new GetPotionCommand());
        Objects.requireNonNull(getCommand("blockdata")).setExecutor(new NexoHelpCommand());

        getServer().getPluginManager().registerEvents(new IdentifyScrollListener(), this);
        getServer().getPluginManager().registerEvents(new CancelUsageInRecipesListener(), this);
        getServer().getPluginManager().registerEvents(new CustomDropsManager(), this);
        getServer().getPluginManager().registerEvents(new PlayerDisconnectListener(), this);
        getServer().getPluginManager().registerEvents(new PotionConsumeListener(),this);
        getServer().getPluginManager().registerEvents(new NexoSmithingListener(), this);

        AURA_LOCALE = AuraSkillsApi.get().getMessageManager().getDefaultLanguage();
        AuraSkillsApi auraSkills = AuraSkillsApi.get();
        NamespacedRegistry registry = auraSkills.useRegistry("itemrarity", getDataFolder());
        loadAuraSkillsCustoms(registry, auraSkills);
        loadRepairableItems();
        //initializeSmithingSystem();

        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            RateLimiter.getInstance().cleanupExpiredCooldowns();
        }, 6000L, 6000L);
    }

    private void loadAuraSkillsCustoms(NamespacedRegistry registry, AuraSkillsApi auraSkills) {
        registry.registerTrait(CustomTraits.DODGE_CHANCE);
        registry.registerTrait(CustomTraits.ATTACK_SPEED);
        registry.registerTrait(CustomTraits.HIT_CHANCE);
        registry.registerStat(CustomStats.EVASION);
        registry.registerStat(CustomStats.ACCURACY);
        registry.registerStat(CustomStats.DEXTERITY);
        auraSkills.getHandlers().registerTraitHandler(new DodgeChanceTrait(auraSkills));
        auraSkills.getHandlers().registerTraitHandler(new HitChanceTrait(auraSkills));
        auraSkills.getHandlers().registerTraitHandler(new AttackSpeedTraitHandler(auraSkills));
        saveResource("stats.yml", false);
    }

    private void loadRepairableItems(){
        // Crear el ItemRepairManager con referencia al plugin
        ItemRepairManager repairManager = new ItemRepairManager(this);

        // Cargar configuraciones desde Nexo
        repairManager.loadFromNexoConfigs();

        // Registrar el listener del yunque
        getServer().getPluginManager().registerEvents(new AnvilListener(repairManager), this);

        // Opcional: Mostrar estadísticas de carga
        Map<String, Object> stats = repairManager.getStatistics();
        getLogger().info("=== Estadísticas de Reparación ===");
        getLogger().info("Items configurados: " + stats.get("total_items"));
        getLogger().info("Materiales únicos: " + stats.get("unique_materials"));

        // Log detallado de materiales y su uso (solo si hay pocos materiales)
        @SuppressWarnings("unchecked")
        Map<String, Integer> materialUsage = (Map<String, Integer>) stats.get("material_usage");
        if (materialUsage.size() <= 10) {
            getLogger().info("Uso de materiales:");
            for (Map.Entry<String, Integer> entry : materialUsage.entrySet()) {
                getLogger().info("- " + entry.getKey() + ": " + entry.getValue() + " items");
            }
        }

        Objects.requireNonNull(getCommand("reloadrepair")).setExecutor(new ReloadRepairCommand(repairManager));
    }
}