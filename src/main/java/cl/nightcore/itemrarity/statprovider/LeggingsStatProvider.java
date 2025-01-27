package cl.nightcore.itemrarity.statprovider;

import cl.nightcore.itemrarity.config.CombinedStats;
import dev.aurelium.auraskills.api.stat.Stat;

import java.util.Arrays;
import java.util.List;

public class LeggingsStatProvider implements StatProvider {
    private static final List<Stat> LEGGINGS_STATS = Arrays.asList(
            CombinedStats.CRIT_CHANCE, CombinedStats.CRIT_DAMAGE, CombinedStats.HEALTH, CombinedStats.LUCK,
            CombinedStats.REGENERATION, CombinedStats.STRENGTH, CombinedStats.TOUGHNESS, CombinedStats.DEXTERITY,
            CombinedStats.WISDOM, CombinedStats.ACCURACY
    );
    private static final List<Stat> GAUSS_LEGGINGS_STATS = Arrays.asList(CombinedStats.CRIT_DAMAGE, CombinedStats.TOUGHNESS);

    @Override
    public List<Stat> getAvailableStats() {
        return LEGGINGS_STATS;
    }

    @Override
    public List<Stat> getGaussStats() {
        return GAUSS_LEGGINGS_STATS;
    }

    @Override
    public boolean isThisStatGauss(Stat stat) {
        return getGaussStats().contains(stat);
    }
}