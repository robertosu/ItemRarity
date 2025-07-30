package cl.nightcore.itemrarity.rollquality;

import org.apache.commons.math3.distribution.NormalDistribution;

import static cl.nightcore.itemrarity.util.ItemUtil.RANDOM;

public class StatValueGenerator {

    private static final int[] POSSIBLE_VALUES = {5, 8, 11, 15, 20};

    private static final RollQuality rollQuality = MainRollQuality.getInstance();

    public static int generateValueForStat(boolean useNormalDistribution) {
        if (useNormalDistribution) {
            return generateValueUsingNormalDistribution();
        } else {
            return generateValueUsingRandomDistribution();
        }
    }

    private static int generateValueUsingNormalDistribution() {
        NormalDistribution distribution = new NormalDistribution(rollQuality.getMean(), rollQuality.getSd());
        int value = (int) Math.round(distribution.sample());
        value = Math.max(value, 10);
        return value;
    }

    private static int generateValueUsingRandomDistribution() {
        // Generar un índice aleatorio entre 0 y el máximo permitido
        int randomIndex = RANDOM.nextInt(POSSIBLE_VALUES.length);
        // Retornar el valor correspondiente del array de valores posibles
        return POSSIBLE_VALUES[randomIndex];
    }
}