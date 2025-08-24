package cl.nightcore.itemrarity.listener;

import com.nexomc.nexo.api.NexoItems;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;

public class NexoSmithingListener implements Listener {



    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSmithing(InventoryClickEvent e) {
        if (e.getInventory() instanceof SmithingInventory inv) {
            /*System.out.println(e.getSlot());
            System.out.println(e.getAction());*/
            if (e.getSlot() == 0 && NexoItems.exists(e.getCursor()) && e.getSlotType().equals(InventoryType.SlotType.CRAFTING)) {
                e.setCancelled(true);

                ItemStack cursor = e.getCursor().clone();
                ItemStack current = e.getCurrentItem() != null ? e.getCurrentItem().clone() : null;

                inv.setInputTemplate(cursor);
                e.getWhoClicked().setItemOnCursor(current);
            }
        }
    }
}
