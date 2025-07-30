package cl.nightcore.itemrarity.test;

import cl.nightcore.itemrarity.rollquality.RollQuality;
import cl.nightcore.itemrarity.rollquality.StatValueGenerator;
import cl.nightcore.itemrarity.util.RarityCalculator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatDistributionTester {

    private final int simulationCount;
    private final RollQuality rollQuality;

    public StatDistributionTester(RollQuality rollQuality,int simulationcount){
        this.simulationCount =simulationcount;
        this.rollQuality=rollQuality;
    }

   // Número de items a generar para cada prueba

    public void runAnalysis() {

        // Recopilar todas las stats generadas
        List<Double> averages = new ArrayList<>();
        Map<String, Integer> rarityCount = new HashMap<>();
        
        for (int i = 0; i < simulationCount; i++) {
            // Generar 2 stats con distribución normal
            List<Integer> stats = new ArrayList<>();
            stats.add(StatValueGenerator.generateValueForStat(true));
            stats.add(StatValueGenerator.generateValueForStat(true));
            
            // Generar 3 stats con distribución uniforme
            stats.add(StatValueGenerator.generateValueForStat(false));
            stats.add(StatValueGenerator.generateValueForStat(false));
            stats.add(StatValueGenerator.generateValueForStat(false));
            
            // Calcular promedio
            double average = stats.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            averages.add(average);
            
            // Calcular rareza y contarla
            Component rarity = RarityCalculator.calculateRarity(rollQuality, average);

            String rarityName = PlainTextComponentSerializer.plainText().serialize(rarity);

            rarityCount.merge(rarityName, 1, Integer::sum);
        }
        
        // Análisis estadístico
        DescriptiveStatistics stats = new DescriptiveStatistics();
        averages.forEach(stats::addValue);

        System.out.println("\n=== Análisis para " + rollQuality.getClass().getSimpleName() + " ===");
        System.out.println("Configuración:");
        System.out.println("Mean: " + rollQuality.getMean());
        System.out.println("SD: " + rollQuality.getSd());
        
        System.out.println("\nEstadísticas de los promedios:");
        System.out.printf("Min: %.2f\n", stats.getMin());
        System.out.printf("Max: %.2f\n", stats.getMax());
        System.out.printf("Media: %.2f\n", stats.getMean());
        System.out.printf("Mediana: %.2f\n", stats.getPercentile(50));
        System.out.printf("Desviación Estándar: %.2f\n", stats.getStandardDeviation());
        
        System.out.println("\nPercentiles clave:");
        System.out.printf("P99.5 (Godlike): %.2f\n", stats.getPercentile(99.5));
        System.out.printf("P98.0 (Legendario): %.2f\n", stats.getPercentile(98));
        System.out.printf("P92.0 (Épico): %.2f\n", stats.getPercentile(92));
        System.out.printf("P74.0 (Raro): %.2f\n", stats.getPercentile(74));
        System.out.printf("P42.0 (Común): %.2f\n", stats.getPercentile(42));
        
        System.out.println("\nDistribución de rarezas actual:");
        rarityCount.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(entry -> {
                double percentage = (entry.getValue() * 100.0) / simulationCount;
                System.out.printf("%s: %d (%.2f%%)\n", 
                    entry.getKey(), entry.getValue(), percentage);
            });
    }
}