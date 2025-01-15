package cl.nightcore.itemrarity.statprovider;

import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.Stats;

import java.util.Arrays;
import java.util.List;

public class WeaponStatProvider implements StatProvider {


    private static final List<Stat> ITEM_STATS = Arrays.asList(
            Stats.CRIT_CHANCE, Stats.CRIT_DAMAGE, Stats.SPEED, Stats.STRENGTH, Stats.REGENERATION, Stats.TOUGHNESS
    );
    private static final List<Stat> GAUSS_ITEM_STATS = Arrays.asList(Stats.STRENGTH, Stats.CRIT_CHANCE);

    @Override
    public List<Stat> getAvailableStats() {
        return ITEM_STATS;
    }

    @Override
    public List<Stat> getGaussStats() {
        return GAUSS_ITEM_STATS;
    }

    @Override
    public boolean isThisStatGauss(Stat stat) {
        return getGaussStats().contains(stat);
    }


}