package cl.nightcore.itemrarity.statprovider;

import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.Stats;

import java.util.Arrays;
import java.util.List;

public class HelmetStatProvider implements StatProvider {
    private static final List<Stat> HELMET_STATS = Arrays.asList(
            Stats.CRIT_CHANCE, Stats.CRIT_DAMAGE, Stats.HEALTH, Stats.LUCK,
            Stats.REGENERATION, Stats.SPEED, Stats.STRENGTH, Stats.TOUGHNESS,
            Stats.WISDOM
    );
    private static final List<Stat> GAUSS_HELMET_STATS = Arrays.asList(Stats.HEALTH, Stats.CRIT_CHANCE);

    @Override
    public List<Stat> getAvailableStats() {
        return HELMET_STATS;
    }

    @Override
    public List<Stat> getGaussStats() {
        return GAUSS_HELMET_STATS;
    }

    @Override
    public boolean isThisStatGauss(Stat stat) {
        return getGaussStats().contains(stat);
    }
}