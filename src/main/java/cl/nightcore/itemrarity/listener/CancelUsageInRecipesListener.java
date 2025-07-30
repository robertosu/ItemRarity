package cl.nightcore.itemrarity.listener;

import cl.nightcore.itemrarity.util.ItemUtil;
import com.nexomc.nexo.api.NexoItems;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

public class CancelUsageInRecipesListener implements Listener {
    @EventHandler
    public void onItemCraft(PrepareItemCraftEvent event) {
        ItemStack[] matrix = event.getInventory().getMatrix();
        for (ItemStack item : matrix) {
            if (!ItemUtil.getObjectType(item).equals(ItemUtil.ObjectType.NONE)|| NexoItems.exists(item)){
                event.getInventory().setResult(null);
                return;
            }
        }
    }
}