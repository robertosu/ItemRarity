package cl.nightcore.itemrarity.listener;

import cl.nightcore.itemrarity.GemManager;
import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.abstracted.SocketableItem;
import cl.nightcore.itemrarity.abstracted.UpgradeableItem;
import cl.nightcore.itemrarity.abstracted.XpBonusItem;
import cl.nightcore.itemrarity.config.ItemConfig;
import cl.nightcore.itemrarity.model.ExperienceMultiplierModel;
import cl.nightcore.itemrarity.model.GemModel;
import cl.nightcore.itemrarity.model.GemRemoverModel;
import cl.nightcore.itemrarity.model.ItemUpgraderModel;
import cl.nightcore.itemrarity.util.ItemUtil;
import cl.nightcore.itemrarity.util.PerformanceTimer;
import cl.nightcore.itemrarity.util.RateLimiter;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.BlocksAttacks;
import io.papermc.paper.datacomponent.item.blocksattacks.DamageReduction;
import io.papermc.paper.datacomponent.item.blocksattacks.ItemDamageFunction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.function.BiFunction;

import static cl.nightcore.itemrarity.util.ItemUtil.isGem;
import static cl.nightcore.itemrarity.util.ItemUtil.isIdentified;

@SuppressWarnings("UnstableApiUsage")
public class IdentifyScrollListener implements Listener {

    PerformanceTimer timer = new PerformanceTimer();
    private final RateLimiter rateLimiter = RateLimiter.getInstance(); // Nueva instancia

    private static final NamespacedKey LORE_UPDATED_KEY =
            new NamespacedKey(ItemRarity.PLUGIN, "lore_updated");

    private static final BlocksAttacks SWORD_BLOCK_ATTACKS = BlocksAttacks.blocksAttacks()
            .blockDelaySeconds(0f)
            .disableCooldownScale(0f)
            .addDamageReduction(DamageReduction.damageReduction().factor(0.5f).build())
            .blockSound(Registry.SOUNDS.getKey(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR))
            .itemDamage(ItemDamageFunction.itemDamageFunction().factor(0.01f).build())
            .build();

    private boolean isLoreUpdated(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(LORE_UPDATED_KEY);
    }

    private void setLoreUpdated(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(LORE_UPDATED_KEY, PersistentDataType.BOOLEAN, true);
            item.setItemMeta(meta);
        }
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (event instanceof InventoryCreativeEvent) {
            return;
        }

        if (event.getInventory() instanceof SmithingInventory){
            return;
        }

        ItemStack cursor = event.getCursor();
        ItemStack targetItem = event.getCurrentItem();

        if (ItemUtil.isInvalidInteraction(targetItem)) {
            return;
        }


        ItemUtil.ObjectType objectType = ItemUtil.getObjectType(cursor);

        if (objectType == ItemUtil.ObjectType.NONE) {
            // Only add the attribute in lore to weapons
            if (ItemUtil.getItemType(targetItem).equals("Weapon")) {
                if (!isLoreUpdated(targetItem) || targetItem.containsEnchantment(Enchantment.SHARPNESS)) {
                    ItemUtil.attributesDisplayInLore(targetItem);
                    setLoreUpdated(targetItem);
                    if (targetItem.getType().toString().endsWith("SWORD")) {
                        if (!targetItem.hasData(DataComponentTypes.BLOCKS_ATTACKS)) {
                            targetItem.setData(DataComponentTypes.BLOCKS_ATTACKS, SWORD_BLOCK_ATTACKS);
                        }
                    }
                }
            }
            return;
        }

        if (event.getSlotType() != InventoryType.SlotType.CRAFTING){
            return;
        }

        handleInteraction(event, targetItem, cursor, objectType);
    }


    private void handleInteraction(InventoryClickEvent event, ItemStack targetItem, ItemStack cursor, ItemUtil.ObjectType type) {
        Player player = (Player) event.getWhoClicked();

        switch (type) {
            case IDENTIFY_SCROLL:
                if (isGem(targetItem)){
                    return;
                }
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
            case XP_MULTIPLIER:
                if (!isIdentified(targetItem)) {
                    sendErrorMessage(player, "Tu objeto debe estar identificado.", ItemConfig.XP_MULTIPLIER_PREFIX);
                    event.setCancelled(true);
                    return;
                }
                handleExperienceMultiplier(event, targetItem, cursor, player);
                break;
        }
    }


    private void handleExperienceMultiplier(InventoryClickEvent event, ItemStack targetItem, ItemStack cursor, Player player) {
        ExperienceMultiplierModel multiplierModel = new ExperienceMultiplierModel(cursor);
        XpBonusItem xpBonusItem = new XpBonusItem(targetItem) {};

        int currentMultiplier = xpBonusItem.getExperienceMultiplier();
        int newMultiplier = multiplierModel.getMultiplier();

        if (newMultiplier > currentMultiplier) {
            xpBonusItem.addExperienceMultiplier(newMultiplier, player);
            event.setCurrentItem(xpBonusItem);
            consumeItem(event, cursor);
        } else {
            player.sendMessage(ItemConfig.XP_MULTIPLIER_PREFIX.append(
                    Component.text("El objeto ya tiene un multiplicador mayor (" + currentMultiplier + "%).")
                            .color(NamedTextColor.RED)));
        }
        event.setCancelled(true);
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
            UpgradeableItem item = new UpgradeableItem(targetItem);
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

}