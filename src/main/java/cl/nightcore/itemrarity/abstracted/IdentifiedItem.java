package cl.nightcore.itemrarity.abstracted;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.config.CombinedStats;
import cl.nightcore.itemrarity.config.CombinedTraits;
import cl.nightcore.itemrarity.config.ItemConfig;
import cl.nightcore.itemrarity.item.roller.BlessingObject;
import cl.nightcore.itemrarity.item.roller.IdentifyScroll;
import cl.nightcore.itemrarity.item.roller.MagicObject;
import cl.nightcore.itemrarity.item.roller.RedemptionObject;
import cl.nightcore.itemrarity.rollquality.MainRollQuality;
import cl.nightcore.itemrarity.rollquality.StatValueGenerator;
import cl.nightcore.itemrarity.statprovider.ModifierProvider;
import cl.nightcore.itemrarity.util.ItemType;
import cl.nightcore.itemrarity.util.ItemUtil;
import cl.nightcore.itemrarity.util.RarityCalculator;
import com.nexomc.nexo.api.NexoItems;
import dev.aurelium.auraskills.api.AuraSkillsBukkit;
import dev.aurelium.auraskills.api.item.ModifierType;
import dev.aurelium.auraskills.api.skill.Multiplier;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.StatModifier;
import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.trait.TraitModifier;
import dev.aurelium.auraskills.api.util.AuraSkillsModifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static cl.nightcore.itemrarity.ItemRarity.AURA_LOCALE;

public abstract class IdentifiedItem extends ItemStack {

    protected static final String NATIVE_STATMODIFIER = "native";
    protected static final String MONOLITIC_TRAITMODIFIER = "monolitic";
    protected static final String GEM_STATMODIFIER = "gema";
    protected static final int MAX_EXTRA_BONUSES = 2;

    protected static final boolean randomOperations = false;

