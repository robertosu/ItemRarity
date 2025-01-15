package cl.nightcore.itemrarity.listener;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.util.ItemUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
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
            // System.out.println(clickedItem.getItemMeta().toString());
            if (ItemUtil.isIdentifiable(clickedItem)
                    && !ItemUtil.getItemType(clickedItem).equals("Armor")) {
                if (!isLoreUpdated(clickedItem) || clickedItem.containsEnchantment(Enchantment.SHARPNESS)) {
                    ItemUtil.attributesDisplayInLore(clickedItem);
                    setLoreUpdated(clickedItem);
                }
            }
        }
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
