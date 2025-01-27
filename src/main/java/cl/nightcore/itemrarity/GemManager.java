package cl.nightcore.itemrarity;

import cl.nightcore.itemrarity.config.CombinedStats;
import cl.nightcore.itemrarity.item.GemObject;
import cl.nightcore.itemrarity.util.ItemUtil;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class GemManager {
    public GemObject createGem(int amount, int level, String stat) {

        ItemStack gemItem = new ItemStack(Material.PAPER, amount);
        return new GemObject(
                gemItem,
                CombinedStats.valueOf(stat),
                Component.text(CombinedStats.valueOf(stat)
                                .getDisplayName(
                                        AuraSkillsApi.get().getMessageManager().getDefaultLanguage()))
                        .color(ItemUtil.getColorOfStat(CombinedStats.valueOf(stat))),
                level,
                getCustomModelData(stat));
    }

    public int getCustomModelData(String stat) {
        return switch (stat.toUpperCase()) {
            case "STRENGTH" -> 3250;
            case "HEALTH" -> 3251;
            case "REGENERATION" -> 3252;
            case "LUCK" -> 3253;
            case "WISDOM" -> 3254;
            case "TOUGHNESS" -> 3255;
            case "CRIT_CHANCE" -> 3256;
            case "CRIT_DAMAGE" -> 3257;
            case "DEXTERITY" -> 3258;
            case "ACCURACY" -> 3259;
            default -> throw new IllegalStateException(); // En caso de que el nombre no coincida con ninguna stat
        };
    }
}
