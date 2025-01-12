package cl.nightcore.itemrarity.classes;

import cl.nightcore.itemrarity.abstracted.RollQuality;
import org.apache.commons.math3.distribution.NormalDistribution;

import static cl.nightcore.itemrarity.util.ItemUtil.random;

public class StatValueGenerator {
    public static int generateValueForStat(RollQuality rollQuality, boolean useNormalDistribution) {
        if (useNormalDistribution) {
            return generateValueUsingNormalDistribution(rollQuality);
        } else {
            return generateValueUsingRandomDistribution(rollQuality);
        }
    }

    private static int generateValueUsingNormalDistribution(RollQuality rollQuality) {
        NormalDistribution distribution = new NormalDistribution(rollQuality.getMean(), rollQuality.getSd());
        int value = (int) Math.round(distribution.sample());
        value = Math.max(value, 6); // Asegurar que el valor sea al menos 6
        value = Math.min(value, 50); // Asegurar que el valor no exceda 50
        return value;
    }

    private static int generateValueUsingRandomDistribution(RollQuality rollQuality) {
        int minValue = rollQuality.getMinStatValue();
        int bound = rollQuality.getBound();
        return minValue + random.nextInt(bound);
    }
}