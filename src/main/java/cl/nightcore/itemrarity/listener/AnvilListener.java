package cl.nightcore.itemrarity.listener;

import cl.nightcore.itemrarity.abstracted.UpgradeableItem;
import cl.nightcore.itemrarity.util.AnvilRepairUtil.ItemRepairManager;
import cl.nightcore.itemrarity.util.ItemUtil;
import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.api.events.NexoItemsLoadedEvent;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.EnchantingInventory;

/**
 * Intercepta la reparación en yunque para validar combinaciones de items custom.
 * Utiliza el component "repairable" de Minecraft para el comportamiento vanilla automático.
 */
public class AnvilListener implements Listener {

    private final ItemRepairManager repairManager;

    public AnvilListener(ItemRepairManager repairManager) {
        this.repairManager = repairManager;
    }

    @EventHandler()
    public void onNexoReload(NexoItemsLoadedEvent event) {
        repairManager.reloadConfigs();
    }

    @EventHandler()
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        //System.out.println("onPrepareAnvil INTERFERRED");
        // Ambos son items custom - validar con nuestro sistema
        if (event.getInventory().getSecondItem() != null){
            if (event.getInventory().getSecondItem().getType().equals(Material.ENCHANTED_BOOK)){
                return;
            }
        }

        if (event.getInventory().getFirstItem() != null){
            if (event.getInventory().getFirstItem().getType().equals(Material.ENCHANTED_BOOK)){
                return;
            }
        }


        if (event.getInventory().getSecondItem() != null) {

            boolean isValidRepair = repairManager.isValidRepair(
                    NexoItems.idFromItem(event.getInventory().getFirstItem()),
                    NexoItems.idFromItem(event.getInventory().getSecondItem()));

            System.out.println("isvalidrepair: " + isValidRepair);

            System.out.println("result is nexoitem: " + NexoItems.exists(event.getResult()));

            if (!isValidRepair) {
                // Por defecto mis items de nexo son reparables con papel (de manera vanilla)
                // Y cancelamos si no es el item nexo especifico que repara nuestro objeto
                // Combinación no permitida - cancelar
                event.setResult(null);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemEnchant(EnchantItemEvent event){

        System.out.println("EnchantItemEvent triggered");

        var item = event.getItem();

        item.addEnchantments(event.getEnchantsToAdd());


        if (!ItemUtil.isIdentified(item)) return;

        UpgradeableItem updatedItem = new UpgradeableItem(item);
        updatedItem.setNewLore(true);

        var i = event.getInventory();

        if (i instanceof EnchantingInventory einv){
            einv.setItem(updatedItem);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onIdentifiedItemAsRecipe(PrepareResultEvent event){
        //System.out.println("onIdentifiedItemAsRecipe INTERFERRED");
        if (event.getResult() == null) return;

        if (!ItemUtil.isIdentified(event.getResult())) return;

        UpgradeableItem updatedItem = new UpgradeableItem(event.getResult());
        updatedItem.setNewLore(true);
        event.setResult(updatedItem);
    }
}