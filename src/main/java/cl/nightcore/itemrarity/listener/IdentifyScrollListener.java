package cl.nightcore.itemrarity.listener;

import cl.nightcore.itemrarity.GemManager;
import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.abstracted.SocketableItem;
import cl.nightcore.itemrarity.config.ItemConfig;
import cl.nightcore.itemrarity.item.GemObject;
import cl.nightcore.itemrarity.model.GemModel;
import cl.nightcore.itemrarity.model.GemRemoverModel;
import cl.nightcore.itemrarity.model.ItemUpgraderModel;
import cl.nightcore.itemrarity.abstracted.UpradeableItem;
import cl.nightcore.itemrarity.util.ItemUtil;
import cl.nightcore.itemrarity.util.PerformanceTimer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiFunction;

import static cl.nightcore.itemrarity.util.ItemUtil.isIdentified;

public class IdentifyScrollListener implements Listener {

    PerformanceTimer timer = new PerformanceTimer();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (event instanceof InventoryCreativeEvent) {
            return;
        }

        if (event.getSlotType() == InventoryType.SlotType.ARMOR
                || !event.getInventory().getType().equals(InventoryType.CRAFTING)
                || event.getSlotType() == InventoryType.SlotType.RESULT) {
            return;
        }

        ItemStack cursor = event.getCursor();
        ItemStack targetItem = event.getCurrentItem();

        if (isInvalidInteraction(cursor, targetItem)) {
            return;
        }
        ObjectType objectType = getObjectType(cursor);

