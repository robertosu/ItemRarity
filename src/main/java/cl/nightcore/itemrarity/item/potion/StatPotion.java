package cl.nightcore.itemrarity.item.potion;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.config.CombinedStats;
import cl.nightcore.itemrarity.config.ItemConfig;
import cl.nightcore.itemrarity.util.ItemUtil;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.stat.StatModifier;
import dev.aurelium.auraskills.api.util.AuraSkillsModifier;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class StatPotion extends ItemStack {

    public static final NamespacedKey POTION_STAT = new NamespacedKey(ItemRarity.PLUGIN, "potion_stat");
    public static final NamespacedKey POTION_LEVEL = new NamespacedKey(ItemRarity.PLUGIN, "potion_level");
    public static final NamespacedKey POTION_DURATION = new NamespacedKey(ItemRarity.PLUGIN, "potion_duration");
    public static final NamespacedKey IS_STAT_POTION = new NamespacedKey(ItemRarity.PLUGIN, "is_stat_potion");

    // Mantener keys legacy para compatibilidad con pociones existentes
    public static final NamespacedKey POTION_VALUE = new NamespacedKey(ItemRarity.PLUGIN, "potion_value");
    public static final NamespacedKey POTION_DURATION_SECONDS = new NamespacedKey(ItemRarity.PLUGIN, "potion_duration_seconds");

    private static final String POTION_MODIFIER_NAME = "potion_effect";

    private final CombinedStats stat;
    private final PotionManager.PotionLevel level;
    private final PotionManager.PotionDuration duration;
    private final Double legacyValue;
    private final Integer legacyDurationSeconds;
    private final Component displayName;
    private final int customModelData;

    // Constructor usando enums
    // Constructor usando valores directos
    public StatPotion(CombinedStats stat, double value, int durationSeconds) {
        super(Material.PAPER);
        this.stat = stat;
        this.level = null;
        this.duration = null;
        this.legacyValue = value;
        this.legacyDurationSeconds = durationSeconds;
        this.displayName = createDisplayNameFromValues(stat, value, durationSeconds);
        this.customModelData = determineCustomModelData();

        setupPotionItem();
    }

    // Constructor con color personalizado (enums)
    public StatPotion(CombinedStats stat, PotionManager.PotionLevel level, PotionManager.PotionDuration duration) {
        super(Material.PAPER);
        this.stat = stat;
        this.level = level;
        this.duration = duration;
        this.legacyValue = null;
        this.legacyDurationSeconds = null;
        this.displayName = createDisplayName(stat, level, duration);
        this.customModelData = determineCustomModelData();

        setupPotionItem();
    }

    private static Component createDisplayName(CombinedStats stat, PotionManager.PotionLevel level, PotionManager.PotionDuration duration) {
        Component levelComponent = Component.text(" [+" + level.getName() + "] ", NamedTextColor.DARK_GRAY);
        Component nameComponent = Component.text("Elixir de " + stat.getDisplayName(ItemRarity.AURA_LOCALE) + " " + duration.getName(),ItemUtil.getColorOfStat(stat));

        return nameComponent.append(levelComponent);
    }

    private static Component createDisplayNameFromValues(CombinedStats stat, double value, int durationSeconds) {
        Component valueComponent = Component.text("[+" + (int)value + "] ", NamedTextColor.DARK_GRAY);
        String durationName = formatDurationName(durationSeconds);
        Component nameComponent = Component.text("Elixir de " + stat.getDisplayName(ItemRarity.AURA_LOCALE) + " " + durationName);

        return valueComponent.append(nameComponent);
    }

    private static String formatDurationName(int seconds) {
        if (seconds <= 300) return "Efímero";
        else if (seconds <= 600) return "Intermedio";
        else if (seconds <= 900) return "Duradero";
        else return "Muy Duradero";
    }



    // Métodos para verificar si un item es una poción de stats
    public static boolean isStatPotion(ItemStack item) {
        if (item == null || item.getType() != Material.PAPER) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(IS_STAT_POTION, PersistentDataType.BOOLEAN);
    }


    @SuppressWarnings("UnstableApiUsage")
    private void setupPotionItem() {
        // Configurar como item consumible usando DataComponents
        this.setData(DataComponentTypes.CONSUMABLE, Consumable.consumable()
                .consumeSeconds(1.6f)
                .animation(ItemUseAnimation.DRINK)
                .sound(Registry.SOUNDS.getKey(Sound.ENTITY_GENERIC_DRINK))
                .hasConsumeParticles(true)
                .build());

        // Configurar metadata del item
        ItemMeta meta = this.getItemMeta();
        meta.setCustomModelData(customModelData);

        // Nombre personalizado
        meta.itemName(displayName.decoration(TextDecoration.ITALIC, false));

        // Lore descriptivo
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        var start = Component.text("Al consumir: ", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false);

        // Obtener valor actual (puede venir de enum o valor directo)
        double currentValue = getValue();
        String operation = currentValue > 0 ? "+" : "";
        lore.add(start.append(Component.text(operation + (int)currentValue + " " + stat.getDisplayName(ItemRarity.AURA_LOCALE))
                .color(getStatColor()).decoration(TextDecoration.ITALIC, false)));

        lore.add(Component.empty());

        // Obtener duración formateada
        String formattedDuration = getFormattedDuration();
        lore.add(Component.text("Duración: ").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                .append(Component.text(formattedDuration).color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)));

        // Guardar datos de la poción en NBT
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(POTION_STAT, PersistentDataType.STRING, stat.name());
        container.set(IS_STAT_POTION, PersistentDataType.BOOLEAN, true);

        // Si tenemos enums, guardar esos datos
        if (level != null && duration != null) {
            container.set(POTION_LEVEL, PersistentDataType.STRING, level.name());
            container.set(POTION_DURATION, PersistentDataType.STRING, duration.name());
        }

        // Siempre guardar valores numéricos para compatibilidad
        container.set(POTION_VALUE, PersistentDataType.DOUBLE, currentValue);
        container.set(POTION_DURATION_SECONDS, PersistentDataType.INTEGER, getDurationSeconds());

        this.setItemMeta(meta);
        this.lore(lore);
    }

    private int determineCustomModelData() {
        switch (stat) {
            case STRENGTH -> {
                return 7100;
            }
            case DEXTERITY -> {
                return 7101;
            }
            case TOUGHNESS -> {
                return 7102;
            }
            case HEALTH -> {
                return 7103;
            }
            case CRIT_DAMAGE -> {
                return 7104;
            }
            case EVASION -> {
                return 7105;
            }
            case ACCURACY -> {
                return 7106;
            }
            case LUCK -> {
                return 7107;
            }
            case WISDOM -> {
                return 7108;
            }
            case CRIT_CHANCE -> {
                return 7109;
            }
        }
        return 0;
    }

    private TextColor getStatColor() {
        return ItemUtil.getColorOfStat(stat);
    }

    public void consumePotion(Player player) {
        // Calcular tiempo de expiración
        long expirationTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(getDurationSeconds());

        // Crear el modificador temporal
        StatModifier modifier = new StatModifier(
                POTION_MODIFIER_NAME + stat.getDelegateStat().name(),
                stat.getDelegateStat(),
                getValue(),
                AuraSkillsModifier.Operation.ADD
        );

        modifier.makeTemporary(expirationTime, true);

        // Aplicar el modificador temporal
        AuraSkillsApi.get().getUser(player.getUniqueId()).addTempStatModifier(modifier, true, expirationTime);

        // Efectos visuales y sonoros
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.0f);

        // Mensaje de confirmación
        Component message = Component.text("Poción consumida: ", NamedTextColor.GREEN)
                .append(Component.text("+" + (int)getValue() + " ")
                        .color(getStatColor()))
                .append(Component.text(stat.getDisplayName(ItemRarity.AURA_LOCALE))
                        .color(getStatColor()))
                .append(Component.text(" por " + getFormattedDuration(), NamedTextColor.YELLOW));

        player.sendMessage(ItemConfig.STATPOTION_PREFIX.append(message));
    }

    // Getters
    public CombinedStats getStat() {
        return stat;
    }

    public PotionManager.PotionLevel getLevel() {
        return level;
    }

    public PotionManager.PotionDuration getDuration() {
        return duration;
    }

    public double getValue() {
        return level != null ? level.getValue() : legacyValue != null ? legacyValue : 0.0;
    }

    public int getDurationSeconds() {
        return duration != null ? duration.getSeconds() : legacyDurationSeconds != null ? legacyDurationSeconds : 300;
    }

    private String getFormattedDuration() {
        if (duration != null) {
            return duration.getFormattedTime();
        } else {
            // Formatear duración manual
            int seconds = getDurationSeconds();
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;
            return String.format("%d:%02d", minutes, remainingSeconds);
        }
    }

    public Component getDisplayName() {
        return displayName;
    }

    // Clase helper para datos de la poción - actualizada para soportar ambos formatos
    public static class StatPotionData {
        private final CombinedStats stat;
        private final PotionManager.PotionLevel level;
        private final PotionManager.PotionDuration duration;
        private final Double legacyValue;
        private final Integer legacyDuration;
        private final boolean isLegacy;

        // Constructor para formato nuevo
        public StatPotionData(CombinedStats stat, PotionManager.PotionLevel level, PotionManager.PotionDuration duration) {
            this.stat = stat;
            this.level = level;
            this.duration = duration;
            this.legacyValue = null;
            this.legacyDuration = null;
            this.isLegacy = false;
        }

        // Constructor para formato legacy
        public StatPotionData(CombinedStats stat, double value, int duration) {
            this.stat = stat;
            this.level = null;
            this.duration = null;
            this.legacyValue = value;
            this.legacyDuration = duration;
            this.isLegacy = true;
        }

        public CombinedStats getStat() { return stat; }
        public PotionManager.PotionLevel getLevel() { return level; }
        public PotionManager.PotionDuration getDuration() { return duration; }
        public Double getLegacyValue() { return legacyValue; }
        public Integer getLegacyDuration() { return legacyDuration; }
        public boolean isLegacy() { return isLegacy; }

        // Métodos de conveniencia
        public double getValue() {
            return isLegacy ? legacyValue : level.getValue();
        }

        public int getDurationSeconds() {
            return isLegacy ? legacyDuration : duration.getSeconds();
        }
    }
}