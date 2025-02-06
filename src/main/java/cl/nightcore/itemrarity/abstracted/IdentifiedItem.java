package cl.nightcore.itemrarity.abstracted;

import cl.nightcore.itemrarity.classes.*;
import cl.nightcore.itemrarity.config.CombinedStats;
import cl.nightcore.itemrarity.config.ItemConfig;
import cl.nightcore.itemrarity.item.BlessingObject;
import cl.nightcore.itemrarity.item.IdentifyScroll;
import cl.nightcore.itemrarity.item.MagicObject;
import cl.nightcore.itemrarity.item.RedemptionObject;
import cl.nightcore.itemrarity.statprovider.StatProvider;
import cl.nightcore.itemrarity.util.ItemUtil;
import cl.nightcore.itemrarity.util.RarityCalculator;
import com.nexomc.nexo.api.NexoItems;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.AuraSkillsBukkit;
import dev.aurelium.auraskills.api.item.ModifierType;
import dev.aurelium.auraskills.api.skill.Multiplier;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.StatModifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static cl.nightcore.itemrarity.ItemRarity.PLUGIN;
import static cl.nightcore.itemrarity.abstracted.SocketableItem.BASE_STAT_VALUE_PREFIX;
import static cl.nightcore.itemrarity.abstracted.SocketableItem.GEM_BOOST_PREFIX;
import static cl.nightcore.itemrarity.util.ItemUtil.RANDOM;

public abstract class IdentifiedItem extends ItemStack {

    private final List<Stat> addedStats;
    private final List<Integer> statValues;
    private static final String statModifierName = "identifiedstats";

    protected final ModifierType modifierType;
    protected final StatProvider statProvider;
    protected Component rarity;

    public IdentifiedItem(ItemStack item) {
        super(item);
        this.statProvider = ItemUtil.getStatProvider(item);
        this.modifierType = ItemUtil.getModifierType(item);
        this.addedStats = new ArrayList<>();
        this.statValues = new ArrayList<>();
    }

    public void identify(Player player) {
        setIdentifiedAndLevelNBT(1);
        setMaxBonuses(5);
        generateStats();
        applyStatsToItem();
        setLore();
        Component message = Component.text("¡Identificaste el arma! Calidad: ", IdentifyScroll.getLoreColor())
                .append(rarity);
        player.sendMessage(ItemConfig.PLUGIN_PREFIX.append(message));
    }


    protected void generateStats() {
        int statsCount = getMaxBonuses(); // Número total de stats a generar
        List<Stat> availableStats = statProvider.getAvailableStats();

        // 1. Agregar stats gaussianas
        for (Stat stat : statProvider.getGaussStats()) {
            getAddedStats().add(stat);
            int value = StatValueGenerator.generateValueForStat(getRollQuality(), statProvider.isThisStatGauss(stat));
            getStatValues().add(value);
        }

        // 2. Calcular cuántas stats adicionales se necesitan
        int remainingStats = statsCount - getAddedStats().size();

        // 3. Agregar stats aleatorias hasta alcanzar el límite
        for (int i = 0; i < remainingStats; i++) {
            Stat stat;
            do {
                stat = availableStats.get(ThreadLocalRandom.current().nextInt(availableStats.size()));
            } while (getAddedStats().contains(stat)); // Evitar duplicados

            getAddedStats().add(stat);
            int value = StatValueGenerator.generateValueForStat(getRollQuality(), statProvider.isThisStatGauss(stat));
            getStatValues().add(value);
        }
    }

    protected void removeSpecificModifier(Stat stat) {
        this.setItemMeta(AuraSkillsBukkit.get()
                .getItemManager()
                .removeStatModifier(this, modifierType, stat, statModifierName)
                .getItemMeta());
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
                    new NamespacedKey(PLUGIN, GEM_BOOST_PREFIX + currentStat.name()), PersistentDataType.INTEGER, 0);

