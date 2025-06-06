package cl.nightcore.itemrarity.listener;

import cl.nightcore.itemrarity.util.ItemUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

public class CancelUsageInRecipesListener implements Listener {
    @EventHandler
    public void onItemCraft(PrepareItemCraftEvent event) {
        ItemStack[] matrix = event.getInventory().getMatrix();
        for (ItemStack item : matrix) {
            if (ItemUtil.isMagicObject(item)
                    || ItemUtil.isIdentifyScroll(item)
                    || ItemUtil.isBlessingObject(item)
                    || ItemUtil.isRedemptionObject(item)
                    || ItemUtil.isGem(item)) {
                event.getInventory().setResult(null);
                return;
            }
        }
    }
}