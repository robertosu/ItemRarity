package cl.nightcore.itemrarity.statprovider;

import cl.nightcore.itemrarity.config.CombinedStats;
import cl.nightcore.itemrarity.customstats.CustomStats;
import dev.aurelium.auraskills.api.stat.Stat;

import java.util.Arrays;
import java.util.List;

public class HelmetStatProvider implements StatProvider {
    private static final List<Stat> HELMET_STATS = Arrays.asList(
            CombinedStats.CRIT_CHANCE, CombinedStats.CRIT_DAMAGE, CombinedStats.HEALTH, CombinedStats.LUCK,
            CombinedStats.REGENERATION,  CustomStats.DEXTERITY, CombinedStats.STRENGTH, CombinedStats.TOUGHNESS,
            CombinedStats.WISDOM, CombinedStats.EVASION
    );
    private static final List<Stat> GAUSS_HELMET_STATS = Arrays.asList(CombinedStats.HEALTH, CombinedStats.CRIT_CHANCE);

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