package cl.nightcore.itemrarity;

import cl.nightcore.itemrarity.config.CombinedStats;
import cl.nightcore.itemrarity.config.ItemConfig;
import cl.nightcore.itemrarity.item.GemObject;
import cl.nightcore.itemrarity.item.ItemUpgrader;
import cl.nightcore.itemrarity.model.GemModel;
import cl.nightcore.itemrarity.model.ItemUpgraderModel;
import cl.nightcore.itemrarity.util.ItemUtil;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.stat.Stat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GemManager {
    private static final int MAX_LEVEL = 6;

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
            case "ACCURACY" -> 3252;
            case "LUCK" -> 3253;
            case "WISDOM" -> 3254;
            case "TOUGHNESS" -> 3255;
            case "CRIT_CHANCE" -> 3259;
            case "CRIT_DAMAGE" -> 3257;
            case "DEXTERITY" -> 3256;
            case "EVASION" -> 3258;
            default -> throw new IllegalStateException(); // En caso de que el nombre no coincida con ninguna stat
        };
    }

    public ItemStack upgradeGem(Player player, ItemStack gem, ItemUpgraderModel itemUpgrader) {

        GemObject gemObject;

        GemModel gemModel = new GemModel(gem);
        int level = gemModel.getLevel();
        Stat stat = gemModel.getStat();
        int type = itemUpgrader.getType();
        int percentage = itemUpgrader.getPercentage();

        // Aumentar la probabilidad de fallo en un 5% por cada nivel del objeto
        double adjustedPercentage = percentage - (level * 5);
        if (adjustedPercentage < 0) adjustedPercentage = 0; // Asegurar que no sea negativo

        if (level < MAX_LEVEL) {
            if (ItemUtil.rollthedice(adjustedPercentage)) {
                var newlevel = level + 1;
                player.sendMessage(ItemConfig.ITEM_UPGRADER_PREFIX
                        .color(ItemUpgraderModel.getPrimaryColor(itemUpgrader.getType()))
                        .append(Component.text("Mejora exitosa, tu objeto subió a: ", ItemUpgrader.getLoreColor())
                                .append(Component.text("Nivel " + newlevel, NamedTextColor.DARK_GRAY))));

                // Reproducir sonido de éxito
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

                return createGem(1, newlevel, stat.name());
            } else {
                switch (type) {
                    case 1 -> { // Inestable
                        player.sendMessage(ItemConfig.ITEM_UPGRADER_PREFIX
                                .color(ItemUpgraderModel.getPrimaryColor(itemUpgrader.getType()))
                                .append(Component.text("La mejora falló y tu objeto se rompió.", NamedTextColor.RED)));
                        gem.setAmount(0); // Romper el objeto

                        // Reproducir sonido de rotura
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                    }
                    case 2 -> { // Activa
                        if (level > 1) {
                            int newlevel = level - 1;

                            player.sendMessage(ItemConfig.ITEM_UPGRADER_PREFIX
                                    .color(ItemUpgraderModel.getPrimaryColor(itemUpgrader.getType()))
                                    .append(Component.text("La mejora falló, tu objeto bajó a: ", NamedTextColor.RED)
                                            .append(Component.text(
                                                    "Nivel " + newlevel, ItemUpgrader.getActiveColor()))));

                            // Reproducir sonido de fallo (puedes usar un sonido diferente si lo deseas)
                            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 1.0f, 1.0f);

                            return createGem(1, newlevel, stat.name());
                        } else {
                            player.sendMessage(ItemConfig.ITEM_UPGRADER_PREFIX
                                    .color(ItemUpgraderModel.getPrimaryColor(itemUpgrader.getType()))
                                    .append(Component.text("La mejora falló.", NamedTextColor.RED)));

                            // Reproducir sonido de fallo
                            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 1.0f, 1.0f);
                        }
                    }
                    case 3 -> { // Estable
                        player.sendMessage(ItemConfig.ITEM_UPGRADER_PREFIX
                                .color(ItemUpgraderModel.getPrimaryColor(itemUpgrader.getType()))
                                .append(Component.text(
                                        "La mejora falló, pero tu objeto no cambió.", NamedTextColor.YELLOW)));

                        // Reproducir sonido de fallo (puedes usar un sonido diferente si lo deseas)
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
                    }
                }
            }
        } else {
            player.sendMessage(ItemConfig.ITEM_UPGRADER_PREFIX.append(
                    Component.text("Tu objeto ya es del nivel máximo. ", ItemUpgrader.getLoreColor())));

            // Reproducir sonido de error o advertencia
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
        }
        return gem;
    }
}
