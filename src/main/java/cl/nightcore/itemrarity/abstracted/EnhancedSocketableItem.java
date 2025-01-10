package cl.nightcore.itemrarity.abstracted;

import cl.nightcore.itemrarity.abstracted.IdentifiedItem;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.AuraSkillsBukkit;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.StatModifier;
import dev.aurelium.auraskills.api.stat.Stats;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnhancedSocketableItem extends IdentifiedItem {
    private static final String GEM_BOOST_PREFIX = "gem_boost_";
    private static final String BASE_STAT_VALUE_PREFIX = "base_stat_";

    public EnhancedSocketableItem(ItemStack item) {
        super(item);
    }

    protected void addGemStat(Stat stat, int gemValue) {
        ItemMeta meta = getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        // Guardar el valor de la gema para esta stat
        container.set(
                new NamespacedKey(plugin, GEM_BOOST_PREFIX + stat.name()),
                PersistentDataType.INTEGER,
                gemValue
        );

        // Obtener el modificador actual si existe
        List<StatModifier> currentModifiers = AuraSkillsBukkit.get().getItemManager()
                .getStatModifiers(this, MODIFIER_TYPE);

        StatModifier existingMod = currentModifiers.stream()
                .filter(mod -> mod.type().equals(stat))
                .findFirst()
                .orElse(null);

        if (existingMod != null) {
            // Guardar el valor base original
            container.set(
                    new NamespacedKey(plugin, BASE_STAT_VALUE_PREFIX + stat.name()),
                    PersistentDataType.INTEGER,
                    (int)existingMod.value()
            );

            // Remover el modificador existente
            removeSpecificModifier(stat);

            // Agregar nuevo modificador combinado (sin generar lore)
            addModifier(stat, (int)existingMod.value() + gemValue, false);

            // Agregar línea de lore manual mostrando el desglose
            addStatBreakdownToLore(stat, (int)existingMod.value(), gemValue);
        } else {
            // Si no existe modificador previo, solo agregar el de la gema
            addModifier(stat, gemValue, true);
        }

        setItemMeta(meta);
    }

    private void addStatBreakdownToLore(Stat stat, int baseValue, int gemBoost) {
        ItemMeta meta = getItemMeta();
        List<Component> lore = meta.hasLore() ? meta.lore() : new ArrayList<>();

        // Crear el componente de desglose
        Component breakdown = Component.text("+" + baseValue)
                .color(TextColor.fromHexString(stat.getColor(AuraSkillsApi.get().getMessageManager().getDefaultLanguage())))
                .append(Component.text(" + " + gemBoost + " "))
                .append(Component.text(stat.getDisplayName(AuraSkillsApi.get().getMessageManager().getDefaultLanguage())))
                .decoration(TextDecoration.ITALIC, false);

        // Insertar al inicio de las stats
        lore.add(0, breakdown);
        meta.lore(lore);
        setItemMeta(meta);
    }

    @Override
    protected void generateStats() {

    }

    protected void rerollStatsEnhanced() {
        // Guardar los boosts de gemas antes de reroll
        Map<Stat, Integer> gemBoosts = new HashMap<>();
        PersistentDataContainer container = getItemMeta().getPersistentDataContainer();

        // Recolectar todos los boosts de gemas
        for (NamespacedKey key : container.getKeys()) {
            if (key.getKey().startsWith(GEM_BOOST_PREFIX)) {
                String statName = key.getKey().substring(GEM_BOOST_PREFIX.length());
                Stat stat = Stats.valueOf(statName);
                int boost = container.getOrDefault(key, PersistentDataType.INTEGER, 0);
                gemBoosts.put(stat, boost);
            }
        }

        // Realizar el reroll normal
        super.rerollStats();

        // Reaplicar los boosts de gemas
        for (Map.Entry<Stat, Integer> entry : gemBoosts.entrySet()) {
            Stat stat = entry.getKey();
            int gemBoost = entry.getValue();

            StatModifier newMod = AuraSkillsBukkit.get().getItemManager()
                    .getStatModifiers(this, MODIFIER_TYPE)
                    .stream()
                    .filter(mod -> mod.type().equals(stat))
                    .findFirst()
                    .orElse(null);

            if (newMod != null) {
                // Si la stat existe en el nuevo roll
                removeSpecificModifier(stat);
                addModifier(stat, (int)newMod.value() + gemBoost, false);
                addStatBreakdownToLore(stat, (int)newMod.value(), gemBoost);
            } else {
                // Si la stat no existe en el nuevo roll, agregar solo el boost de la gema
                addModifier(stat, gemBoost, true);
            }
        }
    }

    @Override
    public void updateLoreWithSockets() {

    }

    // Método helper para agregar modificador con control sobre generación de lore
    private void addModifier(Stat stat, int value, boolean generateLore) {
        this.setItemMeta(AuraSkillsBukkit.get().getItemManager()
                .addStatModifier(this, MODIFIER_TYPE, stat, value, generateLore)
                .getItemMeta());
    }
}