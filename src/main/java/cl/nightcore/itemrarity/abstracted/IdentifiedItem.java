package cl.nightcore.itemrarity.abstracted;

import cl.nightcore.itemrarity.config.CombinedStats;
import cl.nightcore.itemrarity.config.ItemConfig;
import cl.nightcore.itemrarity.item.BlessingObject;
import cl.nightcore.itemrarity.item.IdentifyScroll;
import cl.nightcore.itemrarity.item.MagicObject;
import cl.nightcore.itemrarity.item.RedemptionObject;
import cl.nightcore.itemrarity.rollquality.MainRollQuality;
import cl.nightcore.itemrarity.rollquality.StatValueGenerator;
import cl.nightcore.itemrarity.statprovider.ModifierProvider;
import cl.nightcore.itemrarity.util.ItemUtil;
import cl.nightcore.itemrarity.util.RarityCalculator;
import com.nexomc.nexo.api.NexoItems;
import dev.aurelium.auraskills.api.AuraSkillsBukkit;
import dev.aurelium.auraskills.api.item.ModifierType;
import dev.aurelium.auraskills.api.skill.Multiplier;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.StatModifier;
import dev.aurelium.auraskills.api.trait.Trait;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static cl.nightcore.itemrarity.ItemRarity.AURA_LOCALE;

public abstract class IdentifiedItem extends ItemStack {

    private final List<Stat> addedStats;
    private final List<Integer> statValues;
    protected static final String NATIVE_STATMODIFIER = "native";
    protected static final String MONOLITIC_TRAITMODIFIER = "monolitic";
    protected static final String GEM_STATMODIFIER = "gema";

    protected final ModifierType modifierType;
    protected final ModifierProvider statProvider;
    protected Component rarity;
    protected boolean identified;

    public IdentifiedItem(ItemStack item) {
        super(item);
        this.statProvider = ItemUtil.getProvider(item);
        this.modifierType = ItemUtil.getModifierType(item);
        this.addedStats = new ArrayList<>();
        this.statValues = new ArrayList<>();
    }

    public void identify(Player player) {
        this.identified = false;
        setIdentifiedAndLevelNBT(1);
        setMaxBonuses(5);
        generateStats();
        applyStatsToItem();
        setLore();
        if (ItemUtil.getItemType(this).equals("Weapon")) {
            ItemUtil.attributesDisplayInLore(this);
        }
        setMonoliticStats(1);


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
            int value = StatValueGenerator.generateValueForStat(statProvider.isThisStatGauss(stat));
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
            int value = StatValueGenerator.generateValueForStat(statProvider.isThisStatGauss(stat));
            getStatValues().add(value);
        }
    }

 /*   protected void removeSpecificStatModifier(Stat stat) {
        this.setItemMeta(AuraSkillsBukkit.get()
                .getItemManager()
                .removeStatModifier(this, modifierType, stat, NATIVE_STATMODIFIER, false)
                .getItemMeta());
    }
*/
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

