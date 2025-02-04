package cl.nightcore.itemrarity.listener;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.abstracted.SocketableItem;
import cl.nightcore.itemrarity.config.ItemConfig;
import cl.nightcore.itemrarity.model.GemModel;
import cl.nightcore.itemrarity.model.GemRemoverModel;
import cl.nightcore.itemrarity.model.ItemUpgraderModel;
import cl.nightcore.itemrarity.type.RolledAbstract;
import cl.nightcore.itemrarity.util.ItemUtil;
import cl.nightcore.itemrarity.util.PerformanceTimer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
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
        ItemStack cursor = event.getCursor();
        ItemStack targetItem = event.getCurrentItem();

        if (isInvalidInteraction(cursor, targetItem)) {
            return;
        }
        ObjectType objectType = getObjectType(cursor);
        if (objectType == ObjectType.NONE) {
            return;
        }
        else if (!ItemUtil.isIdentifiable(targetItem)) {
            return;
        }
        handleInteraction(event, targetItem, cursor, objectType);
    }

    private boolean isInvalidInteraction(ItemStack cursor, ItemStack targetItem) {
        return cursor == null ||
                cursor.getType().isAir() ||
                targetItem == null ||
                targetItem.getType().isAir();
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
        return ObjectType.NONE;
    }

    private void handleInteraction(InventoryClickEvent event, ItemStack targetItem, ItemStack cursor, ObjectType type) {
        Player player = (Player) event.getWhoClicked();


        // Para gemas, verificar si el item está identificado
        if (type == ObjectType.GEM) {
            if (!isIdentified(targetItem)) {
                sendErrorMessage(player, "Tu objeto debe estar identificado para insertar gemas.", ItemConfig.GEMSTONE_PREFIX);
                event.setCancelled(true);
                return;
            }
        }
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
                handleObjectOperation(event, targetItem, cursor, player,
                        ItemRarity::rollStats);
                break;
            case BLESSING_OBJECT:
                if (!isIdentified(targetItem)) {
                    sendErrorMessage(player, "Tu objeto debe estar identificado.", ItemConfig.BLESSING_PREFIX);
                    event.setCancelled(true);
                    return;
                }
                handleObjectOperation(event, targetItem, cursor, player,
                        ItemRarity::rerollLowestStat);
                break;
            case REDEMPTION_OBJECT:
                if (!isIdentified(targetItem)) {
                    sendErrorMessage(player, "Tu objeto debe estar identificado.", ItemConfig.REDEMPTION_PREFIX);
                    event.setCancelled(true);
                    return;
                }
                handleObjectOperation(event, targetItem, cursor, player,
                        ItemRarity::rerollAllStatsExceptHighest);
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
                if (!isIdentified(targetItem)) {
                    sendErrorMessage(player, "Tu objeto debe estar identificado.", ItemConfig.ITEM_UPGRADER_PREFIX);
                    event.setCancelled(true);
                    return;
                }
                handleItemUpgrade(event, targetItem, cursor, player);

        }
    }

    private void handleItemUpgrade(InventoryClickEvent event, ItemStack targetItem,ItemStack cursor, Player player){
        RolledAbstract item = new RolledAbstract(targetItem);
        ItemUpgraderModel upgrader = new ItemUpgraderModel(cursor);

        if (item.incrementLevel(player,upgrader)) {
            event.setCurrentItem(item);
            consumeItem(event, cursor);
            event.setCancelled(true);
        }else {
            event.setCancelled(true);
        }
    }

    private void handleBlessingBall(InventoryClickEvent event, ItemStack targetItem, ItemStack cursor, Player player) {
        SocketableItem item = new SocketableItem(targetItem);
        if (item.addRandomMissingStat(player)) {
            event.setCurrentItem(item);
            consumeItem(event, cursor);
            event.setCancelled(true);
        }else {
            event.setCancelled(true);
        }
    }

    private void handleGemRemoval(InventoryClickEvent event, ItemStack targetItem,ItemStack cursor, Player player){
        SocketableItem item = new SocketableItem(targetItem);
        GemRemoverModel remover = new GemRemoverModel(cursor);

        if (item.extractAllGems(player,remover)){
            event.setCurrentItem(item);
            consumeItem(event, cursor);
            event.setCancelled(true);
        }else {
            event.setCancelled(true);
        }

    }
    private void handleGemInsertion(InventoryClickEvent event, ItemStack targetItem, ItemStack cursor, Player player) {
        GemModel gem = new GemModel(cursor);
        SocketableItem item = new SocketableItem(targetItem);

        // Intentar instalar la gema
        if (item.installGem(gem, player)) {
            consumeItem(event, cursor);
            event.setCurrentItem(item);
        } else {
            event.setCancelled(true);
        }
    }

    private void handleIdentifyScroll(InventoryClickEvent event, ItemStack targetItem,
                                      ItemStack cursor, Player player) {
        if (isIdentified(targetItem)) {
            sendErrorMessage(player, "Este item ya está identificado.", ItemConfig.PLUGIN_PREFIX);
            event.setCancelled(true);
            return;
        }

        consumeItem(event, cursor);
        event.setCurrentItem(ItemRarity.identifyItem(player, targetItem));
        event.setCancelled(true);
    }

    private void handleObjectOperation(InventoryClickEvent event, ItemStack targetItem,
                                       ItemStack cursor, Player player,
                                       BiFunction<Player, ItemStack, ItemStack> operation) {
        timer.start();
        consumeItem(event, cursor);
        event.setCurrentItem(operation.apply(player, targetItem));
        event.setCancelled(true);
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
        player.sendMessage(prefix.append(
                Component.text(message).color(NamedTextColor.RED)
        ));
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
        NONE
    }
}