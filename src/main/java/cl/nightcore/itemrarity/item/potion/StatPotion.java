package cl.nightcore.itemrarity.item.potion;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.config.CombinedStats;
import cl.nightcore.itemrarity.util.ItemUtil;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class StatPotion extends ItemStack {

    public static final NamespacedKey POTION_STAT = new NamespacedKey(ItemRarity.PLUGIN, "potion_stat");
    public static final NamespacedKey POTION_LEVEL = new NamespacedKey(ItemRarity.PLUGIN, "potion_level");
    public static final NamespacedKey POTION_DURATION = new NamespacedKey(ItemRarity.PLUGIN, "potion_duration");
    public static final NamespacedKey IS_STAT_POTION = new NamespacedKey(ItemRarity.PLUGIN, "is_stat_potion");

    public static final String POTION_MODIFIER_NAME = "potion_effect";

    private final CombinedStats stat;
    private final PotionManager.PotionLevel level;
    private final PotionManager.PotionDuration duration;


    public StatPotion(CombinedStats stat, double value, int durationSeconds) {
        super(Material.PAPER);
        this.stat = stat;
        // Convertir valor a nivel más cercano
        this.level = valueToLevel(value);
        // Convertir segundos a duración más cercana
        this.duration = secondsToDuration(durationSeconds);

        setupPotionItem();
    }

    private static PotionManager.PotionLevel valueToLevel(double value) {
        if (value <= 5) return PotionManager.PotionLevel.ONE;
        if (value <= 10) return PotionManager.PotionLevel.TWO;
        if (value <= 15) return PotionManager.PotionLevel.THREE;
        if (value <= 20) return PotionManager.PotionLevel.FOUR;
        return PotionManager.PotionLevel.FIVE;
    }

    private static PotionManager.PotionDuration secondsToDuration(int seconds) {
        if (seconds <= 300) return PotionManager.PotionDuration.LOW;
        if (seconds <= 600) return PotionManager.PotionDuration.MEDIUM;
        if (seconds <= 900) return PotionManager.PotionDuration.HIGH;
        return PotionManager.PotionDuration.HIGHEST;
    }

    public StatPotion(CombinedStats stat, PotionManager.PotionLevel level, PotionManager.PotionDuration duration) {
        super(Material.PAPER);
        this.stat = stat;
        this.level = level;
        this.duration = duration;

        setupPotionItem();
    }

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
        meta.setCustomModelData(determineCustomModelData());

        // Nombre personalizado
        Component displayName = createDisplayName();
        meta.itemName(displayName.decoration(TextDecoration.ITALIC, false));

        // Lore descriptivo
        List<Component> lore = createLore();

        // Guardar datos de la poción en NBT
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(POTION_STAT, PersistentDataType.STRING, stat.name());
        container.set(POTION_LEVEL, PersistentDataType.STRING, level.name());
        container.set(POTION_DURATION, PersistentDataType.STRING, duration.name());
        container.set(IS_STAT_POTION, PersistentDataType.BOOLEAN, true);

        this.setItemMeta(meta);
        this.lore(lore);
    }

    private Component createDisplayName() {
        Component levelComponent = Component.text(" [+" + level.getName() + "] ", NamedTextColor.DARK_GRAY);
        Component nameComponent = Component.text("Elixir de " + stat.getDisplayName(ItemRarity.AURA_LOCALE) + " " + duration.getName(), getStatColor());

        return nameComponent.append(levelComponent);
    }

    private List<Component> createLore() {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());

        Component start = Component.text("Al consumir: ", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false);

        lore.add(start.append(Component.text("+" + level.getValue() + " " + stat.getDisplayName(ItemRarity.AURA_LOCALE))
                .color(getStatColor()).decoration(TextDecoration.ITALIC, false)));

        lore.add(Component.empty());

        lore.add(Component.text("Duración: ").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                .append(Component.text(duration.getFormattedTime()).color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)));

        return lore;
    }

    private int determineCustomModelData() {
        return switch (stat) {
            case STRENGTH -> 7100;
            case DEXTERITY -> 7101;
            case TOUGHNESS -> 7102;
            case HEALTH -> 7103;
            case CRIT_DAMAGE -> 7104;
            case EVASION -> 7105;
            case ACCURACY -> 7106;
            case LUCK -> 7107;
            case WISDOM -> 7108;
            case CRIT_CHANCE -> 7109;
        };
    }

    private TextColor getStatColor() {
        return ItemUtil.getColorOfStat(stat);
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
}