        if (objectType == ObjectType.NONE) {
            return;
        } else if (!ItemUtil.isIdentifiable(targetItem) && !ItemUtil.isGem(targetItem)) {
            return;
        }
        handleInteraction(event, targetItem, cursor, objectType);
    }

    private boolean isInvalidInteraction(ItemStack cursor, ItemStack targetItem) {
        return cursor == null
                || cursor.getType().isAir()
                || targetItem == null
                || targetItem.getType().isAir();
    }

    private ObjectType getObjectType(ItemStack item) {
        if (ItemUtil.isIdentifyScroll(item)) return ObjectType.IDENTIFY_SCROLL;
        if (ItemUtil.isMagicObject(item)) return ObjectType.MAGIC_OBJECT;
        if (ItemUtil.isBlessingObject(item)) return ObjectType.BLESSING_OBJECT;
        if (ItemUtil.isRedemptionObject(item)) return ObjectType.REDEMPTION_OBJECT;
        if (ItemUtil.isGem(item)) return ObjectType.GEM;
        if (ItemUtil.isGemRemover(item)) return ObjectType.GEM_REMOVER;
        if (ItemUtil.isBlessingBall(item)) return ObjectType.BLESSING_BALL;
        if (ItemUtil.isItemUpgrader(item)) return ObjectType.ITEM_UPGRADER;
        if (ItemUtil.isSocketStone(item)) return ObjectType.SOCKET_STONE;
        return ObjectType.NONE;
    }

    private void handleInteraction(InventoryClickEvent event, ItemStack targetItem, ItemStack cursor, ObjectType type) {
        Player player = (Player) event.getWhoClicked();

        switch (type) {
            case IDENTIFY_SCROLL:
                handleIdentifyScroll(event, targetItem, cursor, player);
                break;
            case MAGIC_OBJECT:
                if (!isIdentified(targetItem)) {
                    sendErrorMessage(player, "Tu objeto debe estar identificado.", ItemConfig.REROLL_PREFIX);
                    event.setCancelled(true);
                    return;
                }
                handleObjectOperation(event, targetItem, cursor, player, ItemRarity::rollStats);
                break;
            case BLESSING_OBJECT:
                if (!isIdentified(targetItem)) {
                    sendErrorMessage(player, "Tu objeto debe estar identificado.", ItemConfig.BLESSING_PREFIX);
                    event.setCancelled(true);
                    return;
                }
                handleObjectOperation(event, targetItem, cursor, player, ItemRarity::rerollLowestStat);
                break;
            case REDEMPTION_OBJECT:
                if (!isIdentified(targetItem)) {
                    sendErrorMessage(player, "Tu objeto debe estar identificado.", ItemConfig.REDEMPTION_PREFIX);
                    event.setCancelled(true);
                    return;
                }
                handleObjectOperation(event, targetItem, cursor, player, ItemRarity::rerollAllStatsExceptHighest);
                break;
            case GEM:
                if (!isIdentified(targetItem)) {
                    sendErrorMessage(player, "Tu objeto debe estar identificado.", ItemConfig.GEMSTONE_PREFIX);
                    event.setCancelled(true);
                    return;
                }
                handleGemInsertion(event, targetItem, cursor, player);
                break;
            case GEM_REMOVER:
                if (!isIdentified(targetItem)) {
                    sendErrorMessage(player, "Tu objeto debe estar identificado.", ItemConfig.GEM_REMOVER_PREFIX);
                    event.setCancelled(true);
                    return;
                }
                handleGemRemoval(event, targetItem, cursor, player);
                break;
            case BLESSING_BALL:
                if (!isIdentified(targetItem)) {
                    sendErrorMessage(player, "Tu objeto debe estar identificado.", ItemConfig.BLESSING_BALL_PREFIX);
                    event.setCancelled(true);
                    return;
                }
                handleBlessingBall(event, targetItem, cursor, player);
                break;
            case ITEM_UPGRADER:
                if (ItemUtil.isGem(targetItem)) {
                    handleItemUpgrade(event, targetItem, cursor, player);
                    return;
                }

                if (!isIdentified(targetItem)) {
                    sendErrorMessage(player, "Tu objeto debe estar identificado.", ItemConfig.ITEM_UPGRADER_PREFIX);
                    event.setCancelled(true);
                    return;
                }

                handleItemUpgrade(event, targetItem, cursor, player);
                break;
            case SOCKET_STONE:
                if (!isIdentified(targetItem)) {
                    sendErrorMessage(player, "Tu objeto debe estar identificado.", ItemConfig.GEMSTONE_PREFIX);
                    event.setCancelled(true);
                    return;
                }
                handleSocketStone(event, targetItem, cursor, player);
                break;
        }
    }

    private void handleSocketStone(InventoryClickEvent event, ItemStack targetItem, ItemStack cursor, Player player) {
        SocketableItem item = new SocketableItem(targetItem);
        if (item.addSocket(player)) {
            event.setCurrentItem(item);
            consumeItem(event, cursor);
            event.setCancelled(true);
        } else {
            event.setCancelled(true);
        }
    }

    private void handleItemUpgrade(InventoryClickEvent event, ItemStack targetItem, ItemStack cursor, Player player) {
        if (!ItemUtil.isGem(targetItem)) {
            UpradeableItem item = new UpradeableItem(targetItem);
            ItemUpgraderModel upgrader = new ItemUpgraderModel(cursor);

            if (item.incrementLevel(player, upgrader)) {
                event.setCurrentItem(item);
                consumeItem(event, cursor);
                event.setCancelled(true);
            } else {
                event.setCancelled(true);
            }
        } else if (ItemUtil.isGem(targetItem)) {

            GemManager manager = new GemManager();
            ItemUpgraderModel upgrader = new ItemUpgraderModel(cursor);
            ItemStack newitem = manager.upgradeGem(player, targetItem, upgrader);

            if (!newitem.equals(targetItem)) {
                event.setCurrentItem(newitem);
                consumeItem(event, cursor);
                event.setCancelled(true);
            } else {
                event.setCancelled(true);
            }
        }
    }

    private void handleBlessingBall(InventoryClickEvent event, ItemStack targetItem, ItemStack cursor, Player player) {
        SocketableItem item = new SocketableItem(targetItem);
        if (item.addRandomMissingStat(player)) {
            event.setCurrentItem(item);
            consumeItem(event, cursor);
            event.setCancelled(true);
        } else {
            event.setCancelled(true);
        }
    }

    private void handleGemRemoval(InventoryClickEvent event, ItemStack targetItem, ItemStack cursor, Player player) {
        SocketableItem item = new SocketableItem(targetItem);
        GemRemoverModel remover = new GemRemoverModel(cursor);

        if (item.extractAllGems(player, remover)) {
            event.setCurrentItem(item);
            consumeItem(event, cursor);
            event.setCancelled(true);
        } else {
            event.setCancelled(true);
        }
    }

    private void handleGemInsertion(InventoryClickEvent event, ItemStack targetItem, ItemStack cursor, Player player) {
        GemModel gem = new GemModel(cursor);
        SocketableItem item = new SocketableItem(targetItem);

        int result = item.installGem(gem, player);
        if (result == 0) {
            consumeItem(event, cursor);
            event.setCurrentItem(item);
            event.setCancelled(true);
        } else if (result == 1) {
            event.setCancelled(true);
        } else {
            consumeItem(event, cursor);
            event.setCancelled(true);
        }
    }

    private void handleIdentifyScroll(
            InventoryClickEvent event, ItemStack targetItem, ItemStack cursor, Player player) {
        if (isIdentified(targetItem)) {
            sendErrorMessage(player, "Este item ya está identificado.", ItemConfig.PLUGIN_PREFIX);
            event.setCancelled(true);
            return;
        }

        consumeItem(event, cursor);
        event.setCurrentItem(ItemRarity.identifyItem(player, targetItem));
        event.setCancelled(true);
    }

    private void handleObjectOperation(
            InventoryClickEvent event,
            ItemStack targetItem,
            ItemStack cursor,
            Player player,
            BiFunction<Player, ItemStack, ItemStack> operation) {
        timer.start();
        consumeItem(event, cursor);
        event.setCurrentItem(operation.apply(player, targetItem));
        event.setCancelled(true);
        timer.stop();
        System.out.println(timer.getFormattedDuration());
    }

    private void consumeItem(InventoryClickEvent event, ItemStack cursor) {
        if (cursor.getAmount() > 1) {
            cursor.setAmount(cursor.getAmount() - 1);
        } else {
            event.getWhoClicked().setItemOnCursor(null);
        }
    }

    private void sendErrorMessage(Player player, String message, Component prefix) {
        player.sendMessage(prefix.append(Component.text(message).color(NamedTextColor.RED)));
    }

    private enum ObjectType {
        IDENTIFY_SCROLL,
        MAGIC_OBJECT,
        BLESSING_OBJECT,
        REDEMPTION_OBJECT,
        GEM,
        GEM_REMOVER,
        BLESSING_BALL,
        ITEM_UPGRADER,
        SOCKET_STONE,
        NONE
    }
}