            // Obtener el valor base (ya sea guardado en NBT o calculado)
            double baseValue;
            if (gemBoost > 0) {
                // Si hay boost de gema, usar el valor base guardado
                baseValue = container.getOrDefault(
                        new NamespacedKey(PLUGIN, BASE_STAT_VALUE_PREFIX + currentStat.name()),
                        PersistentDataType.INTEGER,
                        (int) (modifier.value() - gemBoost));
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
        if (lore != null) {
            String statDisplayName = lowestStat.getDisplayName(
                    AuraSkillsApi.get().getMessageManager().getDefaultLanguage());
            lore.removeIf(line -> line.toString().contains(statDisplayName));
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
                String statName =
                        key.getKey().substring(GEM_BOOST_PREFIX.length()).toUpperCase();
                Stat stat = CombinedStats.valueOf(statName);
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
                    0);

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
        int statsCount = getMaxBonuses(); // 5 o 6 estadísticas
        StatProvider statProvider = ItemUtil.getStatProvider(this);
        List<Stat> availableStats = statProvider.getAvailableStats();
        addedStats.clear();
        statValues.clear();

        // 1. Agregar estadísticas Gauss (excluyendo excludedStat)
        for (Stat stat : statProvider.getGaussStats()) {
            if (stat != excludedStat) {
                getAddedStats().add(stat);
                int value =
                        StatValueGenerator.generateValueForStat(getRollQuality(), statProvider.isThisStatGauss(stat));
                getStatValues().add(value);
            }
        }

        // 2. Calcular cuántas estadísticas adicionales se necesitan
        int gaussStatsAdded = getAddedStats().size();
        int additionalStatsNeeded = statsCount - gaussStatsAdded;

        // 3. Agregar estadísticas adicionales (excluyendo excludedStat)
        for (int i = 0; i < additionalStatsNeeded; i++) {
            Stat stat;
            do {
                stat = availableStats.get(RANDOM.nextInt(availableStats.size()));
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
            this.setItemMeta(AuraSkillsBukkit.get()
                    .getItemManager()
                    .addStatModifier(this, modifierType, stat,statModifierName , value, true)
                    .getItemMeta());
        }
    }

    public void removeModifiers() {
        for (Stat stat : CombinedStats.values()) {
            this.setItemMeta(AuraSkillsBukkit.get()
                    .getItemManager()
                    .removeStatModifier(this, modifierType, stat, statModifierName)
                    .getItemMeta());
        }
    }

    protected void emptyLore() {
        ItemMeta meta = getItemMeta();
        List<Component> emptylore = new ArrayList<>();
        meta.lore(emptylore);
        setItemMeta(meta);
    }

    protected void reApplyMultipliers(){
        var multipliers = AuraSkillsBukkit.get().getItemManager().getMultipliers(this,modifierType);
        for (Multiplier multiplier : multipliers){
            this.setItemMeta(AuraSkillsBukkit.get().getItemManager().removeMultiplier(this,modifierType,multiplier.skill()).getItemMeta());
            this.setItemMeta(AuraSkillsBukkit.get().getItemManager().addMultiplier(this,modifierType,multiplier.skill(),multiplier.value(),true).getItemMeta());
        }
    }


    public RollQuality getRollQuality() {
        return switch (getLevel()) {
            case 9 -> _9RollQuality.getInstance();
            case 8 -> _8RollQuality.getInstance();
            case 7 -> _7RollQuality.getInstance();
            case 6 -> _6RollQuality.getInstance();
            case 5 -> _5RollQuality.getInstance();
            case 4 -> _4RollQuality.getInstance();
            case 3 -> _3RollQuality.getInstance();
            case 2 -> _2RollQuality.getInstance();
            case 1 -> _1RollQuality.getInstance();
            default -> throw new IllegalStateException();
        };
    }

    public double calculateAverage() {

        if (getStatModifiers().isEmpty()) {
            throw new IllegalStateException();
        }

        PersistentDataContainer container = getItemMeta().getPersistentDataContainer();
        double totalValue = 0;
        int statCount = 0;

        for (StatModifier modifier : getStatModifiers()) {
            Stat currentStat = modifier.type();
            double currentValue = modifier.value();

            // Obtener el boost de la gema si existe
            int gemBoost = container.getOrDefault(
                    new NamespacedKey(PLUGIN, GEM_BOOST_PREFIX + currentStat.name()), PersistentDataType.INTEGER, 0);
            // Si el valor total del modificador es igual al boost de la gema,
            // significa que toda la stat proviene de la gema, así que la ignoramos completamente
            if (currentValue == gemBoost) {
                continue; // No contar esta stat ni para el total ni para el contador
            }
            // Si hay un boost de gema pero no es el valor total, restar el boost antes de agregar al total
            if (gemBoost > 0) {
                currentValue -= gemBoost;
            }

            // Agregar al total y aumentar el contador
            totalValue += currentValue;
            statCount++;
        }
        // Calcular el promedio solo si hay stats válidas
        return (statCount > 0) ? totalValue / statCount : 0.0;
    }

    public void setRarity(double average) {
        this.rarity = RarityCalculator.calculateRarity(getRollQuality(), average);
    }

    public TextColor getRarityColor() {
        return this.rarity.color();
    }

    public int getLevel() {
        PersistentDataContainer container = this.getItemMeta().getPersistentDataContainer();
        return container.get(ItemConfig.LEVEL_KEY_NS, PersistentDataType.INTEGER);
    }

    public void setIdentifiedAndLevelNBT(int level) {
        ItemMeta meta = this.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(ItemConfig.SCROLLED_IDENTIFIER_KEY_NS, PersistentDataType.BOOLEAN, true);
        container.set(ItemConfig.LEVEL_KEY_NS, PersistentDataType.INTEGER, level);
        this.setItemMeta(meta);
    }

    protected int getMaxBonuses() {
        ItemMeta meta = this.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.get(ItemConfig.MAX_BONUSES_KEY_NS, PersistentDataType.INTEGER);
    }

    protected void setMaxBonuses(int amount) {
        ItemMeta meta = this.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(ItemConfig.MAX_BONUSES_KEY_NS, PersistentDataType.INTEGER, amount);
        this.setItemMeta(meta);
    }

    protected void setLore() {
        setRarity(calculateAverage());
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
            lore.removeIf(line -> line.toString().contains("%"));
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
        Component rarityLine = ilvl.append(Component.text("[")
                .color(rarity.color())
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                .append(rarity)
                .append(Component.text("]")
                        .color(rarity.color())
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)));
        // Insertar en la posición correcta
        int insertIndex = lastStatIndex != -1 ? lastStatIndex + 1 : 0;
        assert lore != null;
        if (insertIndex < lore.size()) {
            lore.add(insertIndex, rarityLine);
        } else {
            lore.add(rarityLine);
        }

        meta.lore(lore);
        this.setItemMeta(meta);

        handleCustomNameAndAttributesInLore(this.getItemMeta(), NexoItems.idFromItem(this) != null, ItemUtil.getItemType(this));
        updateLoreWithSockets();
        reApplyMultipliers();
    }

