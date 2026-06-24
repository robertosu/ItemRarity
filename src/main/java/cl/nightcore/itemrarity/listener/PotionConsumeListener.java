package cl.nightcore.itemrarity.listener;

import cl.nightcore.itemrarity.item.potion.StatPotion;
import cl.nightcore.itemrarity.model.StatPotionModel;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.AuraSkillsBukkit;
import dev.aurelium.auraskills.api.AuraSkillsBukkitProvider;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.skill.Skills;
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

        StatPotionModel potionModel = StatPotionModel.fromItemStack(item);

        // Obtener datos necesarios del modelo
        if (potionModel.stat() == null) {
            return;
        }
        var level = potionModel.level();
        var duration = potionModel.duration();

        var value = (((double) duration.getSeconds() /10) * level.getValue()) / 1.5;

        AuraSkillsApi.get().getUser(player.getUniqueId()).addSkillXp(Skills.ALCHEMY, value);
        // Aplicar el efecto de la poción
        potionModel.consumePotion(player);
    }
}