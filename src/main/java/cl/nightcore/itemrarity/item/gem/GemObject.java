package cl.nightcore.itemrarity.item.gem;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.util.ItemUtil;
import dev.aurelium.auraskills.api.stat.Stat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

import static cl.nightcore.itemrarity.ItemRarity.AURA_LOCALE;

public class GemObject extends ItemStack {
    private static final String GEM_STAT_KEY = "gem_stat";
    private static final String GEM_LEVEL_KEY = "gem_level";
    private static final TextColor PRIMARY_COLOR = TextColor.fromHexString("#2DF0FF");
    private final Stat stat;
    private final int value;
    private final int level;
    private final Component gemName;
    private final int customModelData;
    private final ItemMeta meta = this.getItemMeta();

    public GemObject(ItemStack item, Stat stat, Component gemName, int level, int customModelData) {
        super(item);
        this.stat = stat;
        this.value = 4 + (level - 1) * level / 2;
        this.gemName = gemName;
        this.level = level;
        this.customModelData = customModelData;
        setupCustomName();
        setupGemLore();
        setGemNBT();
    }

    public static TextColor getPrimaryColor() {
        return PRIMARY_COLOR;
    }

    private void setGemNBT() {
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(ItemRarity.PLUGIN, GEM_STAT_KEY);
        container.set(key, PersistentDataType.STRING, stat.name());
        NamespacedKey lvlkey = new NamespacedKey(ItemRarity.PLUGIN, GEM_LEVEL_KEY);
        container.set(lvlkey, PersistentDataType.INTEGER, level);
        meta.setCustomModelData(customModelData);
        meta.setMaxStackSize(1);
        setItemMeta(meta);
    }

    private void setupGemLore() {
        List<Component> lore = new ArrayList<>();

        lore.add(Component.empty());
        lore.add(Component.text("Al incrustar: ")
                .color(NamedTextColor.GRAY)
                .append(Component.text(String.format("+%d %s", value, stat.getDisplayName(AURA_LOCALE)))
                        .color(ItemUtil.getColorOfStat(stat))
                        .decoration(TextDecoration.ITALIC, false))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Mejorable con Forja").color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC,true));
        meta.lore(lore);
        setItemMeta(meta);
    }

    private void setupCustomName() {
        var levelComponent = Component.text(" [+" + this.level + "]")
                .color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, false);
        meta.customName(Component.text("Gema de " + stat.getDisplayName(AURA_LOCALE))
                .color(ItemUtil.getColorOfStat(stat))
                .decoration(TextDecoration.ITALIC, false)
                .append(levelComponent));
        setItemMeta(meta);
    }

    public Stat getStat() {
        return stat;
    }
}