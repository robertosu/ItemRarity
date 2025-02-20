package cl.nightcore.itemrarity.listener;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.util.ItemUtil;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChangedMainHandEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ItemClickListener implements Listener {

    private static final NamespacedKey LORE_UPDATED_KEY =
            new NamespacedKey(ItemRarity.getPlugin(ItemRarity.class), "lore_updated");

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();

        if (ItemUtil.isNotEmpty(clickedItem)) {
            // Only add the attribute to weapons
            if (ItemUtil.isIdentifiable(clickedItem) && ItemUtil.getItemType(clickedItem).equals("Weapon")) {
                if (!isLoreUpdated(clickedItem) || clickedItem.containsEnchantment(Enchantment.SHARPNESS)) {
                    ItemUtil.attributesDisplayInLore(clickedItem);
                    setLoreUpdated(clickedItem);
                }
            }
        }
    }


    @EventHandler
    public void onChangeHand(PlayerInventorySlotChangeEvent event){
        System.out.println(event.getEventName() + "evento llamado");
    }


    @EventHandler
    public void onChangeWtf(PlayerItemHeldEvent event){
        System.out.println(event.getEventName() + "evento llamado");
    }


    private boolean isLoreUpdated(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.getOrDefault(LORE_UPDATED_KEY, PersistentDataType.BOOLEAN, false);
    }

    private void setLoreUpdated(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(LORE_UPDATED_KEY, PersistentDataType.BOOLEAN, true);
            item.setItemMeta(meta);
        }
    }
}
