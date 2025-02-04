package cl.nightcore.itemrarity.util;

import cl.nightcore.itemrarity.abstracted.RollQuality;
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
        // Determinamos los umbrales basados en el RollQuality específico
        List<RarityThreshold> thresholds = getThresholdsForQuality(rollQuality);

        // Debug information
        System.out.println("Umbrales de rareza para " + rollQuality.getClass().getSimpleName() + ":");
        thresholds.forEach(threshold -> System.out.println(threshold.rarityText + ": " + threshold.minAverage));

        RARITY_THRESHOLDS_CACHE.put(rollQuality, thresholds);
    }

    private static List<RarityThreshold> getThresholdsForQuality(RollQuality rollQuality) {
        return switch (rollQuality.getClass().getSimpleName()) {
            case "_9RollQuality" -> List.of(
                    new RarityThreshold(23.80, ItemConfig.GODLIKE_COLOR, "Divino"),
                    new RarityThreshold(22.40, ItemConfig.LEGENDARY_COLOR, "Legendario"),
                    new RarityThreshold(21.00, ItemConfig.EPIC_COLOR, "Épico"),
                    new RarityThreshold(19.20, ItemConfig.RARE_COLOR, "Raro"),
                    new RarityThreshold(17.40, ItemConfig.UNCOMMON_COLOR, "Común"),
                    new RarityThreshold(Double.NEGATIVE_INFINITY, ItemConfig.COMMON_COLOR, "Basura")
            );
            case "_8RollQuality" -> List.of(
                    new RarityThreshold(22.40, ItemConfig.GODLIKE_COLOR, "Divino"),
                    new RarityThreshold(21.20, ItemConfig.LEGENDARY_COLOR, "Legendario"),
                    new RarityThreshold(19.80, ItemConfig.EPIC_COLOR, "Épico"),
                    new RarityThreshold(18.20, ItemConfig.RARE_COLOR, "Raro"),
                    new RarityThreshold(16.60, ItemConfig.UNCOMMON_COLOR, "Común"),
                    new RarityThreshold(Double.NEGATIVE_INFINITY, ItemConfig.COMMON_COLOR, "Basura")
            );
            case "_7RollQuality" -> List.of(
                    new RarityThreshold(21.40, ItemConfig.GODLIKE_COLOR, "Divino"),
                    new RarityThreshold(20.40, ItemConfig.LEGENDARY_COLOR, "Legendario"),
                    new RarityThreshold(19.00, ItemConfig.EPIC_COLOR, "Épico"),
                    new RarityThreshold(17.40, ItemConfig.RARE_COLOR, "Raro"),
                    new RarityThreshold(15.80, ItemConfig.UNCOMMON_COLOR, "Común"),
                    new RarityThreshold(Double.NEGATIVE_INFINITY, ItemConfig.COMMON_COLOR, "Basura")
            );
            case "_6RollQuality" -> List.of(
                    new RarityThreshold(20.20, ItemConfig.GODLIKE_COLOR, "Divino"),
                    new RarityThreshold(19.00, ItemConfig.LEGENDARY_COLOR, "Legendario"),
                    new RarityThreshold(17.80, ItemConfig.EPIC_COLOR, "Épico"),
                    new RarityThreshold(16.20, ItemConfig.RARE_COLOR, "Raro"),
                    new RarityThreshold(14.60, ItemConfig.UNCOMMON_COLOR, "Común"),
                    new RarityThreshold(Double.NEGATIVE_INFINITY, ItemConfig.COMMON_COLOR, "Basura")
            );
            case "_5RollQuality" -> List.of(
                    new RarityThreshold(19.00, ItemConfig.GODLIKE_COLOR, "Divino"),
                    new RarityThreshold(17.80, ItemConfig.LEGENDARY_COLOR, "Legendario"),
                    new RarityThreshold(16.60, ItemConfig.EPIC_COLOR, "Épico"),
                    new RarityThreshold(15.00, ItemConfig.RARE_COLOR, "Raro"),
                    new RarityThreshold(13.60, ItemConfig.UNCOMMON_COLOR, "Común"),
                    new RarityThreshold(Double.NEGATIVE_INFINITY, ItemConfig.COMMON_COLOR, "Basura")
            );
            case "_4RollQuality" -> List.of(
                    new RarityThreshold(18.00, ItemConfig.GODLIKE_COLOR, "Divino"),
                    new RarityThreshold(17.00, ItemConfig.LEGENDARY_COLOR, "Legendario"),
                    new RarityThreshold(15.80, ItemConfig.EPIC_COLOR, "Épico"),
                    new RarityThreshold(14.20, ItemConfig.RARE_COLOR, "Raro"),
                    new RarityThreshold(12.80, ItemConfig.UNCOMMON_COLOR, "Común"),
                    new RarityThreshold(Double.NEGATIVE_INFINITY, ItemConfig.COMMON_COLOR, "Basura")
            );
            case "_3RollQuality" -> List.of(
                    new RarityThreshold(16.80, ItemConfig.GODLIKE_COLOR, "Divino"),
                    new RarityThreshold(15.80, ItemConfig.LEGENDARY_COLOR, "Legendario"),
                    new RarityThreshold(14.60, ItemConfig.EPIC_COLOR, "Épico"),
                    new RarityThreshold(13.20, ItemConfig.RARE_COLOR, "Raro"),
                    new RarityThreshold(11.60, ItemConfig.UNCOMMON_COLOR, "Común"),
                    new RarityThreshold(Double.NEGATIVE_INFINITY, ItemConfig.COMMON_COLOR, "Basura")
            );
            case "_2RollQuality" -> List.of(
                    new RarityThreshold(15.60, ItemConfig.GODLIKE_COLOR, "Divino"),
                    new RarityThreshold(14.60, ItemConfig.LEGENDARY_COLOR, "Legendario"),
                    new RarityThreshold(13.40, ItemConfig.EPIC_COLOR, "Épico"),
                    new RarityThreshold(12.00, ItemConfig.RARE_COLOR, "Raro"),
                    new RarityThreshold(10.60, ItemConfig.UNCOMMON_COLOR, "Común"),
                    new RarityThreshold(Double.NEGATIVE_INFINITY, ItemConfig.COMMON_COLOR, "Basura")
            );
            case "_1RollQuality" -> List.of(
                    new RarityThreshold(14.80, ItemConfig.GODLIKE_COLOR, "Divino"),
                    new RarityThreshold(13.80, ItemConfig.LEGENDARY_COLOR, "Legendario"),
                    new RarityThreshold(12.60, ItemConfig.EPIC_COLOR, "Épico"),
                    new RarityThreshold(11.20, ItemConfig.RARE_COLOR, "Raro"),
                    new RarityThreshold(9.80, ItemConfig.UNCOMMON_COLOR, "Común"),
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
        //System.out.println("Evaluando average: " + average);

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