package cl.nightcore.itemrarity.util;

import cl.nightcore.itemrarity.config.ItemConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;

import java.util.List;

public class RarityCalculator {
    // Componentes pre-instanciados
    private static final Component GODLIKE_COMPONENT = Component.text("[Divino]")
            .color(ItemConfig.GODLIKE_COLOR)
            .decoration(TextDecoration.ITALIC, false);

    private static final Component LEGENDARY_COMPONENT = Component.text("[Legendario]")
            .color(ItemConfig.LEGENDARY_COLOR)
            .decoration(TextDecoration.ITALIC, false);

    private static final Component EPIC_COMPONENT = Component.text("[Épico]")
            .color(ItemConfig.EPIC_COLOR)
            .decoration(TextDecoration.ITALIC, false);

    private static final Component RARE_COMPONENT = Component.text("[Raro]")
            .color(ItemConfig.RARE_COLOR)
            .decoration(TextDecoration.ITALIC, false);

    private static final Component UNCOMMON_COMPONENT = Component.text("[Poco común]")
            .color(ItemConfig.UNCOMMON_COLOR)
            .decoration(TextDecoration.ITALIC, false);

    private static final Component COMMON_COMPONENT = Component.text("[Común]")
            .color(ItemConfig.COMMON_COLOR)
            .decoration(TextDecoration.ITALIC, false);

    // Thresholds constantes - se evalúan una sola vez al cargar la clase
    private static final List<RarityThreshold> RARITY_THRESHOLDS = List.of(
            // Los valores se ajustaron basados en los percentiles observados para lograr:
            // Godlike: 0.5% (P99.5)  -> ~21.40
            // Legendario: 1.5% (P98.0) -> ~20.20
            // Épico: 6% (P92.0) -> ~18.40
            // Raro: 18% (P74.0) -> ~16.40
            // Común: 32% (arriba de P42.0) -> ~14.20
            // Basura: 42% (resto)
            new RarityThreshold(21.40, GODLIKE_COMPONENT),
            new RarityThreshold(20.20, LEGENDARY_COMPONENT),
            new RarityThreshold(18.40, EPIC_COMPONENT),
            new RarityThreshold(16.40, RARE_COMPONENT),
            new RarityThreshold(14.20, UNCOMMON_COMPONENT),
            new RarityThreshold(Double.NEGATIVE_INFINITY, COMMON_COMPONENT)
    );

    static {
        // Log de inicialización (opcional)
        System.out.println("Umbrales de rareza inicializados:");
        RARITY_THRESHOLDS.forEach(threshold ->
                System.out.println(threshold.component.toString() + ": " + threshold.minAverage));
    }
    private static final NamespacedKey TOOLTIP_COMMON = new NamespacedKey("minecraft","common");
    private static final NamespacedKey TOOLTIP_UNCOMMON = new NamespacedKey("minecraft","uncommon");
    private static final NamespacedKey TOOLTIP_RARE = new NamespacedKey("minecraft","rare");
    private static final NamespacedKey TOOLTIP_EPIC = new NamespacedKey("minecraft","epic");
    private static final NamespacedKey TOOLTIP_LEGENDARY = new NamespacedKey("minecraft","legendary");
    private static final NamespacedKey TOOLTIP_GODLIKE = new NamespacedKey("minecraft","godlike");



    public static NamespacedKey getTooltip(Component rarity){
        if (rarity == RARE_COMPONENT){
            return TOOLTIP_RARE;
        }else if(rarity == COMMON_COMPONENT){
            return TOOLTIP_COMMON;
        }else if(rarity == UNCOMMON_COMPONENT){
            return TOOLTIP_UNCOMMON;
        }else if(rarity == EPIC_COMPONENT){
            return TOOLTIP_EPIC;
        }else if(rarity == LEGENDARY_COMPONENT){
            return TOOLTIP_LEGENDARY;
        }else if(rarity == GODLIKE_COMPONENT){
            return TOOLTIP_GODLIKE;
        }
        throw new IllegalStateException("Objeto no tiene rareza?");
    }

    public static Component calculateRarity(double average) {
        for (RarityThreshold threshold : RARITY_THRESHOLDS) {
            if (average >= threshold.minAverage) {
                return threshold.component;
            }
        }

        throw new IllegalStateException("No se encontró rareza para el valor: " + average);
    }

    private record RarityThreshold(double minAverage, Component component) {}
}