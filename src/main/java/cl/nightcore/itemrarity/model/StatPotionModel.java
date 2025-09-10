package cl.nightcore.itemrarity.model;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.config.CombinedStats;
import cl.nightcore.itemrarity.config.ItemConfig;
import cl.nightcore.itemrarity.item.potion.PotionManager;
import cl.nightcore.itemrarity.item.potion.StatPotion;
import cl.nightcore.itemrarity.util.ItemUtil;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.stat.StatModifier;
import dev.aurelium.auraskills.api.util.AuraSkillsModifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.concurrent.TimeUnit;

public record StatPotionModel(
        CombinedStats stat,
        PotionManager.PotionLevel level,
        PotionManager.PotionDuration duration
) {

    private static final NamespacedKey POTION_STAT = new NamespacedKey(ItemRarity.PLUGIN, "potion_stat");
    private static final NamespacedKey POTION_LEVEL = new NamespacedKey(ItemRarity.PLUGIN, "potion_level");
    private static final NamespacedKey POTION_DURATION = new NamespacedKey(ItemRarity.PLUGIN, "potion_duration");
    private static final NamespacedKey IS_STAT_POTION = new NamespacedKey(ItemRarity.PLUGIN, "is_stat_potion");

    /**
     * Crea un modelo desde un ItemStack, retorna null si no es una poción válida
     */
    public static StatPotionModel fromItemStack(ItemStack item) {
        if (item == null || item.getItemMeta() == null) {
            return null;
        }

        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();

        if (!container.has(IS_STAT_POTION, PersistentDataType.BOOLEAN)) {
            return null;
        }

        // Recuperar stat
        String statName = container.get(POTION_STAT, PersistentDataType.STRING);
        if (statName == null) return null;

        CombinedStats stat;
        try {
            stat = CombinedStats.valueOf(statName);
        } catch (IllegalArgumentException e) {
            return null;
        }

        // Recuperar level
        String levelName = container.get(POTION_LEVEL, PersistentDataType.STRING);
        if (levelName == null) return null;

        PotionManager.PotionLevel level;
        try {
            level = PotionManager.PotionLevel.valueOf(levelName);
        } catch (IllegalArgumentException e) {
            return null;
        }

        // Recuperar duration
        String durationName = container.get(POTION_DURATION, PersistentDataType.STRING);
        if (durationName == null) return null;

        PotionManager.PotionDuration duration;
        try {
            duration = PotionManager.PotionDuration.valueOf(durationName);
        } catch (IllegalArgumentException e) {
            return null;
        }

        return new StatPotionModel(stat, level, duration);
    }

    /**
     * Consume la poción y aplica el efecto al jugador
     */
    public void consumePotion(Player player) {
        // Calcular tiempo de expiración
        long expirationTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(duration.getSeconds());

        // Crear el modificador temporal
        StatModifier modifier = new StatModifier(
                StatPotion.POTION_MODIFIER_NAME + stat.getDelegateStat().name(),
                stat.getDelegateStat(),
                level.getValue(),
                AuraSkillsModifier.Operation.ADD
        );

        modifier.makeTemporary(expirationTime, true);

        // Aplicar el modificador temporal
        AuraSkillsApi.get().getUser(player.getUniqueId()).addTempStatModifier(modifier, true, expirationTime);

        // Efectos visuales y sonoros
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.0f);

        // Mensaje de confirmación
        Component message = Component.text("Poción consumida: ", NamedTextColor.GREEN)
                .append(Component.text("+" + level.getValue() + " ")
                        .color(getStatColor()))
                .append(Component.text(stat.getDisplayName(ItemRarity.AURA_LOCALE))
                        .color(getStatColor()))
                .append(Component.text(" por " + duration.getFormattedTime(), NamedTextColor.YELLOW));

        player.sendMessage(ItemConfig.STATPOTION_PREFIX.append(message));
    }

    private TextColor getStatColor() {
        return ItemUtil.getColorOfStat(stat);
    }
}