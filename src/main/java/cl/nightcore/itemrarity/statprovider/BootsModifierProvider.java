package cl.nightcore.itemrarity.statprovider;

import cl.nightcore.itemrarity.config.CombinedStats;
import cl.nightcore.itemrarity.config.CombinedTraits;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.trait.Trait;

import java.util.Arrays;
import java.util.List;

public class BootsModifierProvider implements ModifierProvider {

    private static final List<Stat> BOOTS_STATS = Arrays.asList(
            CombinedStats.CRIT_CHANCE,
            CombinedStats.CRIT_DAMAGE,
            CombinedStats.HEALTH,
            CombinedStats.LUCK,
            // CombinedStats.REGENERATION,
            CombinedStats.DEXTERITY,
            CombinedStats.STRENGTH,
            CombinedStats.TOUGHNESS,
            CombinedStats.WISDOM,
            CombinedStats.EVASION,
            CombinedStats.ACCURACY);
    private static final List<Stat> GAUSS_BOOTS_STATS =
            Arrays.asList(CombinedStats.CRIT_DAMAGE, CombinedStats.CRIT_CHANCE);

    private static final List<Trait> MONOLITIC_TRAITS =
            Arrays.asList(CombinedTraits.DAMAGE_REDUCTION, CombinedTraits.HP);

    @Override
    public List<Trait> getMonoliticTraits() {
        return MONOLITIC_TRAITS;
    }

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