package cl.nightcore.itemrarity.listener;

import cl.nightcore.itemrarity.ItemRepairManager;
import com.nexomc.nexo.api.NexoItems;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.view.AnvilView;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("UnstableApiUsage")
public class AnvilListener implements Listener {
    private final ItemRepairManager repairManager;
    private final JavaPlugin plugin;

    public AnvilListener(ItemRepairManager repairManager, JavaPlugin plugin) {
        this.repairManager = repairManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory anvilInv = event.getInventory();
        ItemStack firstItem = anvilInv.getFirstItem();
        ItemStack secondItem = anvilInv.getSecondItem();

        if (firstItem == null || secondItem == null) return;

        String firstItemId = NexoItems.idFromItem(firstItem);
        String secondItemId = NexoItems.idFromItem(secondItem);

        if (firstItemId == null || secondItemId == null) return;

        if (repairManager.isValidRepair(firstItemId, secondItemId)) {
            ItemStack repairedItem = firstItem.clone();
            Damageable metaNew = (Damageable) repairedItem.getItemMeta();

            int currentDamage = metaNew.getDamage();
            if (currentDamage == 0) return;

            int maxDurability = metaNew.getMaxDamage();
            int repairPerItem = maxDurability / 4;
            int itemsNeeded = (int) Math.ceil((double) currentDamage / repairPerItem);
            int availableItems = Math.min(itemsNeeded, secondItem.getAmount());

            if (availableItems == 0) return;

            int totalRepair = repairPerItem * availableItems;
            int newDamage = Math.max(0, currentDamage - totalRepair);
            metaNew.setDamage(newDamage);
            repairedItem.setItemMeta(metaNew);

            // Combinar con nombre personalizado si existe
            String newName = event.getView().getRenameText();
            if (!newName.isEmpty()) {
                ItemMeta meta = repairedItem.getItemMeta();
                meta.displayName(Component.text(newName));
                repairedItem.setItemMeta(meta);
            }

            // Guardar metadata
            ItemMeta meta = repairedItem.getItemMeta();
            meta.getPersistentDataContainer().set(
                    new NamespacedKey(plugin, "repair_items_needed"),
                    PersistentDataType.INTEGER,
                    availableItems
            );
            repairedItem.setItemMeta(meta);

            // Establecer costo y forzar actualización
            int repairCost = availableItems + (newName.isEmpty() ? 0 : 1); // Costo de reparación + renombre
            event.getView().setRepairCost(repairCost);

            event.setResult(repairedItem);

            anvilInv.getViewers().forEach(human -> {
                if (human instanceof Player) ((Player) human).updateInventory();

            });
        }
    }

    @EventHandler
    public void onAnvilClick(InventoryClickEvent event) {
        if (!(event.getInventory() instanceof AnvilInventory anvilInv)) return;
        if (event.getSlotType() != InventoryType.SlotType.RESULT) return;

        ItemStack result = event.getCurrentItem();
        if (result == null) return;

        ItemMeta meta = result.getItemMeta();
        if (meta == null) return;

        Integer itemsToConsume = meta.getPersistentDataContainer().get(
                new NamespacedKey(plugin, "repair_items_needed"),
                PersistentDataType.INTEGER
        );

        if (itemsToConsume == null) return;

        // Consumir materiales del segundo slot
        ItemStack secondItem = anvilInv.getSecondItem();
        if (secondItem != null) {
            if (secondItem.getAmount() <= itemsToConsume) {
                anvilInv.setSecondItem(null);
            } else {
                secondItem.setAmount(secondItem.getAmount() - itemsToConsume);
            }
        }

        if (event.getView() instanceof AnvilView anvilView){
            if (anvilView.getRenameText().isEmpty()) {
                anvilInv.setFirstItem(null);
            }
        }


        // Limpiar metadata y actualizar
        meta.getPersistentDataContainer().remove(new NamespacedKey(plugin, "repair_items_needed"));
        result.setItemMeta(meta);
        anvilInv.setResult(null);

        Player player = (Player) event.getWhoClicked();
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
        player.updateInventory(); // Forzar actualización final
    }
}