    private static final Component GEMS_HEADER =
            Component.text("Gemas:").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false);
    protected final ModifierType modifierType;
    protected final ItemType itemType;
    protected final ModifierProvider statProvider;

    // Separar listas para stats gaussianos y no gaussianos
    private final List<Stat> addedStats;
    private final List<Stat> addedGaussStats;
    private final List<Integer> statValues;
    private final List<Integer> gaussStatValues;
    List<Component> newLore;

    public Component getRarityComponent() {
        return rarity;
    }

    protected Component rarity;

    public IdentifiedItem(ItemStack item) {
        super(item);
        this.itemType = ItemUtil.getItemType(item);
        this.statProvider = ItemUtil.getProvider(this.itemType);
        this.modifierType = ItemUtil.getModifierType(this.itemType);
        this.addedStats = new ArrayList<>();
        this.addedGaussStats = new ArrayList<>();
        this.statValues = new ArrayList<>();
        this.gaussStatValues = new ArrayList<>();
    }

    public void identify() {
        this.setIdentifiedAndLevelNBT(1);
        this.setMaxBonuses(4);
        this.generateStats();
        this.applyStatsToItem();
        this.setMonoliticStats(1);
        this.setNewLore();
    }

    protected void generateStats() {
        int statsCount = getMaxBonuses(); // Número total de stats a generar
        List<Stat> availableStats = statProvider.getAvailableStats();

        // 1. Agregar stats gaussianas
        for (Stat stat : statProvider.getGaussStats()) {
            addedGaussStats.add(stat);
            int value = StatValueGenerator.generateValueForStat(statProvider.isThisStatGauss(stat));
            gaussStatValues.add(value);
        }

        // 2. Calcular cuántas stats adicionales se necesitan
        int remainingStats = statsCount - addedGaussStats.size();

        // 3. Agregar stats aleatorias hasta alcanzar el límite
        for (int i = 0; i < remainingStats; i++) {
            Stat stat;
            do {
                stat = availableStats.get(ThreadLocalRandom.current().nextInt(availableStats.size()));
            } while (addedStats.contains(stat) || addedGaussStats.contains(stat)); // Evitar duplicados en ambas listas

            addedStats.add(stat);
            int value = StatValueGenerator.generateValueForStat(statProvider.isThisStatGauss(stat));
            statValues.add(value);
        }
    }

    protected void removeTraitModifierByName(ItemStack item, Trait trait, String name) {
        this.setItemMeta(AuraSkillsBukkit.get()
                .getItemManager()
                .removeTraitModifier(item, modifierType, trait, name)
                .getItemMeta());
    }

    protected void removeStatModifierByName(Stat stat, String name) {
        this.setItemMeta(AuraSkillsBukkit.get()
                .getItemManager()
                .removeStatModifier(this, modifierType, stat, name)
                .getItemMeta());
    }

    protected StatModifier getLowestModifier() {
        // Obtener todos los modificadores de estadísticas nativas
        List<StatModifier> nativeModifiers =
                AuraSkillsBukkit.get().getItemManager().getStatModifiersById(this, modifierType, NATIVE_STATMODIFIER);

        StatModifier lowestModifier = null;
        double lowestValue = Double.MAX_VALUE;

        // Encontrar el modificador con el valor más bajo
        for (StatModifier modifier : nativeModifiers) {
            if (modifier.value() < lowestValue) {
                lowestValue = modifier.value();
                lowestModifier = modifier;
            }
        }

        return lowestModifier;
    }

    public StatModifier getHighestStatModifier() {
        // Obtener todos los modificadores de estadísticas nativas
        List<StatModifier> nativeModifiers =
                AuraSkillsBukkit.get().getItemManager().getStatModifiersById(this, modifierType, NATIVE_STATMODIFIER);

        StatModifier highestModifier = null;
        double highestValue = Double.MIN_VALUE;

        // Encontrar el modificador con el valor más alto
        for (StatModifier modifier : nativeModifiers) {
            if (modifier.value() > highestValue) {
                highestValue = modifier.value();
                highestModifier = modifier;
            }
        }

        return highestModifier;
    }

    protected void generateStatsExceptHighestStat(Stat excludedStat, double excludedvalue) {
        CombinedStats excludedCombinedStat =
                CombinedStats.valueOf(excludedStat.getId().getKey().toUpperCase());
        int statsCount = getMaxBonuses();
        List<Stat> availableStats = statProvider.getAvailableStats();

        // Limpiar todas las listas
        addedStats.clear();
        addedGaussStats.clear();
        statValues.clear();
        gaussStatValues.clear();

        // 1. Procesar stats Gauss
        for (Stat stat : statProvider.getGaussStats()) {
            CombinedStats currentCombinedStat =
                    CombinedStats.valueOf(stat.getId().getKey().toUpperCase());
            if (!currentCombinedStat.equals(excludedCombinedStat)) {
                addedGaussStats.add(stat);
                int value = StatValueGenerator.generateValueForStat(statProvider.isThisStatGauss(stat));
                gaussStatValues.add(value);
            }
        }

        // Agregar el stat excluido a la lista apropiada
        if (statProvider.isThisStatGauss(excludedStat)) {
            addedGaussStats.add(excludedStat);
            gaussStatValues.add((int) Math.round(excludedvalue));
        } else {
            addedStats.add(excludedStat);
            statValues.add((int) Math.round(excludedvalue));
        }

        // 2. Calcular stats adicionales necesarias
        int currentTotalStats = addedStats.size() + addedGaussStats.size();
        int remainingStats = statsCount - currentTotalStats;

        // 3. Agregar stats aleatorias hasta alcanzar el límite
        for (int i = 0; i < remainingStats; i++) {
            Stat stat;
            do {
                stat = availableStats.get(ThreadLocalRandom.current().nextInt(availableStats.size()));
                CombinedStats currentCombinedStat =
                        CombinedStats.valueOf(stat.getId().getKey().toUpperCase());
                // Verificar que la stat no esté ya añadida y no sea la excluida
            } while (addedStats.contains(stat) || addedGaussStats.contains(stat) ||
                    CombinedStats.valueOf(stat.getId().getKey().toUpperCase()).equals(excludedCombinedStat));

            addedStats.add(stat);
            int value = StatValueGenerator.generateValueForStat(statProvider.isThisStatGauss(stat));
            statValues.add(value);
        }
    }

    protected void removeAllModifierStatsByName(String name) {
        for (StatModifier modifier : getNativeStatModifiers()) {
            removeStatModifierByName(modifier.type(), name);
        }
    }

    protected void applyStatsToItem() {
        var sum = 0.0;

        // Aplicar stats gaussianos
        for (int i = 0; i < addedGaussStats.size(); i++) {
            Stat stat = addedGaussStats.get(i);
            double value = gaussStatValues.get(i);
            sum += value;
            addNativeStatModifier(stat, value, randomOperations);
        }

        // Aplicar stats normales
        for (int i = 0; i < addedStats.size(); i++) {
            Stat stat = addedStats.get(i);
            double value = statValues.get(i);
            sum += value;
            addNativeStatModifier(stat, value, randomOperations);
        }

        // Calcular rareza basada en el promedio total
        int totalStats = addedStats.size() + addedGaussStats.size();
        this.rarity = RarityCalculator.calculateRarity(MainRollQuality.getInstance(), sum / totalStats);
    }

    public void removeModifiers() {
        for (CombinedStats combinedStat : CombinedStats.values()) {
            this.setItemMeta(AuraSkillsBukkit.get()
                    .getItemManager()
                    .removeStatModifier(this, modifierType, combinedStat.getDelegateStat(), NATIVE_STATMODIFIER)
                    .getItemMeta());
        }
    }

    protected void emptyLore() {
        ItemMeta meta = this.getItemMeta();
        List<Component> emptylore = new ArrayList<>();
        meta.lore(emptylore);
        this.setItemMeta(meta);
    }

    public double calculateAverage() {
        // Obtener todos los modificadores de estadísticas nativas
        List<StatModifier> nativeModifiers =
                AuraSkillsBukkit.get().getItemManager().getStatModifiersById(this, modifierType, NATIVE_STATMODIFIER);
        if (nativeModifiers.isEmpty()) {
            throw new IllegalStateException("No hay estadísticas nativas para calcular el promedio.");
        }

        double totalValue = 0;
        int statCount = 0;

        // Sumar los valores de las estadísticas nativas
        for (StatModifier modifier : nativeModifiers) {
            totalValue += modifier.value();
            statCount++;
        }

        // Calcular el promedio
        return totalValue / statCount;
    }

    public void setRarity(double average) {
        // Implementar si es necesario
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

    protected List<Component> getAttributeLines() {
        var lore = this.lore();
        List<Component> lines = new ArrayList<>();

        // Líneas monolíticas del principio (primeras 2)
        lines.add(lore.get(0));  // Primera línea monolítica
        lines.add(lore.get(1));  // Segunda línea monolítica

        if (modifierType.equals(ModifierType.ITEM)) {
            // Obtener las últimas 4 líneas (atributos) en el orden correcto
            int size = lore.size();
            lines.add(lore.get(size - 4));  // Espaciador
            lines.add(lore.get(size - 3));  // "En la mano principal:"
            lines.add(lore.get(size - 2));  // Daño por ataque
            lines.add(lore.get(size - 1));  // Velocidad de ataque
        }

        return lines;
    }

    protected void getExtraBonusesLines() {
        if (this.getExtraBonuses().isPresent() && !this.getExtraBonuses().get().isEmpty()) {
            newLore.add((Component)Component.empty());
            newLore.add((Component.text((String)"Bonos extra:").color((TextColor)NamedTextColor.GRAY)).decoration(TextDecoration.ITALIC, false));
            for (StatModifier statModifier : this.getExtraBonuses().get()) {
                String valueText = "+" + (int)statModifier.value();
                if (statModifier.operation() == AuraSkillsModifier.Operation.ADD_PERCENT) {
                    valueText = valueText + "%";
                }
                newLore.add(Component.text(valueText, ItemUtil.getColorOfStat(statModifier.stat())).decoration(TextDecoration.ITALIC, false)
                        .append(Component.text(" " + statModifier.stat().getDisplayName(ItemRarity.AURA_LOCALE), TextColor.color(0xCDCDCD))
                                .decoration(TextDecoration.ITALIC, false)));
            }
        }
    }

    public int addExtraBonus() {
        int actualSize = 0;
        if (this.getExtraBonuses().isPresent()) {
            actualSize = this.getExtraBonuses().get().size();
        }
        if (actualSize < MAX_EXTRA_BONUSES) {
            List<Stat> availableStats = this.statProvider.getAvailableStats();
            List currentExtraStats = this.getExtraBonuses().map(bonuses -> bonuses.stream().map(modifier -> ((Stat)modifier.type()).name()).toList()).orElse(new ArrayList());
            List<Stat> availableNewStats = availableStats.stream().filter(stat -> !currentExtraStats.contains(stat.name())).toList();
            if (availableNewStats.isEmpty()) {
                return 0;
            }
            Stat stat2 = availableNewStats.get(ThreadLocalRandom.current().nextInt(availableNewStats.size()));
            this.addExtraStat(stat2);
            this.setNewLore();
            return actualSize + 1;
        }
        return 0;
    }

    private void addExtraStat(Stat stat) {
        this.setItemMeta(AuraSkillsBukkit.get().getItemManager().addStatModifier((ItemStack)this, this.modifierType, stat, (double)StatValueGenerator.generateValueForStat(false), AuraSkillsModifier.Operation.ADD_PERCENT, "extra", false).getItemMeta());
    }

    public int rerollExtraBonuses() {
        int actualSize = 0;
        Optional<List<StatModifier>> statModifiers = this.getExtraBonuses();
        if (statModifiers.isPresent()) {
            actualSize = statModifiers.get().size();
        }
        if (actualSize > 0) {
            for (CombinedStats stat : CombinedStats.values()) {
                this.removeStatModifierByName(stat.getDelegateStat(), "extra");
            }
            List<Stat> availableStats = this.statProvider.getAvailableStats();
            ArrayList<Stat> statsPool = new ArrayList<Stat>(availableStats);
            for (int i = 0; i < actualSize && !statsPool.isEmpty(); ++i) {
                int randomIndex = ThreadLocalRandom.current().nextInt(statsPool.size());
                Stat selectedStat = (Stat)statsPool.get(randomIndex);
                this.addExtraStat(selectedStat);
                statsPool.remove(randomIndex);
            }
            this.setNewLore();
            return actualSize;
        }
        return 0;
    }

    private List<Stat> getMissingExtraStats() {
        List<Stat> availableStats = this.statProvider.getAvailableStats();
        List currentExtraStats = this.getExtraBonuses().map(bonuses -> bonuses.stream().map(modifier -> ((Stat)modifier.type()).name()).toList()).orElse(new ArrayList());
        return availableStats.stream().filter(stat -> !currentExtraStats.contains(stat.name())).toList();
    }

    protected Optional<List<StatModifier>> getExtraBonuses() {
        return Optional.ofNullable(AuraSkillsBukkit.get().getItemManager().getStatModifiersById((ItemStack)this, this.modifierType, "extra"));
    }

    protected void setNewLore(){
        newLore = new ArrayList<>();
        getMonoliticLines();
        getStatLines();
        getExtraBonusesLines();
        getRarityLines();
        getGemsLines();
        getMultipliersLines();
        this.lore(newLore);
        if (!itemType.isArmor()) {
            ItemUtil.attributesDisplayInLore(this);
        }
        handleCustomName(this.getItemMeta());
    }


    protected void getMonoliticLines(){
        var line = Component.text("|")
                .color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);

        for (TraitModifier traitModifier : AuraSkillsBukkit.get().getItemManager().getTraitModifiers(this,modifierType)) {
            var added = determineValueIncreasePerLevelForTrait(traitModifier.trait());
            var component =
                    Component.text(" +" + getFormattedValue(traitModifier.value(),traitModifier.trait()) + " ").color(getTraitColor(traitModifier.trait())).decoration(TextDecoration.ITALIC,TextDecoration.State.FALSE)
                            .append(Component.text(traitModifier.trait().getDisplayName(AURA_LOCALE) + " ")
                                    .color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC,TextDecoration.State.FALSE)
                                    .append(Component.text("|").color(NamedTextColor.DARK_GRAY))
                                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            line = line.append(component);
        }
        newLore.add(Component.empty());
        newLore.add(line);
    }

    protected void getStatLines(){
        newLore.add(Component.empty());
        for (StatModifier statModifier : AuraSkillsBukkit.get().getItemManager().getStatModifiersById(this, modifierType, NATIVE_STATMODIFIER)) {
            // Determinar si necesita el símbolo de porcentaje
            String valueText = "+" + (int) statModifier.value();
            if (statModifier.operation() == AuraSkillsModifier.Operation.ADD_PERCENT) {
                valueText += "%";
            }

            newLore.add(Component.text(valueText, ItemUtil.getColorOfStat(statModifier.stat())).decoration(TextDecoration.ITALIC,false)
                    .append(Component.text(" "+ statModifier.stat().getDisplayName(AURA_LOCALE),TextColor.color(0xCDCDCD))).decoration(TextDecoration.ITALIC,false));
        }

    }

    protected void getRarityLines(){
        this.rarity = RarityCalculator.calculateRarity(MainRollQuality.getInstance(),calculateAverage());

        Component ilvl = Component.text("● Refinado: [+" + this.getLevel() + "] ● ")
                .color(NamedTextColor.DARK_GRAY)
                .decorate(TextDecoration.ITALIC);

        Component rarityLine = ilvl.append(Component.text("[")
                .color(rarity.color())
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                .append(rarity)
                .append(Component.text("]")
                        .color(rarity.color())
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)));
        newLore.add(Component.empty());
        newLore.add(rarityLine);
    }

    protected void getGemsLines(){
        // Add socket header
        newLore.add(Component.empty());
        newLore.add(GEMS_HEADER);
        // Add installed gems
        Map<Stat, Integer> installedGems = getInstalledGems();
        for (Map.Entry<Stat, Integer> entry : installedGems.entrySet()) {
            Stat stat = entry.getKey();
            int value = calculateGemValue(entry.getValue()); // calcular valor de la gema basado en su nivel.

            // Color del stat
            TextColor statColor = ItemUtil.getColorOfStat(stat);

            // Construcción del texto con colores aplicados explícitamente, otherwise ExcellentEnchants bugea el lore
            Component gemLine = Component.text(" \uD83D\uDC8E ", statColor) // Color explícito para el símbolo
                    .append(Component.text(stat.getDisplayName(AURA_LOCALE))
                            .color(statColor)) // Color explícito para el nombre de la stat
                    .append(Component.text(String.format(" +%d", value))
                            .color(statColor)) // Color explícito para el valor
                    .decoration(TextDecoration.ITALIC, false); // Quitar cursiva

            newLore.add(gemLine);
        }

        // Add empty sockets
        for (int i = 0; i < getAvailableSockets(); i++) {
            newLore.add(EMPTY_SOCKET);
        }
    }

    protected abstract int calculateGemValue(int value);

    protected abstract int getAvailableSockets();

    private static final Component EMPTY_SOCKET =
            Component.text(" ⛶ Vacío").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false);

    protected void getMultipliersLines(){

        newLore.add(Component.empty());
        List<Multiplier> multipliers = AuraSkillsBukkit.get().getItemManager().getMultipliers(this, modifierType);

        if (multipliers.isEmpty()){
            return;
        }else {
            newLore.add(Component.empty());
            newLore.add(Component.text("Multiplicadores XP:",NamedTextColor.GRAY).decoration(TextDecoration.ITALIC,false));
        }

        for (Multiplier multiplier : AuraSkillsBukkit.get().getItemManager().getMultipliers(this, modifierType)){
            if (multiplier.skill() != null) {
                newLore.add(Component.text("+"+(int) multiplier.value()+"%",TextColor.color(0x62FFF8))
                        .append(Component.text(" XP de "+ multiplier.skill().getDisplayName(AURA_LOCALE),TextColor.color(0x62FFF8)))
                        .decoration(TextDecoration.ITALIC,false));

            }else{
                newLore.add(Component.text("+"+(int) multiplier.value()+"%",TextColor.color(0x16A46E))
                        .append(Component.text(" XP de Habilidades",TextColor.color(0x16A46E))).decoration(TextDecoration.ITALIC,false));
            }
        }
    }

    private TextColor getTraitColor(Trait trait) {
        // Primero convertimos el CombinedTrait a su equivalente en Traits si es necesario
        Trait originalTrait = (trait instanceof CombinedTraits) ?
                ((CombinedTraits) trait).getDelegateTrait() : trait;

        var stats = AuraSkillsBukkit.get().getItemManager().getLinkedStats(originalTrait);
        if (stats.stream().findAny().isPresent()) {
            return ItemUtil.getColorOfStat(stats.stream().findAny().get());
        }
        return NamedTextColor.WHITE;
    }

    private String getFormattedValue(double value, Trait trait){
        Trait originalTrait = (trait instanceof CombinedTraits) ?
                ((CombinedTraits) trait).getDelegateTrait() : trait;

        return AuraSkillsBukkit.get().getItemManager().getFormattedTraitValue(value, originalTrait);
    }

    private double determineValueIncreasePerLevelForTrait(Trait trait){
        switch(trait.name()){
            case "ATTACK_DAMAGE" -> { return 1.0; }
            case "ATTACK_SPEED" -> { return 0.01; }
            case "DAMAGE_REDUCTION", "HP" -> { return 0.5; }
        }
        return 0;
    }

    public void handleCustomName(ItemMeta meta) {
        if (NexoItems.idFromItem(this) != null) {
            if(meta.hasCustomName()){
                String plainText = PlainTextComponentSerializer.plainText().serialize(meta.customName());
                Component component = Component.text(plainText, getRarityColor()).decoration(TextDecoration.ITALIC, false);
                meta.customName(component);
            } else {
                String plainText = PlainTextComponentSerializer.plainText().serialize(meta.itemName());
                Component component = Component.text(plainText, getRarityColor()).decoration(TextDecoration.ITALIC, false);
                meta.customName(null);
                meta.itemName(component);
            }
        } else {
            if (meta.hasCustomName()) {
                Component component = meta.customName();
                component =
                        component.color(getRarityColor()).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
                meta.customName(component);

            } else {
                String itemTranslationKey = this.translationKey();
                TranslatableComponent translatedName =
                        Component.translatable(itemTranslationKey).color(getRarityColor()).decoration(TextDecoration.ITALIC,false);
                meta.itemName(translatedName);
            }
        }
        this.setItemMeta(meta);
    }

    // MÉTtODO COMPLETADO: addNativeStatModifier
    protected void addNativeStatModifier(Stat stat, double value, boolean randomOperations) {
        if (randomOperations && !statProvider.isThisStatGauss(stat)) {
            // Elegir aleatoriamente entre ADD y ADD_PERCENT
            AuraSkillsModifier.Operation operation = ThreadLocalRandom.current().nextBoolean()
                    ? AuraSkillsModifier.Operation.ADD
                    : AuraSkillsModifier.Operation.ADD_PERCENT;

            this.setItemMeta(AuraSkillsBukkit.get()
                    .getItemManager()
                    .addStatModifier(this, modifierType, stat, value, operation, NATIVE_STATMODIFIER, false)
                    .getItemMeta());
        } else {
            // Usar ADD por defecto cuando randomOperations es false
            this.setItemMeta(AuraSkillsBukkit.get()
                    .getItemManager()
                    .addStatModifier(this, modifierType, stat, value, AuraSkillsModifier.Operation.ADD, NATIVE_STATMODIFIER, false)
                    .getItemMeta());
        }
    }

    protected void addMonoliticTraitModifier(ItemStack item, Trait trait, double value) {
        this.setItemMeta(AuraSkillsBukkit.get()
                .getItemManager()
                .addTraitModifier(item, modifierType, trait, value, AuraSkillsModifier.Operation.ADD ,MONOLITIC_TRAITMODIFIER, false)
                .getItemMeta());
    }

    public void rerollStatsEnhanced() {
        var attributeLines = getAttributeLines();
        emptyLore();
        removeModifiers();
        // Generar nuevas stats
        generateStats();
        // Aplicar nuevas stats
        applyStatsToItem();
        // Generar lore compuesto
        setNewLore();
    }

    public void rerollLowestStat(Player player) {
        // Obtener la stat más baja
        StatModifier lowestModifier = getLowestModifier();
        Stat stat = lowestModifier.stat();
        var attributeLines = getAttributeLines();
        var statModifiers = getNativeStatModifiers();
        emptyLore();
        // Generar nuevo valor base
        int newValue = StatValueGenerator.generateValueForStat(statProvider.isThisStatGauss(stat));
        statModifiers.remove(lowestModifier);
        removeModifiers();
        statModifiers.addFirst(new StatModifier(NATIVE_STATMODIFIER, stat, newValue, AuraSkillsModifier.Operation.ADD));
        var sum = 0.0;
        for (StatModifier modifier : statModifiers){
            addNativeStatModifier(modifier.stat(),modifier.value(), randomOperations);
            sum = sum + modifier.value();
        }

        this.rarity = RarityCalculator.calculateRarity(MainRollQuality.getInstance(),sum);

        this.setNewLore();

        // Notificar al jugador
        Component message = Component.text("Nuevo bonus: ", BlessingObject.getLoreColor())
                .append(Component.text(stat.getDisplayName(AURA_LOCALE)).color(ItemUtil.getColorOfStat(stat)))
                .append(Component.text(" +")
                        .append(Component.text(newValue))
                        .color(ItemUtil.getColorOfStat(stat)));
        player.sendMessage(ItemConfig.BLESSING_PREFIX.append(message));
    }

    public void rerollExceptHighestStat(Player player) {
        // Obtener la stat más alta válida
        StatModifier highestMod = getHighestStatModifier();
        Stat stat = highestMod.type();
        double value = highestMod.value();
        var attributeLines = getAttributeLines();
        // vaciar lore
        emptyLore();
        // generar las stat deseadas
        generateStatsExceptHighestStat(stat, value);
        // Remover todos los modificadores existentes
        removeModifiers();
        // Generar nuevas stats excepto la más alta
        applyStatsToItem();
        // Actualizar el lore
        this.setNewLore();
        // re aplicar lineas de abajo y de arriba
        // Notificar al jugador
        Component message = Component.text("¡Se conservó ")
                .color(RedemptionObject.getLoreColor())
                .append(Component.text(stat.getDisplayName(AURA_LOCALE))
                        .color(ItemUtil.getColorOfStat(stat)))
                .append(Component.text(" Rareza: ").append(rarity));
        player.sendMessage(ItemConfig.REDEMPTION_PREFIX.append(message));
    }

    public List<StatModifier> getNativeStatModifiers() {
        return AuraSkillsBukkit.get().getItemManager().getStatModifiersById(this, modifierType, NATIVE_STATMODIFIER);
    }

    // Getters para las nuevas listas separadas
    public List<Integer> getStatValues() {
        return statValues;
    }

    public List<Integer> getGaussStatValues() {
        return gaussStatValues;
    }

    public List<Stat> getAddedStats() {
        return addedStats;
    }

    public List<Stat> getAddedGaussStats() {
        return addedGaussStats;
    }

    protected abstract Map<Stat, Integer> getInstalledGems();

    protected abstract void setMonoliticStats(int level);
}