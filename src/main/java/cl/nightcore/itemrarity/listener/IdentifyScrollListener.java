package cl.nightcore.itemrarity.listener;

import cl.nightcore.itemrarity.ItemRarity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiFunction;

import static cl.nightcore.itemrarity.ItemRarity.isIdentified;

public class IdentifyScrollListener implements Listener {

    private enum ObjectType {
        IDENTIFY_SCROLL,
        MAGIC_OBJECT,
        BLESSING_OBJECT,
        REDEMPTION_OBJECT,
        NONE
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event instanceof InventoryCreativeEvent) {
            return;
        }

        ItemStack cursor = event.getCursor();
        ItemStack targetItem = event.getCurrentItem();

        if (isInvalidInteraction(cursor, targetItem)) {
            return;
        }

        if (!ItemRarity.isIdentifiable(targetItem)) {
            return;
        }

        ObjectType objectType = getObjectType(cursor);
        if (objectType == ObjectType.NONE) {
            return;
        }

        handleInteraction(event, targetItem, cursor, objectType);
    }

    private boolean isInvalidInteraction(ItemStack cursor, ItemStack targetItem) {
        return cursor.getType().isAir() ||
                targetItem == null ||
                targetItem.getType().isAir();
    }

    private ObjectType getObjectType(ItemStack item) {
        if (ItemRarity.isIdentifyScroll(item)) return ObjectType.IDENTIFY_SCROLL;
        if (ItemRarity.isMagicObject(item)) return ObjectType.MAGIC_OBJECT;
        if (ItemRarity.isBlessingObject(item)) return ObjectType.BLESSING_OBJECT;
        if (ItemRarity.isRedemptionObject(item)) return ObjectType.REDEMPTION_OBJECT;
        return ObjectType.NONE;
    }

    private void handleInteraction(InventoryClickEvent event, ItemStack targetItem, ItemStack cursor, ObjectType type) {
        Player player = (Player) event.getWhoClicked();

        if (type != ObjectType.IDENTIFY_SCROLL && !isIdentified(targetItem)) {
            sendErrorMessage(player, "Tu objeto debe estar identificado.", ItemRarity.getPluginPrefix());
            event.setCancelled(true);
            return;
        }

        switch (type) {
            case IDENTIFY_SCROLL:
                handleIdentifyScroll(event, targetItem, cursor, player);
                break;
            case MAGIC_OBJECT:
                handleObjectOperation(event, targetItem, cursor, player,
                        ItemRarity.getRerollPrefix(), ItemRarity::rollStats);
                break;
            case BLESSING_OBJECT:
                handleObjectOperation(event, targetItem, cursor, player,
                        ItemRarity.getBlessingPrefix(), ItemRarity::rerollLowestStat);
                break;
            case REDEMPTION_OBJECT:
                handleObjectOperation(event, targetItem, cursor, player,
                        ItemRarity.getRedemptionPrefix(), ItemRarity::rerollAllStatsExceptHighest);
                break;
        }
    }

    private void handleIdentifyScroll(InventoryClickEvent event, ItemStack targetItem,
                                      ItemStack cursor, Player player) {
        if (isIdentified(targetItem)) {
            sendErrorMessage(player, "Este item ya está identificado.", ItemRarity.getPluginPrefix());
            event.setCancelled(true);
            return;
        }

        consumeItem(event, cursor);
        event.setCurrentItem(ItemRarity.identifyItem(player, targetItem));
        event.setCancelled(true);
    }

    private void handleObjectOperation(InventoryClickEvent event, ItemStack targetItem,
                                       ItemStack cursor, Player player, Component prefix,
                                       BiFunction<Player, ItemStack, ItemStack> operation) {
        consumeItem(event, cursor);
        event.setCurrentItem(operation.apply(player, targetItem));
        event.setCancelled(true);
    }

    private void consumeItem(InventoryClickEvent event, ItemStack cursor) {
        if (cursor.getAmount() > 1) {
            cursor.setAmount(cursor.getAmount() - 1);
        } else {
            event.getWhoClicked().setItemOnCursor(null);
        }
    }

    private void sendErrorMessage(Player player, String message, Component prefix) {
        player.sendMessage(prefix.append(
                Component.text(message).color(NamedTextColor.RED)
        ));
    }
}