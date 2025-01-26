package cl.nightcore.itemrarity.abstracted;

import cl.nightcore.itemrarity.classes.StatValueGenerator;
import cl.nightcore.itemrarity.config.CombinedStats;
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
import static cl.nightcore.itemrarity.util.ItemUtil.getStatProvider;
import static cl.nightcore.itemrarity.util.ItemUtil.random;

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
        StatModifier existingMod = getStatModifiers().stream()
                .filter(mod -> {
                    try {
                        // Comparar directamente los nombres de las stats
                        return CombinedStats.valueOf(mod.type().name()).equals(CombinedStats.valueOf(stat.name()));
                    } catch (IllegalArgumentException e) {
                        // Si la stat no existe en CombinedStats, ignorarla
                        return false;
                    }
                })
                .findFirst()
                .orElse(null);

        if (existingMod == null) {
            System.out.println("No se encontró StatModifier para la estadística: " + stat.name());
        }

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
                String statName = key.getKey().substring(GEM_BOOST_PREFIX.length()).toUpperCase();
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
                        .filter(mod -> CombinedStats.valueOf(mod.type().name()).equals(CombinedStats.valueOf(stat.name())))
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
                                .replaceAll("[<>]", "")))).append(Component.text(" Nueva calidad: ").append(rarity));
        player.sendMessage(ItemConfig.REDEMPTION_PREFIX.append(message));
    }

    public boolean addRandomMissingStat(Player player) {

        if (this.getMaxBonuses() != 6) {
            try {
                // Verificar si ya se han alcanzado 6 stats (excluyendo las stats que son solo por gema)
                int nativeStatsCount = getNativeStatsCount();
                if (nativeStatsCount >= 6) {
                    player.sendMessage(ItemConfig.PLUGIN_PREFIX
                            .append(Component.text("El ítem ya tiene el máximo de estadísticas permitidas (6).")
                                    .color(NamedTextColor.RED)));
                    System.out.println("[ItemRarity] No se agregó una nueva stat: el ítem ya tiene 6 stats nativas.");
                    return false;
                }
                this.setMaxBonuses(6);
                // Obtener todas las stats disponibles
                List<Stat> availableStats = getStatProvider(this).getAvailableStats();
                System.out.println("[ItemRarity] Stats disponibles: " + availableStats);

                // Obtener las stats actuales en el ítem (normalizadas)
                List<String> currentStatsNormalized = getStatModifiers().stream()
                        .map(modifier -> modifier.type().name()) // Usar el nombre de la stat
                        .toList();
                System.out.println("[ItemRarity] Stats actuales en el ítem (normalizadas): " + currentStatsNormalized);

                // Filtrar las stats que no están presentes en el ítem (o que solo están presentes por una gema)
                List<Stat> missingStats = new ArrayList<>();
                PersistentDataContainer container = getItemMeta().getPersistentDataContainer();

                for (Stat stat : availableStats) {
                    // Normalizar el nombre de la stat
                    String statName = stat.name();

                    // Verificar si la stat ya está presente en el ítem (excluyendo las stats de gemas)
                    boolean isStatPresent = currentStatsNormalized.contains(statName);

                    // Verificar si la stat tiene un valor base almacenado en el NBT
                    boolean hasBaseValue = container.has(new NamespacedKey(PLUGIN, BASE_STAT_VALUE_PREFIX + statName), PersistentDataType.INTEGER);

                    // Verificar si la stat está presente solo por una gema
                    boolean isStatFromGem = container.has(new NamespacedKey(PLUGIN, GEM_BOOST_PREFIX + statName), PersistentDataType.INTEGER);

                    // Logging detallado para cada stat
                    System.out.println("[ItemRarity] Verificando stat: " + statName +
                            ", isStatPresent: " + isStatPresent +
                            ", hasBaseValue: " + hasBaseValue +
                            ", isStatFromGem: " + isStatFromGem);

                    // Si la stat no está presente en absoluto, o si está presente solo por una gema (sin valor base), es candidata para ser agregada
                    if (!isStatPresent || (isStatFromGem && !hasBaseValue)) {
                        missingStats.add(stat);
                        System.out.println("[ItemRarity] Stat añadida a missingStats: " + statName);
                    }
                }

                System.out.println("[ItemRarity] Stats faltantes: " + missingStats);

                // Si no hay stats faltantes, notificar al jugador
                if (missingStats.isEmpty()) {
                    player.sendMessage(ItemConfig.PLUGIN_PREFIX
                            .append(Component.text("No hay estadísticas faltantes para agregar.")
                                    .color(NamedTextColor.RED)));
                    System.out.println("[ItemRarity] No se agregó una nueva stat: no hay stats faltantes.");
                    return false;
                }

                // Elegir una stat aleatoria de las faltantes

                Stat statToAdd = missingStats.get(random.nextInt(missingStats.size()));
                System.out.println("[ItemRarity] Stat seleccionada para agregar: " + statToAdd.name());

                // Verificar si la stat está presente solo por una gema
                boolean isStatFromGem = container.has(new NamespacedKey(PLUGIN, GEM_BOOST_PREFIX + statToAdd.name()), PersistentDataType.INTEGER);

                if (isStatFromGem) {
                    // Si la stat está presente solo por una gema, obtener el boost de la gema
                    int gemBoost = container.getOrDefault(new NamespacedKey(PLUGIN, GEM_BOOST_PREFIX + statToAdd.name()), PersistentDataType.INTEGER, 0);
                    System.out.println("[ItemRarity] Stat presente solo por gema. Boost de gema: " + gemBoost);

                    // Generar un valor base para la stat
                    int baseValue = StatValueGenerator.generateValueForStat(getRollQuality(), statProvider.isThisStatGauss(statToAdd));
                    System.out.println("[ItemRarity] Valor base generado: " + baseValue);

                    // Guardar el valor base en NBT
                    ItemMeta meta = getItemMeta();
                    container = meta.getPersistentDataContainer();
                    container.set(new NamespacedKey(PLUGIN, BASE_STAT_VALUE_PREFIX + statToAdd.name()), PersistentDataType.INTEGER, baseValue);
                    setItemMeta(meta);

                    // Aplicar el valor total (base + boost de gema)
                    addModifier(statToAdd, baseValue + gemBoost, false);
                    addStatBreakdownToLore(statToAdd, baseValue, gemBoost);

                    // Notificar al jugador
                    Component message = Component.text("Se ha agregado la estadística: ", NamedTextColor.GREEN)
                            .append(Component.text(statToAdd.getDisplayName(AuraSkillsApi.get().getMessageManager().getDefaultLanguage()))
                                    .color(ItemUtil.getColorOfStat(statToAdd)))
                            .append(Component.text(" +" + baseValue)
                                    .color(ItemUtil.getColorOfStat(statToAdd)));
                    player.sendMessage(ItemConfig.PLUGIN_PREFIX.append(message));
                } else {
                    // Si la stat no está presente en absoluto, agregarla con un valor base
                    int baseValue = StatValueGenerator.generateValueForStat(getRollQuality(), statProvider.isThisStatGauss(statToAdd));
                    System.out.println("[ItemRarity] Valor base generado: " + baseValue);

                    // Guardar el valor base en NBT
                    ItemMeta meta = getItemMeta();
                    container = meta.getPersistentDataContainer();
                    container.set(new NamespacedKey(PLUGIN, BASE_STAT_VALUE_PREFIX + statToAdd.name()), PersistentDataType.INTEGER, baseValue);
                    setItemMeta(meta);

                    // Aplicar el valor base
                    addModifier(statToAdd, baseValue, true);

                    // Notificar al jugador
                    Component message = Component.text("Se ha agregado la estadística: ", NamedTextColor.GREEN)
                            .append(Component.text(statToAdd.getDisplayName(AuraSkillsApi.get().getMessageManager().getDefaultLanguage()))
                                    .color(ItemUtil.getColorOfStat(statToAdd)))
                            .append(Component.text(" +" + baseValue)
                                    .color(ItemUtil.getColorOfStat(statToAdd)));
                    player.sendMessage(ItemConfig.BLESSING_BALL_PREFIX.append(message));
                }

                // Actualizar el lore del ítem
                setLore();
                updateLoreWithSockets();
                System.out.println("[ItemRarity] Stat agregada exitosamente: " + statToAdd.name());
            } catch (Exception e) {
                // Manejar cualquier excepción inesperada
                System.err.println("[ItemRarity] Error al agregar una stat aleatoria al ítem:");
                e.printStackTrace();
                player.sendMessage(ItemConfig.BLESSING_BALL_PREFIX
                        .append(Component.text("Ocurrió un error al agregar la estadística. Por favor, contacta a un administrador.")
                                .color(NamedTextColor.RED)));
            }
        }
        return true;
    }
    private int getNativeStatsCount() {
        int count = 0;
        PersistentDataContainer container = getItemMeta().getPersistentDataContainer();

        for (StatModifier modifier : getStatModifiers()) {
            String statName = modifier.type().name();

            // Obtener el valor base de la stat
            int baseValue = container.getOrDefault(new NamespacedKey(PLUGIN, BASE_STAT_VALUE_PREFIX + statName), PersistentDataType.INTEGER, 0);

            // Obtener el boost de gema de la stat
            int gemBoost = container.getOrDefault(new NamespacedKey(PLUGIN, GEM_BOOST_PREFIX + statName), PersistentDataType.INTEGER, 0);

            // Calcular el valor base real (restando el gemBoost al StatModifier)
            double statModifierValue = modifier.value();
            double actualBaseValue = statModifierValue - gemBoost;

            // Determinar si la stat es nativa
            boolean isNative = actualBaseValue > 0; // Es nativa si tiene un valor base real

            // Logging detallado
            System.out.println("[ItemRarity] Verificando stat: " + statName +
                    ", statModifierValue: " + statModifierValue +
                    ", baseValue: " + baseValue +
                    ", gemBoost: " + gemBoost +
                    ", actualBaseValue: " + actualBaseValue +
                    ", isNative: " + isNative);

            // Si la stat es nativa, incrementar el contador
            if (isNative) {
                count++;
            }
        }

        System.out.println("[ItemRarity] Número de stats nativas: " + count);
        return count;
    }

}