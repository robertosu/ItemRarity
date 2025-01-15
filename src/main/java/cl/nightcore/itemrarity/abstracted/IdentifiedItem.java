package cl.nightcore.itemrarity.abstracted;

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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static cl.nightcore.itemrarity.ItemRarity.PLUGIN;
import static cl.nightcore.itemrarity.abstracted.EnhancedSocketableItem.BASE_STAT_VALUE_PREFIX;
import static cl.nightcore.itemrarity.abstracted.EnhancedSocketableItem.GEM_BOOST_PREFIX;
import static cl.nightcore.itemrarity.util.ItemUtil.getStatProvider;
import static cl.nightcore.itemrarity.util.ItemUtil.isIdentified;

public abstract class IdentifiedItem extends ItemStack {

    protected final ModifierType MODIFIER_TYPE;
    protected final StatProvider statProvider;
    private final List<Stat> addedStats;
    private final List<Integer> statValues;
    protected RollQuality rollQuality;
    protected Component rarity;

    Component reset = Component.text().content("").color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false).build();

    public IdentifiedItem(ItemStack item) {
        super(item);
        rollQuality = getRollQuality();
        this.statProvider = getStatProvider(this);
        this.addedStats = new ArrayList<>();
        this.statValues = new ArrayList<>();
        this.MODIFIER_TYPE = ItemUtil.getModifierType(item);
        if (!isIdentified(item)) {
            generateStats();
            applyStatsToItem();
            setIdentifiedNBT();
            setLore();
        }
    }

    public Component getRarityComponent() {
        return rarity;
    }

    protected abstract void generateStats();

    protected void removeSpecificModifier(Stat stat) {
        this.setItemMeta(AuraSkillsBukkit.get().getItemManager().removeStatModifier(this, MODIFIER_TYPE, stat).getItemMeta());
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
                    new NamespacedKey(PLUGIN, GEM_BOOST_PREFIX + currentStat.name()),
                    PersistentDataType.INTEGER,
                    0
            );

            // Obtener el valor base (ya sea guardado en NBT o calculado)
            double baseValue;
            if (gemBoost > 0) {
                // Si hay boost de gema, usar el valor base guardado
                baseValue = container.getOrDefault(
                        new NamespacedKey(PLUGIN, BASE_STAT_VALUE_PREFIX + currentStat.name()),
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
                    new NamespacedKey(PLUGIN, BASE_STAT_VALUE_PREFIX + currentStat.name()),
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
        for (Stats stat : Stats.values()) {
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
        if (level == 4) {
            return new GodRollQuality();
        } else if (level == 3) {
            return new HighRollQuality();
        } else if (level == 2) {
            return new MediumRollQuality();
        } else {
            return new LowRollQuality();
        }
    }


    public void obtainRarity() {
        if (!getStatModifiers().isEmpty()) {
            PersistentDataContainer container = getItemMeta().getPersistentDataContainer();
            double totalValue = 0;
            int statCount = 0;

            for (StatModifier modifier : getStatModifiers()) {
                Stat currentStat = modifier.type();
                double currentValue = modifier.value();

                // Obtener el boost de la gema si existe
                int gemBoost = container.getOrDefault(
                        new NamespacedKey(PLUGIN, GEM_BOOST_PREFIX + currentStat.name()),
                        PersistentDataType.INTEGER,
                        0
                );

                // Si el valor total del modificador es igual al boost de la gema,
                // significa que toda la stat proviene de la gema, así que la ignoramos completamente
                if (currentValue == gemBoost) {
                    continue; // No contar esta stat ni para el total ni para el contador
                }

                // Si hay un boost de gema pero no es el valor total,
                // restar el boost antes de agregar al total
                if (gemBoost > 0) {
                    currentValue -= gemBoost;
                }

                // Agregar al total y aumentar el contador
                totalValue += currentValue;
                statCount++;
            }

            // Calcular el promedio solo si hay stats válidas
            double average = (statCount > 0) ? totalValue / statCount : 0.0;
            this.rarity = ItemUtil.calculateRarity(this.rollQuality, average);
        }
    }

    public TextColor getRarityColor() {
        return this.rarity.color();
    }


    public int getLevel() {
        PersistentDataContainer container = this.getItemMeta().getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(PLUGIN, ItemConfig.ITEM_LEVEL_KEY);
        return container.getOrDefault(key, PersistentDataType.INTEGER, 1);

    }

    public void setIdentifiedNBT() {
        ItemMeta meta = this.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(PLUGIN, ItemConfig.SCROLLED_IDENTIFIER_KEY);
        container.set(key, PersistentDataType.BOOLEAN, true);
        this.setItemMeta(meta);
    }

    void setLore() {
        obtainRarity();
        ItemMeta meta = getItemMeta();
        @Nullable List<Component> lore;
        if (meta.lore() != null) {
            lore = meta.lore();
        } else {
            lore = new ArrayList<>();
        }
        // Eliminar líneas de rareza existentes
        if (lore != null) {
            lore.removeIf(line -> line.toString().contains("●"));
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
        Component ilvl = Component.text("● Nivel " + getLevel() + " ● ")
                .color(NamedTextColor.DARK_GRAY)
                .decorate(TextDecoration.ITALIC);
        Component rarityLine = ilvl.append(Component.text("[").color(rarity.color()).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                .append(rarity)
                .append(Component.text("]").color(rarity.color()).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)));
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
        handleAttributesInLoreAndName(meta, NexoItems.idFromItem(this) != null, ItemUtil.getItemType(this));
        updateLoreWithSockets();
    }

    private void handleAttributesInLoreAndName(ItemMeta meta, boolean isNexoItem, String itemType) {
        if (isNexoItem) {
            String plainText = PlainTextComponentSerializer.plainText().serialize(meta.itemName());
            Component component = Component.text(plainText, getRarityColor())
                    .decoration(TextDecoration.ITALIC, false);
            meta.customName(component);
        } else {
            if (!meta.hasCustomName()) {
                String itemTranslationKey = this.translationKey();
                TranslatableComponent translatedName = Component.translatable(itemTranslationKey).color(getRarityColor());
                Component newName = reset.append(translatedName);
                meta.itemName(newName);
            } else {
                Component component = meta.customName();
                assert component != null;
                component = component.color(getRarityColor())
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
                meta.displayName(component);
            }
        }
        setItemMeta(meta);
        if (itemType.equals("Weapon")) {
            ItemUtil.attributesDisplayInLore(this);
        }
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