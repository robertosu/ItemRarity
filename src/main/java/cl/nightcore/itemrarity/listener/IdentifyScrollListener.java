package cl.nightcore.itemrarity.listener;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.abstracted.SocketableItem;
import cl.nightcore.itemrarity.model.GemModel;
import cl.nightcore.itemrarity.type.IdentifiedArmor;
import cl.nightcore.itemrarity.type.IdentifiedWeapon;
import cl.nightcore.itemrarity.util.ItemUtil;
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

    private enum ObjectType {
        IDENTIFY_SCROLL,
        MAGIC_OBJECT,
        BLESSING_OBJECT,
        REDEMPTION_OBJECT,
        GEM,
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

        ObjectType objectType = getObjectType(cursor);
        if (objectType == ObjectType.NONE) {
            return;
        }

        // Si es una gema, verificar si el item es socketable
        if (objectType == ObjectType.GEM) {
            if (!ItemUtil.isIdentified(targetItem)) {
                sendErrorMessage((Player) event.getWhoClicked(),
                        "Este objeto no acepta gemas, primero debes usar pergamino de identificación.",
                        ItemRarity.getPluginPrefix());
                event.setCancelled(true);
                return;
            }
        }
        // Para otros objetos, verificar si es identificable
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
        return ObjectType.NONE;
    }

    private void handleInteraction(InventoryClickEvent event, ItemStack targetItem, ItemStack cursor, ObjectType type) {
        Player player = (Player) event.getWhoClicked();

        // Para objetos que no son gemas, verificar si el item está identificado
        if (type != ObjectType.IDENTIFY_SCROLL && type != ObjectType.GEM && !isIdentified(targetItem)) {
            sendErrorMessage(player, "Tu objeto debe estar identificado.", ItemRarity.getPluginPrefix());
            event.setCancelled(true);
            return;
        }

        // Para gemas, verificar si el item está identificado
        if (type == ObjectType.GEM) {
            if (!isIdentified(targetItem)) {
                sendErrorMessage(player, "Tu objeto debe estar identificado para insertar gemas.", ItemRarity.getPluginPrefix());
                event.setCancelled(true);
                return;
            }
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
            case GEM:
                handleGemInsertion(event, targetItem, cursor, player);
                break;
        }
    }

    private void handleGemInsertion(InventoryClickEvent event, ItemStack targetItem, ItemStack cursor, Player player) {
        GemModel gem = new GemModel(cursor);

        // Verificar si es una gema válida
        if (!gem.isValid()) {
            sendErrorMessage(player, "Esta gema no es válida.", ItemRarity.getPluginPrefix());
            event.setCancelled(true);
            return;
        }

        // Crear instancia de SocketableItem según el tipo
        SocketableItem socketableItem = ItemUtil.getItemType(targetItem).equals("Weapon") ?
                new IdentifiedWeapon(targetItem) : new IdentifiedArmor(targetItem);

        // Verificar si hay espacios disponibles
        if (!socketableItem.hasAvailableSockets()) {
            sendErrorMessage(player, "Este objeto no tiene espacios disponibles para gemas.", ItemRarity.getPluginPrefix());
            event.setCancelled(true);
            return;
        }

        // Intentar instalar la gema
        if (socketableItem.installGem(gem)) {
            consumeItem(event, cursor);
            event.setCurrentItem(socketableItem);
            player.sendMessage(ItemRarity.getPluginPrefix()
                    .append(Component.text("¡Gema instalada con éxito!")
                            .color(NamedTextColor.GREEN)));
        } else {
            sendErrorMessage(player, "No se pudo instalar la gema.", ItemRarity.getPluginPrefix());
        }

        event.setCancelled(true);
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