package cl.nightcore.itemrarity.abstracted;

import cl.nightcore.itemrarity.classes.StatValueGenerator;
import cl.nightcore.itemrarity.config.ItemConfig;
import cl.nightcore.itemrarity.item.BlessingObject;
import cl.nightcore.itemrarity.item.RedemptionObject;
import cl.nightcore.itemrarity.util.ItemUtil;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.AuraSkillsBukkit;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.StatModifier;
import dev.aurelium.auraskills.api.stat.Stats;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cl.nightcore.itemrarity.ItemRarity.PLUGIN;

public class EnhancedSocketableItem extends IdentifiedItem {
    public static final String GEM_BOOST_PREFIX = "gem_boost_";
    public static final String BASE_STAT_VALUE_PREFIX = "base_stat_";

    public EnhancedSocketableItem(ItemStack item) {
        super(item);
    }


    @Override
    protected void generateStats() {}

    protected void addGemStat(Stat stat, int gemValue) {
        ItemMeta meta = getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        // Obtener los modificadores actuales antes de cualquier cambio
        StatModifier existingMod =
                AuraSkillsBukkit.get().getItemManager().getStatModifiers(this, MODIFIER_TYPE).stream()
                        .filter(mod -> mod.type().equals(stat))
                        .findFirst()
                        .orElse(null);

        // Guardar el valor base del arma si existe y aún no está guardado
        if (existingMod != null
                && !container.has(
                new NamespacedKey(PLUGIN, BASE_STAT_VALUE_PREFIX + stat.name()), PersistentDataType.INTEGER)) {
            container.set(
                    new NamespacedKey(PLUGIN, BASE_STAT_VALUE_PREFIX + stat.name()), PersistentDataType.INTEGER, (int)
                            existingMod.value());
        }

        // Guardar o actualizar el valor de la gema
        container.set(new NamespacedKey(PLUGIN, GEM_BOOST_PREFIX + stat.name()), PersistentDataType.INTEGER, gemValue);

        setItemMeta(meta);

        // Limpiar los modificadores existentes
        removeSpecificModifier(stat);
        removeSpecificStatLoreLine(stat);

        // Obtener el valor base guardado (si existe)
        int baseValue = container.getOrDefault(
                new NamespacedKey(PLUGIN, BASE_STAT_VALUE_PREFIX + stat.name()), PersistentDataType.INTEGER, 0);

        // Aplicar el nuevo valor combinado
        if (baseValue > 0) {
            ItemStack modifiedItem = AuraSkillsBukkit.get()
                    .getItemManager()
                    .addStatModifier(this, MODIFIER_TYPE, stat, baseValue + gemValue, false);
            setItemMeta(modifiedItem.getItemMeta());
            addStatBreakdownToLore(stat, baseValue, gemValue);
        } else {
            addModifier(stat, gemValue, false);
        }

        setLore();
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

    public void rerollStatsEnhanced() {
        // Guardar los boosts de gemas antes de reroll
        Map<Stat, Integer> gemBoosts = new HashMap<>();
        PersistentDataContainer container = getItemMeta().getPersistentDataContainer();

        // Recolectar todos los boosts de gemas
        for (NamespacedKey key : container.getKeys()) {
            if (key.getKey().startsWith(GEM_BOOST_PREFIX)) {
                String statName = key.getKey().substring(GEM_BOOST_PREFIX.length()).toUpperCase();
                Stat stat = Stats.valueOf(statName);
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
                        baseValue
                );
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
    }

    @Override
    public void updateLoreWithSockets() {}


    protected void addModifier(Stat stat, int value, boolean generateLore) {
        this.setItemMeta(AuraSkillsBukkit.get()
                .getItemManager()
                .addStatModifier(this, MODIFIER_TYPE, stat, value, generateLore)
                .getItemMeta());
    }

    public void rerollLowestStat(Player player) {
        Stat lowestStat = getLowestModifier();
        if (lowestStat == null) {
            player.sendMessage(ItemConfig.BLESSING_PREFIX
                    .append(Component.text("No hay estadísticas válidas para rerollear.")
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
                        .color(ItemUtil.getColorOfStat(lowestStat))).append(Component.text(" +").append(Component.text(newBaseValue)).color(ItemUtil.getColorOfStat(lowestStat)));
        player.sendMessage(ItemConfig.BLESSING_PREFIX.append(message));
    }

    public void rerollExceptHighestStat(Player player) {
        // Obtener la stat más alta válida
        StatModifier highestMod = getHighestStatModifier();
        if (highestMod == null) {
            player.sendMessage(ItemConfig.REDEMPTION_PREFIX
                    .append(Component.text("No hay estadísticas válidas para preservar.")
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
                Stat stat = Stats.valueOf(statName);
                int boost = container.get(key, PersistentDataType.INTEGER);
                allGemBoosts.put(stat, boost);
            }
        }

        // Remover todos los modificadores existentes
        removeAllModifierStats();
        // Generar nuevas stats excepto la más alta
        generateStatsExceptHighestStat(highestStat);
        applyStatsToItem();
        // Reaplica la stat más alta con su valor base y boost si existe
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
                        .filter(mod -> mod.type().equals(stat))
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
        setLore();

        Component message = Component.text("Se conservó: ")
                .color(RedemptionObject.getLoreColor())
                .append(Component.text(highestStat.getDisplayName(
                                AuraSkillsApi.get().getMessageManager().getDefaultLanguage()))
                        .color(TextColor.fromHexString(highestStat
                                .getColor(
                                        AuraSkillsApi.get().getMessageManager().getDefaultLanguage())
                                .replaceAll("[<>]", "")))).append(Component.text(" Nueva calidad: ").append(rarity));
        player.sendMessage(ItemConfig.REDEMPTION_PREFIX.append(message));
    }

}