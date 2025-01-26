package cl.nightcore.itemrarity.statprovider;

import cl.nightcore.itemrarity.config.CombinedStats;
import cl.nightcore.itemrarity.customstats.CustomStats;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.Stats;

import java.util.Arrays;
import java.util.List;

public class WeaponStatProvider implements StatProvider {


    private static final List<Stat> ITEM_STATS = Arrays.asList(
            CombinedStats.CRIT_CHANCE, CombinedStats.CRIT_DAMAGE,  CombinedStats.DEXTERITY,
            CombinedStats.STRENGTH, CombinedStats.REGENERATION, CombinedStats.TOUGHNESS, CombinedStats.ACCURACY
    );
    private static final List<Stat> GAUSS_ITEM_STATS = Arrays.asList(CombinedStats.STRENGTH, CombinedStats.CRIT_CHANCE);

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