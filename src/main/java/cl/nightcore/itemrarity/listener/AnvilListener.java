package cl.nightcore.itemrarity.listener;

import cl.nightcore.itemrarity.abstracted.UpgradeableItem;
import cl.nightcore.itemrarity.util.AnvilRepairUtil.ItemRepairManager;
import cl.nightcore.itemrarity.util.ItemUtil;
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
                // Combinación no permitida - cancelar
                event.setResult(null);
            }
        }
        if (event.getResult() != null || ItemUtil.isIdentified(event.getResult())){
            UpgradeableItem temp = new UpgradeableItem(event.getResult());
            temp.setNewLore();
            event.setResult(temp);
        }
    }
}