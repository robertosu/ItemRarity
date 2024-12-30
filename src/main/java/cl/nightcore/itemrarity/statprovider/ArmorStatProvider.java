package cl.nightcore.itemrarity.statprovider;

import cl.nightcore.itemrarity.abstracted.StatProvider;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.Stats;

import java.util.Arrays;
import java.util.List;

public class ArmorStatProvider implements StatProvider {
    private static final List<Stats> ARMOR_STATS = Arrays.asList(
            Stats.CRIT_CHANCE, Stats.CRIT_DAMAGE, Stats.HEALTH, Stats.LUCK,
            Stats.REGENERATION, Stats.SPEED, Stats.STRENGTH, Stats.TOUGHNESS,
            Stats.WISDOM
    );
    private static final List<Stats> GAUSS_ARMOR_STATS = Arrays.asList(Stats.HEALTH, Stats.TOUGHNESS);

    @Override
    public List<Stats> getAvailableStats() {
        return ARMOR_STATS;
    }

    @Override
    public List<Stats> getGaussStats() {
        return GAUSS_ARMOR_STATS;
    }

    @Override
    public boolean isThisStatGauss(Stat stat) {
        return getGaussStats().contains(stat);
    }
}