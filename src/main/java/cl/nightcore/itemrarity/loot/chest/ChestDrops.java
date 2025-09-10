package cl.nightcore.itemrarity.loot.chest;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.item.ExperienceMultiplier;
import cl.nightcore.itemrarity.item.ItemUpgrader;
import cl.nightcore.itemrarity.item.gem.GemObject;
import cl.nightcore.itemrarity.item.gem.GemRemover;
import cl.nightcore.itemrarity.item.potion.StatPotion;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChestDrops {

    private final List<ChestDropConfig> chestDrops = new ArrayList<>();
    private final Map<String, List<ChestDropConfig>> specificChestDrops = new HashMap<>();


    public ChestDrops(){
        loadChestDrops();
    }

    private void loadChestDrops() {
        addChestDrop(ItemRarity.PLUGIN.getItemRegistry().getBlessingObject(), 0.1, 1, 1);
        addChestDrop(ItemRarity.PLUGIN.getItemRegistry().getIdentifyScroll(), 0.2, 1, 2);
        addChestDrop(ItemRarity.PLUGIN.getItemRegistry().getMagicObject(), 0.3, 1, 3);
        addChestDrop(ItemRarity.PLUGIN.getItemRegistry().getRedemptionObject(), 0.1, 1, 1);
        addChestDrop(ItemRarity.PLUGIN.getItemRegistry().getBlessingBall(), 0.05,1, 3);
        addChestDrop(ItemRarity.PLUGIN.getItemRegistry().getSocketStone(), 0.1, 1, 1);
        addChestDrop(ItemRarity.PLUGIN.getItemRegistry().getDarkOpalUpgrade(), 0.05,1,2);

        addArrayToChestDrops(ItemRarity.PLUGIN.getItemRegistry().getAllGems().values().toArray(new GemObject[0]), 0.002,1,1);
        addArrayToChestDrops(ItemRarity.PLUGIN.getItemRegistry().getAllPotions().values().toArray(new StatPotion[0]), 0.002,1,1);
        addArrayToChestDrops(ItemRarity.PLUGIN.getItemRegistry().getAllUpgraders().values().toArray(new ItemUpgrader[0]), 0.004,1,1);
        addArrayToChestDrops(ItemRarity.PLUGIN.getItemRegistry().getAllXpMultipliers().values().toArray(new ExperienceMultiplier[0]), 0.001,1,1);
        addArrayToChestDrops(ItemRarity.PLUGIN.getItemRegistry().getAllGemRemovers().values().toArray(new GemRemover[0]), 0.1,1,2);


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
