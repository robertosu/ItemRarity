package cl.nightcore.itemrarity.abstracted;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.classes.*;
import cl.nightcore.itemrarity.config.ItemConfig;
import cl.nightcore.itemrarity.statprovider.StatProvider;
import cl.nightcore.itemrarity.util.ItemUtil;
import com.nexomc.nexo.api.NexoItems;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.AuraSkillsBukkit;
import dev.aurelium.auraskills.api.item.ModifierType;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.StatModifier;
import dev.aurelium.auraskills.api.stat.Stats;
import dev.aurelium.auraskills.api.util.AuraSkillsModifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static cl.nightcore.itemrarity.abstracted.EnhancedSocketableItem.BASE_STAT_VALUE_PREFIX;
import static cl.nightcore.itemrarity.abstracted.EnhancedSocketableItem.GEM_BOOST_PREFIX;
import static cl.nightcore.itemrarity.util.ItemUtil.isIdentified;

public abstract class IdentifiedItem extends ItemStack {

    //public static final String ROLL_IDENTIFIER_KEY = "roll_count";
    public static final String LEVEL_KEY = "magicobject_roll_lvl";
    public static final String IDENTIFIER_KEY = "is_identify_scrolled";

    private final List<Stat> addedStats;
    private final List<Integer> statValues;
    protected ModifierType MODIFIER_TYPE;
    protected StatProvider statProvider;
    protected RollQuality rollQuality;



