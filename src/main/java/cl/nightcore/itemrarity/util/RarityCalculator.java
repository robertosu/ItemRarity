package cl.nightcore.itemrarity.util;

import cl.nightcore.itemrarity.rollquality.RollQuality;
import cl.nightcore.itemrarity.config.ItemConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RarityCalculator {
    private static final Map<RollQuality, List<RarityThreshold>> RARITY_THRESHOLDS_CACHE = new HashMap<>();

    private static void cacheRarityThresholds(RollQuality rollQuality) {
        List<RarityThreshold> thresholds = getThresholdsForQuality(rollQuality);

        System.out.println("Umbrales de rareza para " + rollQuality.getClass().getSimpleName() + ":");
        thresholds.forEach(threshold -> System.out.println(threshold.rarityText + ": " + threshold.minAverage));

        RARITY_THRESHOLDS_CACHE.put(rollQuality, thresholds);
    }

    private static List<RarityThreshold> getThresholdsForQuality(RollQuality rollQuality) {
        return switch (rollQuality.getClass().getSimpleName()) {
            case "MainRollQuality" -> List.of(
                    // Los valores se ajustaron basados en los percentiles observados para lograr:
                    // Godlike: 0.5% (P99.5)  -> ~21.40
                    // Legendario: 1.5% (P98.0) -> ~20.20
                    // Épico: 6% (P92.0) -> ~18.40
                    // Raro: 18% (P74.0) -> ~16.40
                    // Común: 32% (arriba de P42.0) -> ~14.20
                    // Basura: 42% (resto)
                    new RarityThreshold(21.40, ItemConfig.GODLIKE_COLOR, "Divino"),
                    new RarityThreshold(20.20, ItemConfig.LEGENDARY_COLOR, "Legendario"),
                    new RarityThreshold(18.40, ItemConfig.EPIC_COLOR, "Épico"),
                    new RarityThreshold(16.40, ItemConfig.RARE_COLOR, "Raro"),
                    new RarityThreshold(14.20, ItemConfig.UNCOMMON_COLOR, "Común"),
                    new RarityThreshold(Double.NEGATIVE_INFINITY, ItemConfig.COMMON_COLOR, "Basura")
            );
            default -> throw new IllegalArgumentException("RollQuality no reconocido: " + rollQuality.getClass().getSimpleName());
        };
    }

    public static Component calculateRarity(RollQuality rollQuality, double average) {
        if (!RARITY_THRESHOLDS_CACHE.containsKey(rollQuality)) {
            cacheRarityThresholds(rollQuality);
        }

        List<RarityThreshold> thresholds = RARITY_THRESHOLDS_CACHE.get(rollQuality);

        for (RarityThreshold threshold : thresholds) {
            if (average >= threshold.minAverage) {
                return Component.text(threshold.rarityText)
                        .color(threshold.color)
                        .decoration(TextDecoration.ITALIC, false);
            }
        }

        throw new IllegalStateException("No se encontró rareza para el valor: " + average);
    }

    private record RarityThreshold(double minAverage, TextColor color, String rarityText) {}
}