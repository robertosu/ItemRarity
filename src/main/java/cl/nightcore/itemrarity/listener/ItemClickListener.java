package cl.nightcore.itemrarity.listener;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.abstracted.IdentifiedItem;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.mechanics.provided.misc.custom.listeners.PickupListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;

public class ItemClickListener implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        //method: update the fake lore of the items we are interested in and make sure they have hide_attributes flag
        ItemStack clickedItem = event.getCurrentItem();
       // removido del if de abajo OraxenItems.getIdByItem(clickedItem)!=null&&
        if (ItemRarity.isNotEmpty(clickedItem)) {
            if (ItemRarity.isIdentifiable(clickedItem) && !ItemRarity.getItemType(clickedItem).equals("Armor")) {
                IdentifiedItem.attributesDisplayInLore(clickedItem);
                // No es necesario actualizar el ítem en el inventario, ya que se modifica directamente
            }
        }
    }
    //Comentado por que era una solucion a medias para oraxen sobreescribiendo flag Hide.Attributes del item;
    /*@EventHandler
    public void onItemPickup(InventoryPickupItemEvent event) {
        //method: update the fake lore of the items we are interested in and make sure they have hide_attributes flag
        ItemStack pickedItem = event.getItem().getItemStack();
        if (OraxenItems.getIdByItem(pickedItem)!=null&& ItemRarity.isIdentifiable(pickedItem) && !ItemRarity.getItemType(pickedItem).equals("Armor")) {
            IdentifiedItem.attributesDisplayInLore(pickedItem);
            // No es necesario actualizar el ítem en el inventario, ya que se modifica directamente
        }
    }*/
}