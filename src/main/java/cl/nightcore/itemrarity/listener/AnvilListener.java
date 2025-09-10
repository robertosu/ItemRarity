package cl.nightcore.itemrarity.listener;

import cl.nightcore.itemrarity.abstracted.UpgradeableItem;
import cl.nightcore.itemrarity.util.AnvilRepairUtil.ItemRepairManager;
import cl.nightcore.itemrarity.util.ItemUtil;
import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.api.events.NexoItemsLoadedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;

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
        // Ambos son items custom - validar con nuestro sistema
        if (event.getInventory().getSecondItem() != null || event.getInventory().getSecondItem() != null) {
            if (!repairManager.isValidRepair(
                    NexoItems.idFromItem(event.getInventory().getFirstItem()),
                    NexoItems.idFromItem(event.getInventory().getSecondItem())) && NexoItems.exists(event.getResult())) {
                // Por defecto mis items de nexo son reparables con papel (de manera vanilla)
                // Y cancelamos si no es el item nexo especifico que repara nuestro objeto
                // Combinación no permitida - cancelar
                event.setResult(null);
            }
        }
    }

    @EventHandler()
    public void onIdentifiedItemAsRecipe(PrepareResultEvent event){


        if (event.getResult() == null) return;

        if (!ItemUtil.isIdentified(event.getResult())) return;


        UpgradeableItem updatedItem = new UpgradeableItem(event.getResult());
        updatedItem.setNewLore(true);
        event.setResult(updatedItem);
    }

}