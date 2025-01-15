package cl.nightcore.itemrarity.listener;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.abstracted.SocketableItem;
import cl.nightcore.itemrarity.config.ItemConfig;
import cl.nightcore.itemrarity.model.GemModel;
import cl.nightcore.itemrarity.type.IdentifiedAbstract;
import cl.nightcore.itemrarity.util.ItemUtil;
import cl.nightcore.itemrarity.util.PerformanceTimer;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import static cl.nightcore.itemrarity.util.ItemUtil.*;

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
        return ObjectType.NONE;
    }

    private void handleInteraction(InventoryClickEvent event, ItemStack targetItem, ItemStack cursor, ObjectType type) {
        Player player = (Player) event.getWhoClicked();

        // Para objetos que no son gemas, verificar si el item está identificado
        if (type != ObjectType.IDENTIFY_SCROLL && type != ObjectType.GEM && !isIdentified(targetItem)) {
            sendErrorMessage(player, "Tu objeto debe estar identificado.", ItemConfig.PLUGIN_PREFIX);
            event.setCancelled(true);
            return;
        }
        // Para gemas, verificar si el item está identificado
        if (type == ObjectType.GEM) {
            if (!isIdentified(targetItem)) {
                sendErrorMessage(player, "Tu objeto debe estar identificado para insertar gemas.", ItemConfig.PLUGIN_PREFIX);
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
                        ItemRarity::rollStats);
                break;
            case BLESSING_OBJECT:
                handleObjectOperation(event, targetItem, cursor, player,
                        ItemRarity::rerollLowestStat);
                break;
            case REDEMPTION_OBJECT:
                handleObjectOperation(event, targetItem, cursor, player,
                        ItemRarity::rerollAllStatsExceptHighest);
                break;
            case GEM:
                handleGemInsertion(event, targetItem, cursor, player);
                break;
            case GEM_REMOVER:
                handleGemRemoval(event, targetItem, cursor, player);
                break;

        }
    }
    private void handleGemRemoval(InventoryClickEvent event, ItemStack targetItem,ItemStack cursor, Player player){
        SocketableItem item = new IdentifiedAbstract(targetItem);
        item.extractAllGems(player);
        event.setCurrentItem(item);
        consumeItem(event, cursor);
        event.setCancelled(true);
    }
    private void handleGemInsertion(InventoryClickEvent event, ItemStack targetItem, ItemStack cursor, Player player) {
        GemModel gem = new GemModel(cursor);
        SocketableItem item = new IdentifiedAbstract(targetItem);
        // Verificar si hay espacios disponibles
        if (!item.hasAvailableSockets()) {
            sendErrorMessage(player, "Este objeto no tiene espacios disponibles para gemas.", ItemConfig.GEMSTONE_PREFIX);
            event.setCancelled(true);
            return;
        }
        //Verificar que no haya una gema del mismo tipo
        if (item.hasGemWithStat(gem.getStat())) {
            sendErrorMessage(player, "El objeto ya tiene una gema de este tipo.", ItemConfig.GEMSTONE_PREFIX);
            event.setCancelled(true);
            return;
        }
        // Verificar si la gema es compatible con las stats posibles para un item
        if (!gem.isCompatible(getStatProvider(item))) {
            List<Component> availableStatsComponents = new ArrayList<>();
            // Construir la lista de componentes
            getStatProvider(item)
                    .getAvailableStats()
                    .forEach(stat -> availableStatsComponents.add(
                            Component.text(stat.getDisplayName(AuraSkillsApi.get().getMessageManager().getDefaultLanguage()))
                                    .color(getColorOfStat(stat))
                    ));
            // Mostrar gemas compatibles
            Component baseMessage = Component.text("El tipo de objeto no es compatible con la gema, intenta con: ")
                    .color(NamedTextColor.RED);
            for (int i = 0; i < availableStatsComponents.size(); i++) {
                baseMessage = baseMessage.append(availableStatsComponents.get(i));
                if (i < availableStatsComponents.size() - 1) {
                    baseMessage = baseMessage.append(Component.text(", ").color(NamedTextColor.GRAY));
                }
            }
            sendErrorMessage(player, baseMessage, ItemConfig.GEMSTONE_PREFIX);
            event.setCancelled(true);
            return;
        }
        // Intentar instalar la gema
        if (item.installGem(gem)) {
            consumeItem(event, cursor);
            event.setCurrentItem(item);
            player.sendMessage(ItemConfig.GEMSTONE_PREFIX
                    .append(Component.text("¡Gema instalada con éxito!")
                            .color(NamedTextColor.GREEN)));
        } else {
            sendErrorMessage(player, "No se pudo instalar la gema.", ItemConfig.GEMSTONE_PREFIX);
        }

        event.setCancelled(true);
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
    private void sendErrorMessage(Player player, Component message, Component prefix) {
        player.sendMessage(prefix.append(
                message
        ));
    }

    private enum ObjectType {
        IDENTIFY_SCROLL,
        MAGIC_OBJECT,
        BLESSING_OBJECT,
        REDEMPTION_OBJECT,
        GEM,
        GEM_REMOVER,
        NONE
    }
}