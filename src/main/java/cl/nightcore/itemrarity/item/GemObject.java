package cl.nightcore.itemrarity.item;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.util.ItemUtil;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.Stats;
import dev.aurelium.auraskills.api.trait.Trait;
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

public class GemObject extends ItemStack {
    private static final String GEM_STAT_KEY = "gem_stat";
    private static final String GEM_LEVEL_KEY = "gem_level";
    private final Trait trait;
    private final int value;
    private final int level;
    private final Component gemName;
    private final int customModelData;

    public static String getGemStatKey(){
        return GEM_STAT_KEY;
    }
    public static String getGemLevelKey(){
        return GEM_LEVEL_KEY;
    }

    public GemObject(ItemStack item, Trait trait, Component gemName, int level, int customModelData, int amount) {
        super(item);
        this.trait = trait;
        this.value = 4 + (level - 1) * level / 2;
        this.gemName = gemName;
        this.level = level;
        setGemNBT();
        setupGemLore();
        this.customModelData = customModelData;
    }

    public int getLevel() {
        return level;
    }

    public Component getGemName() {
        return gemName;
    }

    private void setGemNBT() {
        ItemMeta meta = getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(ItemRarity.plugin, GEM_STAT_KEY);
        container.set(key, PersistentDataType.STRING, trait.name());
        NamespacedKey lvlkey = new NamespacedKey(ItemRarity.plugin, GEM_LEVEL_KEY);
        container.set(lvlkey, PersistentDataType.INTEGER, level);
        meta.setCustomModelData(customModelData);
        setItemMeta(meta);

    }
    public int getValueFromNBT(){
        ItemMeta meta = getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey lvlkey = new NamespacedKey(ItemRarity.plugin, GEM_LEVEL_KEY);
        int levelfromnbt = container.get(lvlkey, PersistentDataType.INTEGER);
        return 4 + (levelfromnbt - 1) * levelfromnbt / 2;
    }

    public Stat getStatFromNBT(){
        ItemMeta meta = getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(ItemRarity.plugin, GEM_STAT_KEY);
        String statfromnbt = container.get(key, PersistentDataType.STRING);
        return Stats.valueOf(statfromnbt);
    }

    private void setupGemLore() {
        ItemMeta meta = getItemMeta();
        List<Component> lore = new ArrayList<>();

        lore.add(Component.text("Gema de ").color(NamedTextColor.GRAY)
                .append(gemName)
                .decoration(TextDecoration.ITALIC, false));

        lore.add(Component.text(String.format("+%d %s", value,
                        trait.getDisplayName(AuraSkillsApi.get().getMessageManager().getDefaultLanguage())))
                .color(TextColor.fromHexString(ItemUtil.getColorOfTrait(trait)))
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(lore);
        setItemMeta(meta);
    }

    public Trait getTrait() {
        return trait;
    }

    public int getValue() {
        return value;
    }
}