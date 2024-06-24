package cl.nightcore.itemrarity.listener;

import cl.nightcore.itemrarity.ItemRarity;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiFunction;

import static cl.nightcore.itemrarity.ItemRarity.isIdentified;

public class IdentifyScrollListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event instanceof InventoryCreativeEvent)) {
            ItemStack cursor = event.getCursor();
            ItemStack targetItem = event.getCurrentItem();
            if (cursor!=null && !cursor.getType().isAir() && targetItem!=null && !targetItem.getType().isAir()){
                if (ItemRarity.isIdentifiable(targetItem)) {
                    if (!isTryingToInteractWithSameObjectType(targetItem)){
                        if (ItemRarity.isIdentifyScroll(cursor)) {
                            doIdentifyScroll(event, targetItem, cursor, ItemRarity.getPluginPrefix());
                        }
                        else if (ItemRarity.isMagicObject(cursor)) {
                            doMagicObject(event, targetItem, cursor, ItemRarity.getRerollPrefix());
                        }
                        else if (ItemRarity.isBlessingObject(cursor)) {
                            doBlessingObject(event, targetItem, cursor, ItemRarity.getBlessingPrefix());
                        }
                        else if (ItemRarity.isRedemptionObject(cursor)) {
                            doRedemptionObject(event, targetItem, cursor, ItemRarity.getRedemptionPrefix());
                        }
                    }
                }
            }
        }
    }
    private Boolean isTryingToInteractWithSameObjectType(ItemStack targetItem){
        return (!ItemRarity.isIdentifyScroll(targetItem) && !ItemRarity.isBlessingObject(targetItem) && !ItemRarity.isMagicObject(targetItem) && ItemRarity.isRedemptionObject(targetItem));
    }
    private void doObjectOperation(InventoryClickEvent event, ItemStack targetItem, ItemStack cursor,String prefix, BiFunction<Player, ItemStack, ItemStack> operationFunction) {
        if (isIdentified(targetItem) && cursor != null && !cursor.getType().isAir()) {
            Player p = (Player) event.getWhoClicked();
            if (cursor.getAmount() > 1) {
                cursor.setAmount(cursor.getAmount() - 1);
            } else {
                event.getWhoClicked().setItemOnCursor(null); // Eliminar el último objeto del cursor
            }
            event.setCurrentItem(operationFunction.apply(p, targetItem)); // Actualizar los stats del objeto
            event.setCancelled(true);
        } else {
            Player p = (Player) event.getWhoClicked();
            p.sendMessage(prefix + ChatColor.RED + "Tu objeto debe estar identificado.");
            event.setCancelled(true);
        }
    }
    private void doRedemptionObject(InventoryClickEvent event, ItemStack targetItem, ItemStack cursor, String prefix) {
        doObjectOperation(event, targetItem, cursor, prefix, ItemRarity::rerollAllStatsExceptHighest);
    }

    private void doBlessingObject(InventoryClickEvent event, ItemStack targetItem, ItemStack cursor, String prefix) {
        doObjectOperation(event, targetItem, cursor, prefix, ItemRarity::rerollLowestStat);
    }

    private void doMagicObject(InventoryClickEvent event, ItemStack targetItem, ItemStack cursor, String prefix) {
        doObjectOperation(event, targetItem, cursor, prefix, ItemRarity::rollStats);
    }

    private void doIdentifyScroll(InventoryClickEvent event, ItemStack targetItem, ItemStack cursor, String prefix) {
        if (!isIdentified(targetItem) && cursor != null && !cursor.getType().isAir()) {
            Player p = (Player) event.getWhoClicked();
            if (cursor.getAmount() > 1) {
                cursor.setAmount(cursor.getAmount() - 1);
            } else {
                event.getWhoClicked().setItemOnCursor(null); // Eliminar el último scroll del cursor
            }
            event.setCurrentItem(ItemRarity.identifyItem(p, targetItem)); // No eliminar el objeto identificado
            event.setCancelled(true);
        } else if (isIdentified(targetItem) && event.getCursor() != null && !event.getCursor().getType().isAir()) {
            Player p = (Player) event.getWhoClicked();
            p.sendMessage(prefix + ChatColor.RED + "Este item ya está identificado.");
            event.setCancelled(true);
        }
    }
}