    protected ItemRarity plugin = (ItemRarity) Bukkit.getPluginManager().getPlugin("ItemRarity");
    Component reset = Component.text().content("").color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false).build();
    private Component rarity;

    public IdentifiedItem(ItemStack item) {
        super(item);
        rollQuality = getRollQuality();
        this.addedStats = new ArrayList<>();
        this.statValues = new ArrayList<>();
        this.MODIFIER_TYPE = ItemUtil.getModifierType(item);
        if (!isIdentified(item)) {
            generateStats();
            applyStatsToItem();
            obtainRarity();
            setIdentifiedNBT();
            setLore();
        }
        obtainRarity();
    }

    private int getItemLevel() {
        if (getRollQuality() instanceof LowRollQuality) {
            return 1;
        } else if (getRollQuality() instanceof MediumRollQuality) {
            return 2;
        } else if (getRollQuality() instanceof HighRollQuality) {
            return 3;
        } else if (getRollQuality() instanceof GodRollQuality) {
            return 4;
        }
        return 0;
    }

    protected abstract void generateStats();

    public void rerollStats() {
        emptyLore();
        removeModifiers();
        generateStats();
        applyStatsToItem();
        obtainRarity();
        setLore();
    }

    protected void removeSpecificModifier(Stat stat) {
        this.setItemMeta(AuraSkillsBukkit.get().getItemManager().removeStatModifier(this, MODIFIER_TYPE, stat).getItemMeta());
    }

    protected void addModifier(Stat stat, int value) {
        this.setItemMeta(AuraSkillsBukkit.get().getItemManager().addStatModifier(this, MODIFIER_TYPE, stat, value, true).getItemMeta());
    }

    protected Stat getLowestModifier() {
        PersistentDataContainer container = getItemMeta().getPersistentDataContainer();
        StatModifier lowestValidModifier = null;
        double lowestValue = Double.MAX_VALUE;

        // Obtener todos los modificadores actuales
        for (StatModifier modifier : getStatModifiers()) {
            Stat currentStat = modifier.type();

            // Obtener el boost de gema si existe
            int gemBoost = container.getOrDefault(
                    new NamespacedKey(plugin, GEM_BOOST_PREFIX + currentStat.name()),
                    PersistentDataType.INTEGER,
                    0
            );

            // Obtener el valor base (ya sea guardado en NBT o calculado)
            double baseValue;
            if (gemBoost > 0) {
                // Si hay boost de gema, usar el valor base guardado
                baseValue = container.getOrDefault(
                        new NamespacedKey(plugin, BASE_STAT_VALUE_PREFIX + currentStat.name()),
                        PersistentDataType.INTEGER,
                        (int)(modifier.value() - gemBoost)
                );
            } else {
                // Si no hay boost de gema, usar el valor actual
                baseValue = modifier.value();
            }

            // Comparar usando el valor base
            if (baseValue < lowestValue) {
                lowestValue = baseValue;
                lowestValidModifier = modifier;
            }
        }

        return lowestValidModifier != null ? lowestValidModifier.type() : null;
    }

    protected void removeSpecificStatLoreLine(Stat lowestStat) {
        ItemMeta meta = this.getItemMeta();
        /*List<String> lore = Objects.requireNonNull(meta).getLore();*/
        @Nullable List<Component> lore = meta.lore();
        if (lore != null){
            String statDisplayName = lowestStat.getDisplayName(AuraSkillsApi.get().getMessageManager().getDefaultLanguage());
            lore.removeIf(line-> line.toString().contains(statDisplayName));
            meta.lore(lore);
            this.setItemMeta(meta);
        }
    }

    public StatModifier getHighestStatModifier() {
        PersistentDataContainer container = getItemMeta().getPersistentDataContainer();
        StatModifier highestValidModifier = null;
        double highestValue = Double.MIN_VALUE;

        // Recolectar boosts de gemas
        Map<Stat, Integer> gemBoosts = new HashMap<>();
        for (NamespacedKey key : container.getKeys()) {
            if (key.getKey().startsWith(GEM_BOOST_PREFIX)) {
                String statName = key.getKey().substring(GEM_BOOST_PREFIX.length()).toUpperCase();
                Stat stat = Stats.valueOf(statName);
                int boost = container.get(key, PersistentDataType.INTEGER);
                gemBoosts.put(stat, boost);
            }
        }

        for (StatModifier modifier : getStatModifiers()) {
            Stat currentStat = modifier.type();
            int gemBoost = gemBoosts.getOrDefault(currentStat, 0);

            // Obtener el valor base de la stat (si existe)
            int baseValue = container.getOrDefault(
                    new NamespacedKey(plugin, BASE_STAT_VALUE_PREFIX + currentStat.name()),
                    PersistentDataType.INTEGER,
                    0
            );

            // Solo considerar stats que tienen valor base o no tienen boost de gema
            if (baseValue > 0 || gemBoost == 0) {
                double value = gemBoost > 0 ? baseValue : modifier.value();
                if (value > highestValue) {
                    highestValue = value;
                    highestValidModifier = modifier;
                }
            }
        }

        return highestValidModifier;
    }




    protected void generateStatsExceptHighestStat(Stat excludedStat) {
        Random random = new Random();
        int statsCount = random.nextInt(2) + 4; // 4 o 5 estadísticas
        StatProvider statProvider = ItemUtil.getStatProvider(this);
        assert statProvider != null;
        List<Stat> availableStats = statProvider.getAvailableStats();
        addedStats.clear();
        statValues.clear();
        for (Stat stat : statProvider.getGaussStats()) {
            if (stat != excludedStat) {
                getAddedStats().add(stat);
                int value = StatValueGenerator.generateValueForStat(getRollQuality(), statProvider.isThisStatGauss(stat));
                getStatValues().add(value);
            }
        }
        int gaussStatsAdded = getAddedStats().size();
        for (int i = 0; i < statsCount - gaussStatsAdded; i++) {
            Stat stat;
            do {
                stat = availableStats.get(random.nextInt(availableStats.size()));
            } while (getAddedStats().contains(stat) || stat == excludedStat);
            getAddedStats().add(stat);
            int value = StatValueGenerator.generateValueForStat(getRollQuality(), statProvider.isThisStatGauss(stat));
            getStatValues().add(value);
        }
    }

    protected void removeAllModifierStats() {
        for (StatModifier stat : getStatModifiers()) {
            removeSpecificModifier(stat.type());
            removeSpecificStatLoreLine(stat.type());

        }
    }



    protected void applyStatsToItem() {
        for (int i = this.addedStats.size() - 1; i >= 0; i--) {
            Stat stat = this.addedStats.get(i);
            double value = this.statValues.get(i);
            this.setItemMeta(AuraSkillsBukkit.get().getItemManager().addStatModifier(this, MODIFIER_TYPE, stat, value, true).getItemMeta());
        }
    }

    public void removeModifiers() {
        for (Stats stat : ItemRarity.STATS) {
            this.setItemMeta(AuraSkillsBukkit.get().getItemManager().removeStatModifier(this, MODIFIER_TYPE, stat).getItemMeta());
        }
    }

    protected void emptyLore() {
        ItemMeta meta = getItemMeta();
        List<Component> emptylore = new ArrayList<>();
        meta.lore(emptylore);
        setItemMeta(meta);
    }

    public RollQuality getRollQuality() {
        int level = getLevel();
        if (level >= 30) {
            return new GodRollQuality();
        } else if (level >= 20) {
            return new HighRollQuality();
        } else if (level >= 10) {
            return new MediumRollQuality();
        } else {
            return new LowRollQuality();
        }
    }

    protected void updateRarity() {
        ItemMeta meta = getItemMeta();
        //List<String> lore = meta.getLore();
        @Nullable List<Component> lore = meta.lore();
        if (lore != null) {
            lore.removeIf(line -> line.toString().contains(ItemConfig.COMMON_RARITY_KEYWORD)
                    || line.toString().contains(ItemConfig.UNCOMMON_RARITY_KEYWORD)
                    || line.toString().contains(ItemConfig.RARE_RARITY_KEYWORD)
                    || line.toString().contains(ItemConfig.EPIC_RARITY_KEYWORD)
                    || line.toString().contains(ItemConfig.LEGENDARY_RARITY_KEYWORD)
                    || line.toString().contains(ItemConfig.GODLIKE_RARITY_KEYWORD));
            meta.lore(lore);
            setItemMeta(meta);
        }
        obtainRarity();
        setLore();
    }

    private void obtainRarity() {
        if (!getStatModifiers().isEmpty()) {
            double average = getStatModifiers().stream()
                    .mapToDouble(AuraSkillsModifier::value)
                    .average()
                    .orElse(0.0);

            this.rarity = ItemUtil.calculateRarity(this.rollQuality, average);
        }
    }

    public TextColor getRarityColor() {
        if (rarity.toString().contains(ItemConfig.GODLIKE_RARITY_KEYWORD)) {
            return ItemConfig.GODLIKE_COLOR;
        } else if (rarity.toString().contains(ItemConfig.LEGENDARY_RARITY_KEYWORD)) {
            return ItemConfig.LEGENDARY_COLOR;
        } else if (rarity.toString().contains(ItemConfig.EPIC_RARITY_KEYWORD)) {
            return ItemConfig.EPIC_COLOR;
        } else if (rarity.toString().contains(ItemConfig.RARE_RARITY_KEYWORD)) {
            return ItemConfig.RARE_COLOR;
        } else if (rarity.toString().contains(ItemConfig.UNCOMMON_RARITY_KEYWORD)) {
            return ItemConfig.UNCOMMON_COLOR;
        } else {
            return ItemConfig.COMMON_COLOR;
        }
    }

    public Component getRarityKeyword() {
        if (rarity.toString().contains(ItemConfig.GODLIKE_RARITY_KEYWORD)) {
            return Component.text(ItemConfig.GODLIKE_RARITY_KEYWORD);
        } else if (rarity.toString().contains(ItemConfig.LEGENDARY_RARITY_KEYWORD)) {
            return Component.text(ItemConfig.LEGENDARY_RARITY_KEYWORD);
        } else if (rarity.toString().contains(ItemConfig.EPIC_RARITY_KEYWORD)) {
            return Component.text(ItemConfig.EPIC_RARITY_KEYWORD);
        } else if (rarity.toString().contains(ItemConfig.RARE_RARITY_KEYWORD)) {
            return Component.text(ItemConfig.RARE_RARITY_KEYWORD);
        } else if (rarity.toString().contains(ItemConfig.UNCOMMON_RARITY_KEYWORD)) {
            return Component.text(ItemConfig.UNCOMMON_RARITY_KEYWORD);
        } else {
            return Component.text(ItemConfig.COMMON_RARITY_KEYWORD);
        }
    }

    public int getLevel() {
        PersistentDataContainer container = this.getItemMeta().getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, LEVEL_KEY);
        return container.getOrDefault(key, PersistentDataType.INTEGER, 0);
    }

    public void setIdentifiedNBT() {
        ItemMeta meta = this.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, IDENTIFIER_KEY);
        container.set(key, PersistentDataType.BOOLEAN, true);
        this.setItemMeta(meta);
    }

    void setLore() {
        ItemMeta meta = getItemMeta();
        @Nullable List<Component> lore;
        if (meta.lore() != null) {
            lore = meta.lore();
        } else {
            lore = new ArrayList<>();
        }
        // Eliminar líneas de rareza existentes
        if (lore != null) {
            lore.removeIf(line -> {
                String plainText = PlainTextComponentSerializer.plainText().serialize(line);
                return plainText.contains(" ● Nvl ");
            });
        }
        // Encontrar el índice después de la última línea que comienza con "+"
        int lastStatIndex = -1;
        if (lore != null) {
            for (int i = 0; i < lore.size(); i++) {
                String line = PlainTextComponentSerializer.plainText().serialize(lore.get(i));
                if (line.trim().startsWith("+")) {
                    lastStatIndex = i;
                }
            }
        }
        Component ilvl = Component.text(" ● Nvl " + getItemLevel() + " ●")
                .color(NamedTextColor.DARK_GRAY)
                .decorate(TextDecoration.ITALIC);
        Component rarityLine = rarity.append(ilvl);

        // Insertar en la posición correcta
        int insertIndex = lastStatIndex != -1 ? lastStatIndex + 1 : 0;
        assert lore != null;
        if (insertIndex < lore.size()) {
            lore.add(insertIndex, rarityLine);
        } else {
            lore.add(rarityLine);
        }
        meta.lore(lore);

        // Resto del código para manejar nombres y atributos
        if (NexoItems.idFromItem(this) != null && ItemUtil.getItemType(this).equals("Weapon")) {
            nexoWeaponLoreHandle(meta);
        } else if (NexoItems.idFromItem(this) != null && ItemUtil.getItemType(this).equals("Armor")) {
            nexoArmorHandle(meta);
        } else if (NexoItems.idFromItem(this) == null && !ItemUtil.getItemType(this).equals("Armor")) {
            vanillaWeaponLoreHandle(meta);
        } else if (NexoItems.idFromItem(this) == null && ItemUtil.getItemType(this).equals("Armor")) {
            vanillaArmorLoreHandle(meta);
        }
        updateLoreWithSockets();
    }

    private void vanillaArmorLoreHandle(ItemMeta meta) {
        if (!meta.hasCustomName()) {
            String itemTranslationKey = this.translationKey();
            TranslatableComponent translatedName = Component.translatable(itemTranslationKey).color(getRarityColor());
            Component newName = reset.append(translatedName);
            meta.itemName(newName);
        } else {
            Component component = meta.customName();
            assert component != null;
            component = component.color(getRarityColor()).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
            meta.displayName(component);
        }
        setItemMeta(meta);
    }

    private void vanillaWeaponLoreHandle(ItemMeta meta) {
        if (!meta.hasCustomName()) {
            String itemTranslationKey = this.translationKey();
            TranslatableComponent translatedName = Component.translatable(itemTranslationKey).color(getRarityColor());
            Component newName = reset.append(translatedName);
            meta.itemName(newName);
        } else {
            Component component = meta.customName();
            assert component != null;
            component = component.color(getRarityColor()).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
            meta.displayName(component);
        }
        setItemMeta(meta);
        ItemUtil.attributesDisplayInLore(this);
    }

    private void nexoArmorHandle(ItemMeta meta) {
        String plainText = PlainTextComponentSerializer.plainText().serialize(meta.itemName());
        Component component = Component.text(plainText, getRarityColor()).decoration(TextDecoration.ITALIC, false);
        meta.customName(component);
        setItemMeta(meta);
    }

    private void nexoWeaponLoreHandle(ItemMeta meta) {
        String plainText = PlainTextComponentSerializer.plainText().serialize(meta.itemName());
        Component component = Component.text(plainText, getRarityColor()).decoration(TextDecoration.ITALIC, false);
        meta.customName(component);
        setItemMeta(meta);
        ItemUtil.attributesDisplayInLore(this);
    }

    public List<StatModifier> getStatModifiers() {
        return AuraSkillsBukkit.get().getItemManager().getStatModifiers(this, MODIFIER_TYPE);
    }

    public List<Integer> getStatValues() {
        return statValues;
    }

    public List<Stat> getAddedStats() {
        return addedStats;
    }
    public abstract void updateLoreWithSockets();

}