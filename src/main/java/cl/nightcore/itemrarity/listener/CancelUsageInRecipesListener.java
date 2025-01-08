package cl.nightcore.itemrarity.listener;

import cl.nightcore.itemrarity.ItemRarity;
import com.nexomc.nexo.api.NexoBlocks;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import java.util.List;
import java.util.Set;

public class CancelUsageInRecipesListener implements Listener {
    @EventHandler
    public void onItemCraft(PrepareItemCraftEvent event) {
        ItemStack[] matrix = event.getInventory().getMatrix();
        for (ItemStack item : matrix) {
            if (ItemRarity.isMagicObject(item) || ItemRarity.isIdentifyScroll(item)) {
                event.getInventory().setResult(null);
                return;
            }
        }
    }
}