    private void handleCustomNameAndAttributesInLore(ItemMeta meta, boolean isNexoItem, String itemType) {
        if (isNexoItem) {
            String plainText = PlainTextComponentSerializer.plainText().serialize(meta.itemName());
            Component component = Component.text(plainText, getRarityColor()).decoration(TextDecoration.ITALIC, false);
            meta.customName(component);
        } else {
            if (!meta.hasCustomName()) {
                String itemTranslationKey = this.translationKey();
                TranslatableComponent translatedName =
                        Component.translatable(itemTranslationKey).color(getRarityColor());
                Component newName = ItemUtil.reset.append(translatedName);
                meta.itemName(newName);
            } else {
                Component component = meta.customName();
                assert component != null;
                component =
                        component.color(getRarityColor()).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
                meta.displayName(component);
            }
        }
        setItemMeta(meta);
        if (itemType.equals("Weapon")) {
            ItemUtil.attributesDisplayInLore(this);
        }
    }

    protected void addModifier(Stat stat, int value, boolean generateLore) {
        this.setItemMeta(AuraSkillsBukkit.get()
                .getItemManager()
                .addStatModifier(this, modifierType, stat, statModifierName, value, generateLore)
                .getItemMeta());
    }

    public void rerollStatsEnhanced(Player player) {
        // Guardar los boosts de gemas antes de reroll
        Map<Stat, Integer> gemBoosts = new HashMap<>();
        PersistentDataContainer container = getItemMeta().getPersistentDataContainer();

        // Recolectar todos los boosts de gemas
        for (NamespacedKey key : container.getKeys()) {
            if (key.getKey().startsWith(GEM_BOOST_PREFIX)) {
                String statName =
                        key.getKey().substring(GEM_BOOST_PREFIX.length()).toUpperCase();
                Stat stat = CombinedStats.valueOf(statName);
                int boost = container.getOrDefault(key, PersistentDataType.INTEGER, 0);
                gemBoosts.put(stat, boost);
            }
        }

        // Limpiar stats existentes y generar nuevas
        emptyLore();
        removeModifiers();
        generateStats();

        // Aplicar nuevas stats y boosts en una sola pasada
        for (int i = this.getAddedStats().size() - 1; i >= 0; i--) {
            Stat stat = this.getAddedStats().get(i);
            int baseValue = this.getStatValues().get(i);
            int gemBoost = gemBoosts.getOrDefault(stat, 0);

            if (gemBoost > 0) {
                // Si hay boost de gema, guardar el valor base y aplicar el total
                ItemMeta meta = getItemMeta();
                container = meta.getPersistentDataContainer();
                container.set(
                        new NamespacedKey(PLUGIN, BASE_STAT_VALUE_PREFIX + stat.name()),
                        PersistentDataType.INTEGER,
                        baseValue);
                setItemMeta(meta);

                addModifier(stat, baseValue + gemBoost, false);
                addStatBreakdownToLore(stat, baseValue, gemBoost);
            } else {
                // Si no hay boost, aplicar solo el valor base
                addModifier(stat, baseValue, true);
            }
        }

        // Aplicar gemas que no coinciden con ninguna stat base
        for (Map.Entry<Stat, Integer> entry : gemBoosts.entrySet()) {
            Stat stat = entry.getKey();
            int gemBoost = entry.getValue();

            // Solo aplicar si la stat no estaba en las nuevas stats generadas
            if (!getAddedStats().contains(stat)) {
                addModifier(stat, gemBoost, false);
            }
        }
        setLore();
        updateLoreWithSockets();
        Component message = Component.text("¡El objeto cambió! Rareza: ", MagicObject.getLoreColor());
        player.sendMessage(ItemConfig.REROLL_PREFIX.append(message).append(rarity));
    }

