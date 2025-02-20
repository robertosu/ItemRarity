package cl.nightcore.itemrarity.item;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.config.ItemConfig;
import cl.nightcore.itemrarity.model.GemModel;
import cl.nightcore.itemrarity.model.ItemUpgraderModel;
import cl.nightcore.itemrarity.util.ItemUtil;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.stat.Stat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

import static cl.nightcore.itemrarity.config.ItemConfig.LEVEL_KEY_NS;

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

        lore.add(Component.text("Insertala en tu objeto para agregar ").color(NamedTextColor.GRAY)
                .append(gemName)
                .decoration(TextDecoration.ITALIC, false));

        lore.add(Component.text(String.format("+%d %s", value,
                        stat.getDisplayName(AuraSkillsApi.get().getMessageManager().getDefaultLanguage())))
                .color(ItemUtil.getColorOfStat(stat))
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(lore);
        setItemMeta(meta);
    }
    private void setupCustomName(){
        meta.customName(Component.text("Gema de "+ stat.getDisplayName(AuraSkillsApi.get().getMessageManager().getDefaultLanguage()) + " +" + this.level).color(ItemUtil.getColorOfStat(stat)).decoration(TextDecoration.ITALIC,false));
        setItemMeta(meta);
    }

    public static TextColor getPrimaryColor(){
        return PRIMARY_COLOR;
    }
    public Stat getStat() {
        return stat;
    }

}