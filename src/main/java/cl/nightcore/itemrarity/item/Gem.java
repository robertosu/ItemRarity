package cl.nightcore.itemrarity.item;

import cl.nightcore.itemrarity.ItemRarity;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.Stats;
import dev.aurelium.auraskills.api.item.ModifierType;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.List;

public class Gem extends ItemStack {
    private static final String GEM_TYPE_KEY = "gem_type";
    private final Stat stat;
    private final int value;
    private final ModifierType validType;

    public Component getGemName() {
        return gemName;
    }

    private final Component gemName;

    public Gem(ItemStack item, Stat stat, int value, ModifierType validType, Component gemName) {
        super(item);
        this.stat = stat;
        this.value = value;
        this.validType = validType;
        this.gemName = gemName;
        setGemNBT();
        setupGemLore();
    }

    private void setGemNBT() {
        ItemMeta meta = getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(ItemRarity.plugin, GEM_TYPE_KEY);
        container.set(key, PersistentDataType.STRING, stat.name());
        setItemMeta(meta);
    }

    private void setupGemLore() {
        ItemMeta meta = getItemMeta();
        List<Component> lore = new ArrayList<>();

        lore.add(Component.text("Gema de ").color(NamedTextColor.GRAY)
                .append(gemName)
                .decoration(TextDecoration.ITALIC, false));

        lore.add(Component.text(String.format("+%d %s", value,
                        stat.getDisplayName(AuraSkillsApi.get().getMessageManager().getDefaultLanguage())))
                .color(NamedTextColor.BLUE)
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(lore);
        setItemMeta(meta);
    }

    public Stat getStat() {
        return stat;
    }

    public int getValue() {
        return value;
    }

    public ModifierType getValidType() {
        return validType;
    }

    public static boolean isGem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(ItemRarity.plugin, GEM_TYPE_KEY);
        return container.has(key, PersistentDataType.STRING);
    }
}