/*    protected void removeSpecificModifierLoreLine(StatModifier lowesModifier) {
        ItemMeta meta = this.getItemMeta();
        @Nullable List<Component> lore = meta.lore();
        if (lore != null) {
            String statDisplayName = lowesModifier.stat().getDisplayName(AURA_LOCALE);
            String modifierValue = String.valueOf((int) Math.round(lowesModifier.value()));
            lore.removeIf(line ->
                    line.toString().contains(statDisplayName) && line.toString().contains(modifierValue));
            meta.lore(lore);
            this.setItemMeta(meta);
        }
    }*/

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
        ModifierProvider statProvider = ItemUtil.getProvider(this);
        List<Stat> availableStats = statProvider.getAvailableStats();

        addedStats.clear();
        statValues.clear();

        // 1. Procesar stats Gauss
        for (Stat stat : statProvider.getGaussStats()) {
            CombinedStats currentCombinedStat =
                    CombinedStats.valueOf(stat.getId().getKey().toUpperCase());
            if (!currentCombinedStat.equals(excludedCombinedStat)) {
                getAddedStats().add(stat);
                int value =
                        StatValueGenerator.generateValueForStat(statProvider.isThisStatGauss(stat));
                getStatValues().add(value);
            }
        }

        getAddedStats().add(excludedStat);
        getStatValues().add((int) Math.round(excludedvalue));

        // 2. Calcular stats adicionales necesarias
        int remainingStats = statsCount - getAddedStats().size();

        // 3. Agregar stats aleatorias hasta alcanzar el límite
        for (int i = 0; i < remainingStats; i++) {
            Stat stat;
            do {
                stat = availableStats.get(ThreadLocalRandom.current().nextInt(availableStats.size()));
                CombinedStats currentCombinedStat =
                        CombinedStats.valueOf(stat.getId().getKey().toUpperCase());
                // Verificar que la stat no esté ya añadida y no sea la excluida
            } while (getAddedStats().contains(stat)
                    || CombinedStats.valueOf(stat.getId().getKey().toUpperCase())
                            .equals(excludedCombinedStat));

            getAddedStats().add(stat);
            int value = StatValueGenerator.generateValueForStat(statProvider.isThisStatGauss(stat));
            getStatValues().add(value);
        }
    }

    protected void removeAllModifierStatsByName(String name) {
        for (StatModifier modifier : getNativeStatModifiers()) {
            removeStatModifierByName(modifier.type(), name);
        }
    }



    protected void applyStatsToItem() {
        for (int i = this.addedStats.size() - 1; i >= 0; i--) {
            Stat stat = this.addedStats.get(i);
            double value = this.statValues.get(i);
            addNativeStatModifier(stat, value);
        }
    }

    protected void reApplyStatsToItem(List<StatModifier> stats) {
        for (int i = stats.size() - 1; i >= 0; i--) {
            Stat stat = stats.get(i).stat();
            double value = stats.get(i).value();
            addNativeStatModifier(stat, value);
        }
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
        ItemMeta meta = getItemMeta();
        List<Component> emptylore = new ArrayList<>();
        meta.lore(emptylore);
        setItemMeta(meta);
    }

    protected void reApplyMultipliers() {
        var multipliers = AuraSkillsBukkit.get().getItemManager().getMultipliers(this, modifierType);
        if (!multipliers.isEmpty()) {
            for (Multiplier multiplier : multipliers) {
                this.setItemMeta(AuraSkillsBukkit.get()
                        .getItemManager()
                        .removeMultiplier(this, modifierType, multiplier.skill())
                        .getItemMeta());
                this.setItemMeta(AuraSkillsBukkit.get()
                        .getItemManager()
                        .removeMultiplierLore(multiplier,this).getItemMeta());
                this.setItemMeta(AuraSkillsBukkit.get()
                        .getItemManager()
                        .addMultiplier(this, modifierType, multiplier.skill(), multiplier.value(),true)
                        .getItemMeta());
            }
        }
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
        this.rarity = RarityCalculator.calculateRarity(MainRollQuality.getInstance(), average);
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
        ItemMeta meta = this.getItemMeta();
        @Nullable List<Component> lore;
        if (meta.lore() != null) {
            lore = meta.lore();
        } else {
            lore = new ArrayList<>();
        }
        // Eliminar líneas de rareza existentes
        lore.removeIf(line -> line.toString().contains("●") || line.toString().contains("|"));

        // Encontrar el índice después de la última línea que comienza con "+"
        int lastStatIndex = -1;
        for (int i = 0; i < lore.size(); i++) {
            String line = PlainTextComponentSerializer.plainText().serialize(lore.get(i));
            if (line.startsWith("+")) {
                lastStatIndex = i;
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
        if (insertIndex < lore.size()) {
            lore.add(insertIndex, rarityLine);
        } else {
            lore.add(rarityLine);
        }
        meta.lore(lore);
        this.setItemMeta(meta);

        handleCustomName();

        updateLoreWithSockets();
    }

   /* protected List<Component> getAttributeLines() {
        var lore = this.lore();
        List<Component> lines = new ArrayList<>();
        // Tomamos la línea monolítica del principio
        lines.add(lore.getFirst());
        lines.add(lore.get(1));



        if (ItemUtil.getModifierType(this).equals(ModifierType.ITEM)) {
            // Tomamos las líneas de atributos del final pero en orden correcto
            List<Component> reversed = new ArrayList<>(lore);
            Collections.reverse(reversed);
            lines.add(reversed.get(0));     // Velocidad de ataque
            lines.add(reversed.get(1));     // Daño por ataque
            lines.add(reversed.get(2));     // "En la mano principal:"
            lines.add(reversed.get(3));     // Espaciador
        }
        return lines;
    }*/


    protected List<Component> getAttributeLines() {
        var lore = this.lore();
        List<Component> lines = new ArrayList<>();

        // Líneas monolíticas del principio (primeras 2)
        lines.add(lore.get(0));  // Primera línea monolítica
        lines.add(lore.get(1));  // Segunda línea monolítica

        if (ItemUtil.getModifierType(this).equals(ModifierType.ITEM)) {
            // Obtener las últimas 4 líneas (atributos) en el orden correcto
            int size = lore.size();
            lines.add(lore.get(size - 4));  // Espaciador
            lines.add(lore.get(size - 3));  // "En la mano principal:"
            lines.add(lore.get(size - 2));  // Daño por ataque
            lines.add(lore.get(size - 1));  // Velocidad de ataque
        }

        return lines;
    }

    public void handleCustomName() {
        var meta = this.getItemMeta();
        if (NexoItems.idFromItem(this) != null) {
            if(meta.hasCustomName()){
                String plainText = PlainTextComponentSerializer.plainText().serialize(meta.customName());
                Component component = Component.text(plainText, getRarityColor()).decoration(TextDecoration.ITALIC, false);
                meta.customName(component);
            } else {
            String plainText = PlainTextComponentSerializer.plainText().serialize(meta.itemName());
            Component component = Component.text(plainText, getRarityColor()).decoration(TextDecoration.ITALIC, false);
            meta.customName(component);
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
                        Component.translatable(itemTranslationKey).color(getRarityColor());
                Component newName = ItemUtil.reset.append(translatedName);
                meta.itemName(newName);
            }
        }
        setItemMeta(meta);
    }

    protected void addNativeStatModifier(Stat stat, double value) {
        this.setItemMeta(AuraSkillsBukkit.get()
                .getItemManager()
                .addStatModifier(this, modifierType, stat, value ,AuraSkillsModifier.Operation.ADD, NATIVE_STATMODIFIER, true)
                .getItemMeta());
    }

    protected void addMonoliticTraitModifier(ItemStack item, Trait trait, double value) {
        this.setItemMeta(AuraSkillsBukkit.get()
                .getItemManager()
                .addTraitModifier(item, modifierType, trait, value, AuraSkillsModifier.Operation.ADD ,MONOLITIC_TRAITMODIFIER, false)
                .getItemMeta());
    }

    public void rerollStatsEnhanced(Player player) {
        var attributeLines = getAttributeLines();
        emptyLore();
        removeModifiers();
        // Generar nuevas stats
        generateStats();
        // Aplicar nuevas stats
        applyStatsToItem();
        // Generar lore compuesto
        setLore();

        appendAttributeLines(attributeLines);

        reApplyMultipliers();

        Component message = Component.text("¡El objeto cambió! Rareza: ", MagicObject.getLoreColor());
        player.sendMessage(ItemConfig.REROLL_PREFIX.append(message).append(rarity));
    }

/*    protected void appendAttributeLines(List<Component> attributeLines) {

        ItemMeta meta = this.getItemMeta();
        @Nullable List<Component> lore = meta.lore();

        // Agregar línea monolítica al principio
        lore.addFirst(attributeLines.get(1));
        lore.addFirst(attributeLines.get(0));

        if (ItemUtil.getItemType(this).equals("Weapon")) {
            // Agregar líneas de atributos al final en orden correcto, en caso de ser arma que tiene este lore
            lore.add(attributeLines.get(4));    // Espaciador
            lore.add(attributeLines.get(3));    // "En la mano principal:"
            lore.add(attributeLines.get(2));    // Daño por ataque
            lore.add(attributeLines.get(1));    // Velocidad de ataque
        }

        meta.lore(lore);
        this.setItemMeta(meta);
    }*/

    protected void appendAttributeLines(List<Component> attributeLines) {
        ItemMeta meta = this.getItemMeta();
        @Nullable List<Component> lore = meta.lore();

        // Agregar líneas monolíticas al principio
        lore.addFirst(attributeLines.get(1));  // Segunda línea monolítica
        lore.addFirst(attributeLines.get(0));  // Primera línea monolítica

        if (ItemUtil.getItemType(this).equals("Weapon")) {
            // Agregar líneas de atributos al final en el orden correcto
            lore.add(attributeLines.get(2));  // Espaciador
            lore.add(attributeLines.get(3));  // "En la mano principal:"
            lore.add(attributeLines.get(4));  // Daño por ataque
            lore.add(attributeLines.get(5));  // Velocidad de ataque
        }

        meta.lore(lore);
        this.setItemMeta(meta);
    }

    protected void appendTraitLines(List<Component> attributeLines) {
        ItemMeta meta = this.getItemMeta();
        @Nullable List<Component> lore = meta.lore();
        // Agregar líneas monolíticas al principio
        lore.addFirst(attributeLines.getFirst());  // Primera línea monolítica

        meta.lore(lore);
        this.setItemMeta(meta);
    }

    protected void setSecondSpacer(List<Component> attributeLines) {
        ItemMeta meta = this.getItemMeta();
        List<Component> lore = meta.lore();

        if (lore == null) {
            lore = new ArrayList<>();
        }

        // Verificar si ya existe un espaciador en la posición 1 (después de la línea monolítica)
        if (lore.size() > 1) {
            String secondLine = PlainTextComponentSerializer.plainText().serialize(lore.get(1));
            // Si la segunda línea NO es el espaciador de 9 espacios, lo agregamos
            if (!secondLine.equals("         ")) { // 9 espacios
                lore.add(1, Component.text("         ")); // Insertar espaciador en posición 1
            }
        } else if (lore.size() == 1) {
            // Si solo hay una línea (la monolítica), agregar el espaciador
            lore.add(Component.text("         ")); // 9 espacios
        }

        meta.lore(lore);
        this.setItemMeta(meta);
    }

    public void rerollLowestStat(Player player) {
        // Obtener la stat más baja
        StatModifier lowestModifier = getLowestModifier();
        Stat stat = lowestModifier.stat();

        var attributeLines = getAttributeLines();

        // Remover la stat actual
        removeStatModifierByName(lowestModifier.stat(), NATIVE_STATMODIFIER);

        // Generar nuevo valor base
        int newBaseValue =
                StatValueGenerator.generateValueForStat(statProvider.isThisStatGauss(stat));

        // Aplicar el nuevo valor base como una stat nativa
        addNativeStatModifier(stat, newBaseValue);

        emptyLore();

        var statModifiers = getNativeStatModifiers();

        for (StatModifier modifier : statModifiers){
            addNativeStatModifier(modifier.stat(),modifier.value());
        }

        setLore();

        appendAttributeLines(attributeLines);

        reApplyMultipliers();

        // Notificar al jugador

        Component message = Component.text("Nuevo bonus: ", BlessingObject.getLoreColor())
                .append(Component.text(stat.getDisplayName(AURA_LOCALE)).color(ItemUtil.getColorOfStat(stat)))
                .append(Component.text(" +")
                        .append(Component.text(newBaseValue))
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
        setLore();
        // re aplicar lineas de abajo y de arriba
        appendAttributeLines(attributeLines);
        // re aplicar multiplicadores y su lore en el indice correpospondiente
        reApplyMultipliers();
        // Notificar al jugador
        Component message = Component.text("¡Se conservó ")
                .color(RedemptionObject.getLoreColor())
                .append(Component.text(stat.getDisplayName(AURA_LOCALE))
                        .color(TextColor.fromHexString(
                                stat.getColor(AURA_LOCALE).replaceAll("[<>]", ""))))
                .append(Component.text(" Rareza: ").append(rarity));
        player.sendMessage(ItemConfig.REDEMPTION_PREFIX.append(message));
    }

    public List<StatModifier> getNativeStatModifiers() {
        return AuraSkillsBukkit.get().getItemManager().getStatModifiersById(this, modifierType, NATIVE_STATMODIFIER);
    }

    public List<Integer> getStatValues() {
        return statValues;
    }

    public List<Stat> getAddedStats() {
        return addedStats;
    }

    protected abstract void updateLoreWithSockets();

    protected abstract void setMonoliticStats(int level);
}