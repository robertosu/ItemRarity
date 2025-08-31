package cl.nightcore.itemrarity.abstracted;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.config.CombinedStats;
import cl.nightcore.itemrarity.config.CombinedTraits;
import cl.nightcore.itemrarity.config.ItemConfig;
import cl.nightcore.itemrarity.item.roller.BlessingObject;
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
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static cl.nightcore.itemrarity.ItemRarity.AURA_LOCALE;

public abstract class IdentifiedItem extends ItemStack {

    protected static final String NATIVE_STATMODIFIER = "native";
    protected static final String MONOLITIC_TRAITMODIFIER = "monolitic";
    protected static final String GEM_STATMODIFIER = "gema";
    protected static final int MAX_EXTRA_BONUSES = 2;
    protected static final int MAX_LEVEL = 9;

    protected static final boolean randomOperations = false;

    private static final Component GEMS_HEADER =
            Component.text("Gemas:").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false);
    private static final Component EMPTY_SOCKET =
            Component.text(" ⛶ ").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC,false).append(
                    Component.text("Vacío").color(TextColor.color(0x636363)).decoration(TextDecoration.ITALIC, false));

    protected final ModifierType modifierType;
    protected final ItemType itemType;
    protected final ModifierProvider statProvider;

    // Separar listas para stats gaussianos y no gaussianos
    private final List<Stat> addedStats;
    private final List<Stat> addedGaussStats;
    private final List<Integer> statValues;
    private final List<Integer> gaussStatValues;

    // Nueva estructura para pre-calcular lore de stats
    private final List<Component> preCalculatedStatLines;
    private Component preCalculatedRarity;

    protected Component rarity;
    List<Component> newLore;

    public IdentifiedItem(ItemStack item) {
        super(item);
        this.itemType = ItemUtil.getItemType(item);
        this.statProvider = ItemUtil.getProvider(this.itemType);
        this.modifierType = ItemUtil.getModifierType(this.itemType);
        this.addedStats = new ArrayList<>();
        this.addedGaussStats = new ArrayList<>();
        this.statValues = new ArrayList<>();
        this.gaussStatValues = new ArrayList<>();
        this.preCalculatedStatLines = new ArrayList<>();
    }

    public static double calculateTotalDamage(ItemStack item, double baseDamage) {
        double sharpnessDamage = calculateSharpnessDamage(item);
        return baseDamage + sharpnessDamage;
    }

    private static double calculateSharpnessDamage(ItemStack item) {
        int sharpnessLevel = item.getEnchantmentLevel(Enchantment.SHARPNESS);
        return sharpnessLevel > 0 ? (0.5 * sharpnessLevel + 0.5) : 0;
    }

    public static double calculateAttackSpeed(ItemStack item) {
        double baseSpeed = 0;

        Collection<AttributeModifier> baseModifiers = item.getType().getDefaultAttributeModifiers().get(Attribute.ATTACK_SPEED);

        if (NexoItems.idFromItem(item) == null) {
            for (AttributeModifier modifier : baseModifiers) {
                baseSpeed = modifier.getAmount();
            }
            return baseSpeed + 4;
        }

        ItemMeta meta = item.getItemMeta();
        if (NexoItems.idFromItem(item) != null) {
            if (meta.getAttributeModifiers(Attribute.ATTACK_SPEED) != null) {
                for (AttributeModifier modifier : Objects.requireNonNull(meta.getAttributeModifiers(Attribute.ATTACK_SPEED))) {
                    baseSpeed = modifier.getAmount() + 4;
                    return baseSpeed;
                }
            } else if (meta.getAttributeModifiers(Attribute.ATTACK_SPEED) == null) {
                for (AttributeModifier modifier : baseModifiers) {
                    baseSpeed = modifier.getAmount();
                }
                return baseSpeed + 4;
            }
        }
        return baseSpeed;
    }

    public Component getRarityComponent() {
        return rarity;
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

    // Método para pre-calcular las líneas de lore de stats
    private void preCalculateStatLines() {
        preCalculatedStatLines.clear();

        var sum = 0.0;
        List<Component> tempStatLines = new ArrayList<>();

        // Procesar stats gaussianos
        for (int i = 0; i < addedGaussStats.size(); i++) {
            Stat stat = addedGaussStats.get(i);
            double value = gaussStatValues.get(i);
            sum += value;

            // Crear línea de lore para esta stat
            String valueText = " +" + (int) value;
            Component statLine = Component.text(valueText, ItemUtil.getColorOfStat(stat))
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text(" " + stat.getDisplayName(AURA_LOCALE), TextColor.color(0x636363)))
                    .decoration(TextDecoration.ITALIC, false);
            tempStatLines.add(statLine);
        }

        // Procesar stats normales
        for (int i = 0; i < addedStats.size(); i++) {
            Stat stat = addedStats.get(i);
            double value = statValues.get(i);
            sum += value;

            // Crear línea de lore para esta stat
            String valueText = " +" + (int) value;
            Component statLine = Component.text(valueText, ItemUtil.getColorOfStat(stat))
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text(" " + stat.getDisplayName(AURA_LOCALE), TextColor.color(0x636363)))
                    .decoration(TextDecoration.ITALIC, false);
            tempStatLines.add(statLine);
        }

        // Calcular rareza y agregar al lore pre-calculado
        int totalStats = addedStats.size() + addedGaussStats.size();
        double average = sum / totalStats;
        if (totalStats == 4) {
            average *= 0.85; // Aplicar penalización early game
        }

        this.preCalculatedRarity = RarityCalculator.calculateRarity(average);
        this.rarity = preCalculatedRarity;

        // Agregar header y líneas de stats
        preCalculatedStatLines.add(Component.empty());
        preCalculatedStatLines.add(Component.text("Bonos 1-5: ", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .append(preCalculatedRarity));
        preCalculatedStatLines.addAll(tempStatLines);
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
        // Si tenemos stats pre-calculadas, usar esas
        if (!preCalculatedStatLines.isEmpty()) {
            var sum = 0.0;
            int totalStats = 0;

            // Sumar stats gaussianos
            for (int i = 0; i < addedGaussStats.size(); i++) {
                sum += gaussStatValues.get(i);
                totalStats++;
            }

            // Sumar stats normales
            for (int i = 0; i < addedStats.size(); i++) {
                sum += statValues.get(i);
                totalStats++;
            }

            if (totalStats == 0) {
                throw new IllegalStateException("No hay estadísticas para calcular el promedio.");
            }

            double baseAverage = sum / totalStats;

            // Aplicar penalización solo si tiene 4 stats (early game)
            if (totalStats == 4) {
                return baseAverage * 0.85;
            }

            return baseAverage; // 5 stats, sin penalización
        }

        // Fallback al método original si no hay stats pre-calculadas
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

        // Calcular el promedio base
        double baseAverage = totalValue / statCount;

        // Aplicar penalización solo si tiene 4 stats (early game)
        if (statCount == 4) {
            return baseAverage * 0.85;
        }

        return baseAverage; // 5 stats, sin penalización
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

    protected void getExtraBonusesLines() {
        if (this.getExtraBonuses().isPresent() && !this.getExtraBonuses().get().isEmpty()) {
            newLore.add(Component.empty());
            newLore.add((Component.text("Bonos 6-7:").color(NamedTextColor.GRAY)).decoration(TextDecoration.ITALIC, false));
            for (StatModifier statModifier : this.getExtraBonuses().get()) {
                String valueText = " +" + (int)statModifier.value();
                if (statModifier.operation() == AuraSkillsModifier.Operation.ADD_PERCENT) {
                    valueText = valueText + "%";
                }
                newLore.add(Component.text(valueText, ItemUtil.getColorOfStat(statModifier.stat())).decoration(TextDecoration.ITALIC, false)
                        .append(Component.text(" " + statModifier.stat().getDisplayName(ItemRarity.AURA_LOCALE), TextColor.color(0x636363))
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
            List currentExtraStats = this.getExtraBonuses().map(bonuses -> bonuses.stream().map(modifier -> modifier.type().name()).toList()).orElse(new ArrayList());
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
        this.setItemMeta(AuraSkillsBukkit.get().getItemManager().addStatModifier(this, this.modifierType, stat, StatValueGenerator.generateValueForStat(false), AuraSkillsModifier.Operation.ADD_PERCENT, "extra", false).getItemMeta());
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
                Stat selectedStat = statsPool.get(randomIndex);
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
        List currentExtraStats = this.getExtraBonuses().map(bonuses -> bonuses.stream().map(modifier -> (modifier.type()).name()).toList()).orElse(new ArrayList<>());
        return availableStats.stream().filter(stat -> !currentExtraStats.contains(stat.name())).toList();
    }

    protected Optional<List<StatModifier>> getExtraBonuses() {
        return Optional.ofNullable(AuraSkillsBukkit.get().getItemManager().getStatModifiersById(this, this.modifierType, "extra"));
    }

    public void setNewLore(){
        newLore = new ArrayList<>();

        // Si no tenemos rareza pre-calculada, calcularla normalmente
        if (preCalculatedRarity == null) {
            this.rarity = RarityCalculator.calculateRarity(calculateAverage());
        } else {
            this.rarity = preCalculatedRarity;
        }

        //getMonoliticLines();
        getStatLines();
        getExtraBonusesLines();
        //getRarityLines();
        getRefinedLines();
        getGemsLines();
        getMultipliersLines();
        if (!itemType.isArmor()) {
            getItemMeta().addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            getAttributeLines();
        }
        this.lore(newLore);
        handleCustomName(this.getItemMeta());
    }

    // Método fusionado para líneas de refinado (estadísticas base + rareza)
    protected void getRefinedLines(){
        int currentLevel = this.getLevel();

        // Línea de encabezado con nivel de refinado
        Component refinedHeader;
        if (currentLevel >= MAX_LEVEL) {
            // Si está al máximo
            refinedHeader = Component.text("Refinado: ")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text("[+" + currentLevel + "] ")
                            .color(getLevelColor(currentLevel))
                            .decoration(TextDecoration.ITALIC, false))
                    .append(Component.text("(Máx.)")
                            .color(NamedTextColor.DARK_GRAY)
                            .decoration(TextDecoration.ITALIC, false));
        } else {
            // Si no está al máximo
            refinedHeader = Component.text("Refinado: ")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text("[+" + currentLevel + "] ")
                            .color(getLevelColor(currentLevel))
                            .decoration(TextDecoration.ITALIC, false))
                    .append(Component.text("→ ")
                            .color(NamedTextColor.DARK_GRAY)
                            .decoration(TextDecoration.ITALIC, false))
                    .append(Component.text("[+" + (currentLevel + 1) + "]")
                            .color(getLevelColor(currentLevel+1))
                            .decoration(TextDecoration.ITALIC, false));
        }

        newLore.add(Component.empty());
        newLore.add(refinedHeader);

        // Debug: Verificar cuántos traits tenemos
        List<TraitModifier> traitModifiers = AuraSkillsBukkit.get().getItemManager().getTraitModifiers(this, modifierType);

        // Agregar estadísticas base (traits monolíticos) apiladas verticalmente
        int traitCount = 0;
        for (TraitModifier traitModifier : traitModifiers) {
            traitCount++;

            var currentValue = traitModifier.value();
            var increasePerLevel = determineValueIncreasePerLevelForTrait(traitModifier.trait());
            var nextLevelValue = currentValue + increasePerLevel;

            Component traitLine = Component.text(" "+traitModifier.trait().getDisplayName(AURA_LOCALE))
                    .color(TextColor.color(0x636363))
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text(" +" + getFormattedValue(currentValue, traitModifier.trait()) + " ")
                            .color(getTraitColor(traitModifier.trait()))
                            .decoration(TextDecoration.ITALIC, false));

            // Agregar indicador del siguiente nivel solo si no está al máximo
            if (currentLevel < MAX_LEVEL) {
                Component nextLevelComponent = Component.text(" → ")
                        .color(NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false)
                        .append(Component.text("+" + getFormattedValue(nextLevelValue, traitModifier.trait()))
                                .color(NamedTextColor.DARK_GRAY)
                                .decoration(TextDecoration.ITALIC, false));
                traitLine = traitLine.append(nextLevelComponent);
            }

            newLore.add(traitLine);
        }
    }

    // Método auxiliar para obtener color según el nivel
    private TextColor getLevelColor(int level) {
        return switch (level) {
            case 1 -> TextColor.color(0x9A8772);
            case 2 -> TextColor.color(0x92704E);
            case 3 -> TextColor.color(0x996A28);
            case 4 -> TextColor.color(0xA87924);
            case 5 -> TextColor.color(0xB68921);
            case 6 -> TextColor.color(0xC5991E);
            case 7 -> TextColor.color(0xD3A91B);
            case 8 -> TextColor.color(0xE2B817);
            case 9 -> TextColor.color(0xF0C814);
            default -> NamedTextColor.RED;
        };
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
                                    .color(TextColor.color(0x636363)).decoration(TextDecoration.ITALIC,TextDecoration.State.FALSE)
                                    .append(Component.text("|").color(TextColor.color(0x636363)))
                                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            line = line.append(component);
        }
        newLore.add(Component.empty());
        newLore.add(line);
    }

    protected void getStatLines(){
        // Verificar si tenemos líneas de stats pre-calculadas
        if (!preCalculatedStatLines.isEmpty()) {
            newLore.addAll(preCalculatedStatLines);
            return;
        }

        // Fallback al método original con lookup de AuraSkills
        newLore.add(Component.empty());
        newLore.add(Component.text("Bonos 1-5: ",NamedTextColor.GRAY).decoration(TextDecoration.ITALIC,false).append(rarity));

        for (StatModifier statModifier : AuraSkillsBukkit.get().getItemManager().getStatModifiersById(this, modifierType, NATIVE_STATMODIFIER)) {
            // Determinar si necesita el símbolo de porcentaje
            String valueText = " +" + (int) statModifier.value();
            if (statModifier.operation() == AuraSkillsModifier.Operation.ADD_PERCENT) {
                valueText += "%";
            }

            newLore.add(Component.text(valueText, ItemUtil.getColorOfStat(statModifier.stat())).decoration(TextDecoration.ITALIC,false)
                    .append(Component.text(" "+ statModifier.stat().getDisplayName(AURA_LOCALE),TextColor.color(0x636363))).decoration(TextDecoration.ITALIC,false));
        }
    }

    protected void getGemsLines(){

        // Add socket header
        newLore.add(Component.empty());
        newLore.add(GEMS_HEADER);
        // Add installed gems
        var installedGems = getInstalledGems();
        int availableSockets = getMaxSockets() - installedGems.size();
        for (StatModifier statModifier : installedGems){
            TextColor statColor = ItemUtil.getColorOfStat(statModifier.stat());
            // Construcción del texto con colores aplicados explícitamente, otherwise ExcellentEnchants bugea el lore
            Component gemLine = Component.text(" \uD83D\uDC8E ",statColor) // Color explícito para el símbolo
                    .append(Component.text(statModifier.stat().getDisplayName(AURA_LOCALE))
                            .color(statColor)) // Color explícito para el nombre de la stat
                    .append(Component.text(String.format(" +%d", (int) statModifier.value()))
                            .color(statColor)) // Color explícito para el valor
                    .decoration(TextDecoration.ITALIC, false); // Quitar cursiva
            newLore.add(gemLine);
        }
        // Add empty sockets
        for (int i = 0; i < availableSockets; i++) {
            newLore.add(EMPTY_SOCKET);
        }
    }

    protected abstract int calculateGemValue(int value);

    protected abstract int getMaxSockets();

    protected abstract int getAvailableSockets();

    protected void getMultipliersLines(){

        List<Multiplier> multipliers = AuraSkillsBukkit.get().getItemManager().getMultipliers(this, modifierType);

        if (multipliers.isEmpty()){
            return;
        }else {
            newLore.add(Component.empty());
            newLore.add(Component.text("Multiplicadores XP:",NamedTextColor.GRAY).decoration(TextDecoration.ITALIC,false));
        }

        for (Multiplier multiplier : AuraSkillsBukkit.get().getItemManager().getMultipliers(this, modifierType)){
            if (multiplier.skill() != null) {
                newLore.add(Component.text(" +"+(int) multiplier.value()+"%",TextColor.color(0x62FFF8))
                        .append(Component.text(" XP de "+ multiplier.skill().getDisplayName(AURA_LOCALE),TextColor.color(0x62FFF8)))
                        .decoration(TextDecoration.ITALIC,false));

            }else{
                newLore.add(Component.text(" +"+(int) multiplier.value()+"%",TextColor.color(0x16A46E))
                        .append(Component.text(" XP de Habilidades",TextColor.color(0x16A46E))).decoration(TextDecoration.ITALIC,false));
            }
        }
    }

    protected void getAttributeLines(){

        var meta = this.getItemMeta();

        if(getItemMeta().getAttributeModifiers() == null){
            //trick to turn disable vanilla
            var defaultModifiers = this.getType().getDefaultAttributeModifiers();
            meta.setAttributeModifiers(defaultModifiers);
            this.setItemMeta(meta);
        }

        double baseDamage = 1;

        Collection<AttributeModifier> modifiers = meta.getAttributeModifiers(Attribute.ATTACK_DAMAGE);
        if (modifiers != null) {
            for (AttributeModifier modifier : modifiers) {
                if (modifier.getOperation() == AttributeModifier.Operation.ADD_NUMBER) {
                    baseDamage = baseDamage + modifier.getAmount(); // Usar el valor del modificador
                }
            }
        }

        double totalDamage = calculateTotalDamage(this, baseDamage);
        double attackSpeed = calculateAttackSpeed(this);
        String attackSpeedDisplay = String.format("%.1f", attackSpeed);

        newLore.add(Component.text("          "));
        newLore.add(Component.text("En la mano principal:", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        newLore.add(Component.text(" " +  ItemUtil.AttributesDF.format(totalDamage) + " Daño por ataque", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false));
        newLore.add(Component.text(" " + attackSpeedDisplay + " Velocidad de ataque", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false));

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

    protected static double determineValueIncreasePerLevelForTrait(Trait trait){
        switch(trait.name()){
            case "ATTACK_DAMAGE" -> { return 1.0; }
            case "ATTACK_SPEED" -> { return 0.1; }
            case "DAMAGE_REDUCTION", "HP" -> { return 0.5; }
        }
        return 0;
    }

    public void handleCustomName(ItemMeta meta) {

        int currentLevel = getLevel();
        TextColor levelColor = getLevelColor(currentLevel);
        Component levelSuffix = Component.text(" [+" + currentLevel + "]")
                .color(levelColor)
                .decoration(TextDecoration.ITALIC, false);

        if (NexoItems.idFromItem(this) != null) {
            if (meta.hasCustomName()) {
                String plainText = PlainTextComponentSerializer.plainText().serialize(meta.customName());
                // Remover cualquier patrón de refinado existente
                String cleanedText = removeRefinementPattern(plainText);

                Component component = Component.text(cleanedText, getRarityColor())
                        .decoration(TextDecoration.ITALIC, false)
                        .append(levelSuffix);
                meta.customName(component);
            } else {
                String plainText = PlainTextComponentSerializer.plainText().serialize(meta.itemName());
                // Remover cualquier patrón de refinado existente
                String cleanedText = removeRefinementPattern(plainText);

                Component component = Component.text(cleanedText, getRarityColor())
                        .decoration(TextDecoration.ITALIC, false)
                        .append(levelSuffix);
                meta.itemName(component);
            }
        } else {
            if (meta.hasCustomName()) {
                String plainText = PlainTextComponentSerializer.plainText().serialize(meta.customName());
                // Remover cualquier patrón de refinado existente
                String cleanedText = removeRefinementPattern(plainText);

                Component component = Component.text(cleanedText, getRarityColor())
                        .decoration(TextDecoration.ITALIC, false)
                        .append(levelSuffix);
                meta.customName(component);
            } else {
                String itemTranslationKey = this.translationKey();
                TranslatableComponent translatedName = Component.translatable(itemTranslationKey)
                        .color(getRarityColor())
                        .decoration(TextDecoration.ITALIC, false);

                Component component = translatedName.append(levelSuffix);
                meta.itemName(component);
            }
        }

        meta.setTooltipStyle(RarityCalculator.getTooltip(rarity));
        this.setItemMeta(meta);
    }

    /**
     * Remueve cualquier patrón de refinado existente del texto del nombre del ítem.
     * Busca patrones como "[+1] ", "[+9] ", etc. al inicio del texto.
     *
     * @param text El texto original del nombre del ítem
     * @return El texto limpio sin patrones de refinado
     */
    private String removeRefinementPattern(String text) {
        // Patrón regex para detectar espacio seguido de [+número] al final del string
        // Ejemplo: " [+1]", " [+9]", etc.
        String pattern = "\\s+\\[\\+\\d+\\]$";
        return text.replaceFirst(pattern, "");
    }




    protected void addNativeStatModifierArray(Stat stat, double value, boolean randomOperations) {
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
                    .addStatModifierA(this, modifierType, stat, value, AuraSkillsModifier.Operation.ADD, NATIVE_STATMODIFIER, false)
                    .getItemMeta());
        }
    }





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

    // MÉTODO OPTIMIZADO: rerollStatsEnhanced con pre-cálculo de lore
    public void rerollStatsEnhanced() {
        emptyLore();
        removeModifiers();
        // Limpiar lore pre-calculado
        preCalculatedStatLines.clear();
        preCalculatedRarity = null;

        // Generar nuevas stats
        generateStats();
        // Pre-calcular las líneas de lore mientras aplicamos stats
        preCalculateStatLines();
        // Aplicar nuevas stats
        applyStatsToItem();
        // Generar lore compuesto (usará las líneas pre-calculadas)
        setNewLore();
    }

    // MÉTODO OPTIMIZADO: rerollLowestStat con pre-cálculo de lore
    public void rerollLowestStat(Player player) {
        // Obtener la stat más baja
        StatModifier lowestModifier = getLowestModifier();
        Stat stat = lowestModifier.stat();
        var statModifiers = getNativeStatModifiers();

        emptyLore();
        // Limpiar lore pre-calculado
        preCalculatedStatLines.clear();
        preCalculatedRarity = null;

        // Generar nuevo valor base
        int newValue = StatValueGenerator.generateValueForStat(statProvider.isThisStatGauss(stat));
        statModifiers.remove(lowestModifier);
        removeModifiers();

        // Crear nuevo modificador
        StatModifier newModifier = new StatModifier(NATIVE_STATMODIFIER, stat, newValue, AuraSkillsModifier.Operation.ADD);
        statModifiers.addFirst(newModifier);

        // Pre-calcular líneas de lore usando los modificadores
        preCalculateStatLinesFromModifiers(statModifiers);

        // Aplicar todos los modificadores
        var sum = 0.0;
        for (StatModifier modifier : statModifiers){
            addNativeStatModifier(modifier.stat(), modifier.value(), randomOperations);
            sum += modifier.value();
        }

        this.setNewLore();

        // Notificar al jugador
        Component message = Component.text("Nuevo bonus: ", BlessingObject.getLoreColor())
                .append(Component.text(stat.getDisplayName(AURA_LOCALE)).color(ItemUtil.getColorOfStat(stat)))
                .append(Component.text(" +")
                        .append(Component.text(newValue))
                        .color(ItemUtil.getColorOfStat(stat)));
        player.sendMessage(ItemConfig.BLESSING_PREFIX.append(message));
    }

    // MÉTODO OPTIMIZADO: rerollExceptHighestStat con pre-cálculo de lore
    public void rerollExceptHighestStat(Player player) {
        // Obtener la stat más alta válida
        StatModifier highestMod = getHighestStatModifier();
        Stat stat = highestMod.type();
        double value = highestMod.value();

        // vaciar lore
        emptyLore();
        // Limpiar lore pre-calculado
        preCalculatedStatLines.clear();
        preCalculatedRarity = null;

        // generar las stat deseadas
        generateStatsExceptHighestStat(stat, value);
        // Pre-calcular las líneas de lore
        preCalculateStatLines();

        // Remover todos los modificadores existentes
        removeModifiers();
        // Generar nuevas stats excepto la más alta
        applyStatsToItem();
        // Actualizar el lore
        this.setNewLore();

        // Notificar al jugador
        Component message = Component.text("¡Se conservó ")
                .color(RedemptionObject.getLoreColor())
                .append(Component.text(stat.getDisplayName(AURA_LOCALE))
                        .color(ItemUtil.getColorOfStat(stat)))
                .append(Component.text(" Rareza: ").append(rarity));
        player.sendMessage(ItemConfig.REDEMPTION_PREFIX.append(message));
    }

    // Método auxiliar para pre-calcular líneas desde una lista de modificadores existentes
    private void preCalculateStatLinesFromModifiers(List<StatModifier> statModifiers) {
        preCalculatedStatLines.clear();

        double sum = 0.0;
        List<Component> tempStatLines = new ArrayList<>();

        // Procesar todos los modificadores
        for (StatModifier modifier : statModifiers) {
            sum += modifier.value();

            // Crear línea de lore para esta stat
            String valueText = " +" + (int) modifier.value();
            if (modifier.operation() == AuraSkillsModifier.Operation.ADD_PERCENT) {
                valueText += "%";
            }

            Component statLine = Component.text(valueText, ItemUtil.getColorOfStat(modifier.stat()))
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text(" " + modifier.stat().getDisplayName(AURA_LOCALE), TextColor.color(0x636363)))
                    .decoration(TextDecoration.ITALIC, false);
            tempStatLines.add(statLine);
        }

        // Calcular rareza
        int totalStats = statModifiers.size();
        double average = sum / totalStats;
        if (totalStats == 4) {
            average *= 0.85; // Aplicar penalización early game
        }

        this.preCalculatedRarity = RarityCalculator.calculateRarity(average);
        this.rarity = preCalculatedRarity;

        // Agregar header y líneas de stats
        preCalculatedStatLines.add(Component.empty());
        preCalculatedStatLines.add(Component.text("Bonos 1-5: ", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .append(preCalculatedRarity));
        preCalculatedStatLines.addAll(tempStatLines);
    }

    public List<StatModifier> getNativeStatModifiers() {
        return AuraSkillsBukkit.get().getItemManager().getStatModifiersById(this, modifierType, NATIVE_STATMODIFIER);
    }

    protected abstract List<StatModifier> getInstalledGems();

    protected abstract void setMonoliticStats(int level);
}