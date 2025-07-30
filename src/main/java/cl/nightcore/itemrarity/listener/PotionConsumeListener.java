package cl.nightcore.itemrarity.listener;

import cl.nightcore.itemrarity.item.potion.StatPotion;
import cl.nightcore.itemrarity.util.ItemUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class PotionConsumeListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Verificar si es una poción de stats
        if (!StatPotion.isStatPotion(item)) {
            return;
        }

        // Obtener datos de la poción
        StatPotion.StatPotionData potionData = StatPotion.getPotionData(item);
        if (potionData == null) {
            return;
        }

        // Crear una poción temporal para aplicar el efecto
        StatPotion tempPotion = new StatPotion(
                potionData.stat(),
                potionData.value(),
                potionData.duration(),
                item.displayName(),
                ItemUtil.getColorOfStat(potionData.stat()));

        // Aplicar el efecto de la poción
        tempPotion.consumePotion(player);
    }
}