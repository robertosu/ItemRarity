package cl.nightcore.itemrarity;

import cl.nightcore.itemrarity.item.GemObject;
import cl.nightcore.itemrarity.util.ItemUtil;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.stat.Stats;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class GemManager {
    public GemObject createStrengthGem(int amount, int level, String stat) {
        ItemStack gemItem = new ItemStack(Material.PAPER);
        return new GemObject(
                gemItem,
                Stats.valueOf(stat),
                Component.text(Stats.valueOf(stat)
                                .getDisplayName(
                                        AuraSkillsApi.get().getMessageManager().getDefaultLanguage()))
                        .color(ItemUtil.getColorOfStat(Stats.valueOf(stat))),
                level,
                amount);
    }
}
