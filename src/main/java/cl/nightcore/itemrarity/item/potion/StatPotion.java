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
    public static final NamespacedKey POTION_VALUE = new NamespacedKey(ItemRarity.PLUGIN, "potion_value");
    public static final NamespacedKey POTION_DURATION = new NamespacedKey(ItemRarity.PLUGIN, "potion_duration");
    public static final NamespacedKey IS_STAT_POTION = new NamespacedKey(ItemRarity.PLUGIN, "is_stat_potion");
    private static final String POTION_MODIFIER_NAME = "potion_effect";
    private static final int DEFAULT_DURATION_SECONDS = 300; // 5 minutos
    private final CombinedStats stat;
    private final double value;
    private final int durationSeconds;
    private final Component displayName;
    private final TextColor potionColor;
    private final int customModelData;

    public StatPotion(CombinedStats stat, double value, int durationSeconds, Component displayName, TextColor potionColor) {
        super(Material.PAPER);
        this.stat = stat;
        this.value = value;
        this.durationSeconds = durationSeconds;
        this.displayName = displayName;
        this.potionColor = potionColor;
        this.customModelData = determineCustomModelData();

        setupPotionItem();
    }

    public StatPotion(CombinedStats stat, double value, int durationSeconds) {
        this(
                stat,
                value,
                durationSeconds,
                Component.text("Elixir de " + stat.getDisplayName(ItemRarity.AURA_LOCALE)),
                ItemUtil.getColorOfStat(stat));
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

    public static StatPotionData getPotionData(ItemStack item) {
        if (!isStatPotion(item)) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        String statName = container.get(POTION_STAT, PersistentDataType.STRING);
        Double value = container.get(POTION_VALUE, PersistentDataType.DOUBLE);
        Integer duration = container.get(POTION_DURATION, PersistentDataType.INTEGER);


        if (statName == null || value == null || duration == null) {
            return null;
        }

        try {
            CombinedStats stat = CombinedStats.valueOf(statName);
            return new StatPotionData(stat, value, duration);
        } catch (IllegalArgumentException e) {
            return null;
        }
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
        meta.itemName(displayName.color(potionColor).decoration(TextDecoration.ITALIC, false));

        // Lore descriptivo
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Al consumir:", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));

        String operation = value > 0 ? "+" : "";
        lore.add(Component.text(operation + (int)value + " " + stat.getDisplayName(ItemRarity.AURA_LOCALE))
                .color(getStatColor()).decoration(TextDecoration.ITALIC, false)
                );
        lore.add(Component.text("Duración: ").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                .append(Component.text(formatDuration(durationSeconds)).color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)));



        // Guardar datos de la poción en NBT
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(POTION_STAT, PersistentDataType.STRING, stat.name());
        container.set(POTION_VALUE, PersistentDataType.DOUBLE, value);
        container.set(POTION_DURATION, PersistentDataType.INTEGER, durationSeconds);
        container.set(IS_STAT_POTION, PersistentDataType.BOOLEAN, true);
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

    private String formatDuration(int seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            return (seconds / 60) + "m " + (seconds % 60) + "s";
        } else {
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            return hours + "h " + minutes + "m";
        }
    }

    public void consumePotion(Player player) {

        // Calcular tiempo de expiración
        long expirationTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(durationSeconds);

        // Crear el modificador temporal
        StatModifier modifier = new StatModifier(POTION_MODIFIER_NAME+stat.getDelegateStat().name(), stat.getDelegateStat(), value, AuraSkillsModifier.Operation.ADD);

        modifier.makeTemporary(expirationTime,true);


        // Aplicar el modificador temporal
        AuraSkillsApi.get().getUser(player.getUniqueId()).addTempStatModifier(modifier, true, expirationTime);

        // Efectos visuales y sonoros
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.0f);

        // Mensaje de confirmación
        Component message = Component.text("Poción consumida: ", NamedTextColor.GREEN)
                .append(Component.text((value > 0 ? "+" : "") + (int)value + " ")
                        .color(getStatColor()))
                .append(Component.text(stat.getDisplayName(ItemRarity.AURA_LOCALE))
                        .color(getStatColor()))
                .append(Component.text(" por " + formatDuration(durationSeconds), NamedTextColor.YELLOW));

        player.sendMessage(ItemConfig.STATPOTION_PREFIX.append(message));
    }

    // Getters
    public CombinedStats getStat() {
        return stat;
    }

    public double getValue() {
        return value;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public Component getDisplayName() {
        return displayName;
    }

    public TextColor getPotionColor() {
        return potionColor;
    }

    // Clase helper para datos de la poción
    public record StatPotionData(CombinedStats stat, double value, int duration) {
    }
}