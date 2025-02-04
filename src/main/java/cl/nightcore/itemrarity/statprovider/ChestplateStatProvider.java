package cl.nightcore.itemrarity.statprovider;

import cl.nightcore.itemrarity.config.CombinedStats;
import dev.aurelium.auraskills.api.stat.Stat;

import java.util.Arrays;
import java.util.List;

public class ChestplateStatProvider implements StatProvider {
    private static final List<Stat> CHESTPLATE_STATS = Arrays.asList(
            CombinedStats.CRIT_CHANCE, CombinedStats.CRIT_DAMAGE, CombinedStats.HEALTH, CombinedStats.LUCK,
            CombinedStats.REGENERATION,  CombinedStats.DEXTERITY, CombinedStats.STRENGTH, CombinedStats.TOUGHNESS,
            CombinedStats.WISDOM, CombinedStats.EVASION
    );
    private static final List<Stat> GAUSS_CHESTPLATE_STATS = Arrays.asList(CombinedStats.HEALTH, CombinedStats.TOUGHNESS);

    @Override
    public List<Stat> getAvailableStats() {
        return CHESTPLATE_STATS;
    }

    @Override
    public List<Stat> getGaussStats() {
        return GAUSS_CHESTPLATE_STATS;
    }

    @Override
    public boolean isThisStatGauss(Stat stat) {
        return getGaussStats().contains(stat);
    }
}