    protected void addStatBreakdownToLore(Stat stat, int baseValue, int gemBoost) {
        ItemMeta meta = getItemMeta();
        List<Component> lore = meta.hasLore() ? meta.lore() : new ArrayList<>();
        int total = baseValue + gemBoost;
        // Crear el componente de desglose
        Component breakdown = Component.text("+" + baseValue + " ")
                .color(ItemUtil.getColorOfStat(stat))
                .append(Component.text(stat.getDisplayName(
                                AuraSkillsApi.get().getMessageManager().getDefaultLanguage()))
                        .color(NamedTextColor.GRAY))
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(" (+" + total + ")")
                        .color(NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false));

        // Encontrar la posición correcta para insertar el stat
        int insertIndex = 0;
        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i).toString();
            if (line.contains("[")) {
                insertIndex = i;
                break;
            }
        }

        lore.add(insertIndex, breakdown);
        meta.lore(lore);
        setItemMeta(meta);
    }

    public void rerollLowestStat(Player player) {
        Stat lowestStat = getLowestModifier();
        if (lowestStat == null) {
            player.sendMessage(
                    ItemConfig.BLESSING_PREFIX.append(Component.text("No hay estadísticas válidas para rerollear.")
                            .color(BlessingObject.getLoreColor())));
            return;
        }

        PersistentDataContainer container = getItemMeta().getPersistentDataContainer();

        // Obtener el boost de gema actual si existe
        int gemBoost = container.getOrDefault(
                new NamespacedKey(PLUGIN, GEM_BOOST_PREFIX + lowestStat.name()), PersistentDataType.INTEGER, 0);

        // Remover la stat actual
        removeSpecificStatLoreLine(lowestStat);
        removeSpecificModifier(lowestStat);

        // Generar nuevo valor base
        int newBaseValue =
                StatValueGenerator.generateValueForStat(getRollQuality(), statProvider.isThisStatGauss(lowestStat));

        // Actualizar NBT y aplicar la stat
        ItemMeta meta = getItemMeta();
        container = meta.getPersistentDataContainer();

        if (gemBoost > 0) {
            // Guardar el nuevo valor base en NBT
            container.set(
                    new NamespacedKey(PLUGIN, BASE_STAT_VALUE_PREFIX + lowestStat.name()),
                    PersistentDataType.INTEGER,
                    newBaseValue);
            setItemMeta(meta);

            // Aplicar el valor total (base + boost)
            addModifier(lowestStat, newBaseValue + gemBoost, false);
            addStatBreakdownToLore(lowestStat, newBaseValue, gemBoost);
        } else {
            // Si no hay boost, aplicar solo el nuevo valor base
            addModifier(lowestStat, newBaseValue, true);
        }

        Component message = Component.text("Nuevo bonus: ", BlessingObject.getLoreColor())
                .append(Component.text(lowestStat.getDisplayName(
                                AuraSkillsApi.get().getMessageManager().getDefaultLanguage()))
                        .color(ItemUtil.getColorOfStat(lowestStat)))
                .append(Component.text(" +")
                        .append(Component.text(newBaseValue))
                        .color(ItemUtil.getColorOfStat(lowestStat)));
        player.sendMessage(ItemConfig.BLESSING_PREFIX.append(message));
    }

    public void rerollExceptHighestStat(Player player) {
        // Obtener la stat más alta válida
        StatModifier highestMod = getHighestStatModifier();
        if (highestMod == null) {
            player.sendMessage(
                    ItemConfig.REDEMPTION_PREFIX.append(Component.text("No hay estadísticas válidas para preservar.")
                            .color(RedemptionObject.getLoreColor())));
            return;
        }

        Stat highestStat = highestMod.type();
        PersistentDataContainer container = getItemMeta().getPersistentDataContainer();

        // Guardar el boost de gema si existe
        int gemBoost = container.getOrDefault(
                new NamespacedKey(PLUGIN, GEM_BOOST_PREFIX + highestStat.name()), PersistentDataType.INTEGER, 0);

        // Obtener el valor base guardado (si existe) o calcularlo
        int baseValue = container.getOrDefault(
                new NamespacedKey(PLUGIN, BASE_STAT_VALUE_PREFIX + highestStat.name()),
                PersistentDataType.INTEGER,
                gemBoost > 0 ? (int) (highestMod.value() - gemBoost) : (int) highestMod.value());

        // Guardar todos los boosts de gemas actuales
        Map<Stat, Integer> allGemBoosts = new HashMap<>();
        for (NamespacedKey key : container.getKeys()) {
            if (key.getKey().startsWith(GEM_BOOST_PREFIX)) {
                String statName =
                        key.getKey().substring(GEM_BOOST_PREFIX.length()).toUpperCase();
                Stat stat = CombinedStats.valueOf(statName);
                int boost = container.get(key, PersistentDataType.INTEGER);
                allGemBoosts.put(stat, boost);
            }
        }

        // Remover todos los modificadores existentes
        removeAllModifierStats();
        // Generar nuevas stats excepto la más alta
        generateStatsExceptHighestStat(highestStat);
        applyStatsToItem();

        // Reaplicar la stat más alta con su valor base y boost si existe
        removeSpecificModifier(highestStat);
        removeSpecificStatLoreLine(highestStat);

        if (gemBoost > 0) {
            // Si tiene boost de gema
            addModifier(highestStat, baseValue + gemBoost, false);
            addStatBreakdownToLore(highestStat, baseValue, gemBoost);

            // Actualizar el valor base en NBT
            ItemMeta meta = getItemMeta();
            container = meta.getPersistentDataContainer();
            container.set(
                    new NamespacedKey(PLUGIN, BASE_STAT_VALUE_PREFIX + highestStat.name()),
                    PersistentDataType.INTEGER,
                    baseValue);
            setItemMeta(meta);
        } else {
            addModifier(highestStat, baseValue, true);
        }

        // Reaplicar todos los boosts de gemas a las nuevas stats
        for (Map.Entry<Stat, Integer> entry : allGemBoosts.entrySet()) {
            Stat stat = entry.getKey();
            if (stat != highestStat) { // No procesar la stat más alta de nuevo
                int boost = entry.getValue();

                StatModifier newMod = getStatModifiers().stream()
                        .filter(mod ->
                                CombinedStats.valueOf(mod.type().name()).equals(CombinedStats.valueOf(stat.name())))
                        .findFirst()
                        .orElse(null);

                if (newMod != null) {
                    removeSpecificModifier(stat);
                    removeSpecificStatLoreLine(stat);
                    int newBaseValue = (int) newMod.value();
                    addModifier(stat, newBaseValue + boost, false);
                    addStatBreakdownToLore(stat, newBaseValue, boost);

                    // Actualizar el valor base en NBT
                    ItemMeta meta = getItemMeta();
                    container = meta.getPersistentDataContainer();
                    container.set(
                            new NamespacedKey(PLUGIN, BASE_STAT_VALUE_PREFIX + stat.name()),
                            PersistentDataType.INTEGER,
                            newBaseValue);
                    setItemMeta(meta);
                } else {
                    addModifier(stat, boost, false);
                }
            }
        }

        // Actualizar el lore
        setLore();

        // Notificar al jugador
        Component message = Component.text("Se conservó: ")
                .color(RedemptionObject.getLoreColor())
                .append(Component.text(highestStat.getDisplayName(
                                AuraSkillsApi.get().getMessageManager().getDefaultLanguage()))
                        .color(TextColor.fromHexString(highestStat
                                .getColor(
                                        AuraSkillsApi.get().getMessageManager().getDefaultLanguage())
                                .replaceAll("[<>]", ""))))
                .append(Component.text(" Nueva calidad: ").append(rarity));
        player.sendMessage(ItemConfig.REDEMPTION_PREFIX.append(message));
    }

    public List<StatModifier> getStatModifiers() {
        return AuraSkillsBukkit.get().getItemManager().getStatModifiers(this, modifierType);
    }

    public List<Integer> getStatValues() {
        return statValues;
    }

    public List<Stat> getAddedStats() {
        return addedStats;
    }

    public abstract void updateLoreWithSockets();
}