package cl.nightcore.itemrarity.loot.chest;

import cl.nightcore.itemrarity.item.ExperienceMultiplier;
import cl.nightcore.itemrarity.item.ItemUpgrader;
import cl.nightcore.itemrarity.item.gem.GemObject;
import cl.nightcore.itemrarity.item.gem.GemRemover;
import cl.nightcore.itemrarity.item.potion.StatPotion;
import cl.nightcore.itemrarity.loot.Items;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChestDrops {

    private final List<ChestDropConfig> chestDrops = new ArrayList<>();
    private final Map<String, List<ChestDropConfig>> specificChestDrops = new HashMap<>();

    private static ChestDrops instance;


    private ChestDrops(){
        loadChestDrops();
    }

    public static synchronized ChestDrops instance(){
        if (instance == null) {
            instance = new ChestDrops();
        }
        return instance;
    }

    private void loadChestDrops() {
        addChestDrop(Items.instance().getBlessingObject(), 0.1, 1, 1);
        addChestDrop(Items.instance().getIdentifyScroll(), 0.2, 1, 2);
        addChestDrop(Items.instance().getMagicObject(), 0.3, 1, 3);
        addChestDrop(Items.instance().getRedemptionObject(), 0.1, 1, 1);
        addChestDrop(Items.instance().getBlessingBall(), 0.05,1, 3);
        addChestDrop(Items.instance().getSocketStone(), 0.1, 1, 1);

        addArrayToChestDrops(Items.instance().getAllGems().values().toArray(new GemObject[0]), 0.002,1,1);
        addArrayToChestDrops(Items.instance().getAllPotions().values().toArray(new StatPotion[0]), 0.002,1,1);
        addArrayToChestDrops(Items.instance().getAllUpgraders().values().toArray(new ItemUpgrader[0]), 0.004,1,1);
        addArrayToChestDrops(Items.instance().getAllXpMultipliers().values().toArray(new ExperienceMultiplier[0]), 0.001,1,1);
        addArrayToChestDrops(Items.instance().getAllGemRemovers().values().toArray(new GemRemover[0]), 0.1,1,2);


    }

    public void addChestDrop(ItemStack item, double chance, int minAmount, int maxAmount) {
        chestDrops.add(new ChestDropConfig(item, chance, minAmount, maxAmount));
    }

    public void addArrayToChestDrops(ItemStack[] items, double chance, int minAmount, int maxAmount) {
        for (ItemStack item : items){
            chestDrops.add(new ChestDropConfig(item, chance, minAmount, maxAmount));
        }
    }

    public void addSpecificChestDrop(String lootTableKey, ItemStack item, double chance, int minAmount, int maxAmount) {
        ChestDropConfig chestDropConfig = new ChestDropConfig(item, chance, minAmount, maxAmount);
        specificChestDrops.computeIfAbsent(lootTableKey, k -> new ArrayList<>()).add(chestDropConfig);
    }

    public List<ChestDropConfig> getChestDrops() {
        return chestDrops;
    }

    public Map<String, List<ChestDropConfig>> getSpecificChestDrops() {
        return specificChestDrops;
    }

}
