package cl.nightcore.itemrarity.statprovider;

import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.Stats;

import java.util.Arrays;
import java.util.List;

public class BootsStatProvider implements StatProvider {
    private static final List<Stat> BOOTS_STATS = Arrays.asList(
            Stats.CRIT_CHANCE, Stats.CRIT_DAMAGE, Stats.HEALTH, Stats.LUCK,
            Stats.REGENERATION, Stats.SPEED, Stats.STRENGTH, Stats.TOUGHNESS,
            Stats.WISDOM
    );
    private static final List<Stat> GAUSS_BOOTS_STATS = Arrays.asList(Stats.CRIT_DAMAGE, Stats.CRIT_CHANCE);

    @Override
    public List<Stat> getAvailableStats() {
        return BOOTS_STATS;
    }

    @Override
    public List<Stat> getGaussStats() {
        return GAUSS_BOOTS_STATS;
    }

    @Override
    public boolean isThisStatGauss(Stat stat) {
        return getGaussStats().contains(stat);
    }
}