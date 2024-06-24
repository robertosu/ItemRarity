package cl.nightcore.itemrarity.statprovider;

import cl.nightcore.itemrarity.abstracted.StatProvider;
import dev.aurelium.auraskills.api.stat.Stats;

import java.util.Arrays;
import java.util.List;

public class ArmorStatProvider implements StatProvider {
    private static final List<Stats> ARMOR_STATS = Arrays.asList(
            Stats.CRIT_CHANCE, Stats.CRIT_DAMAGE, Stats.HEALTH, Stats.LUCK,
            Stats.REGENERATION, Stats.SPEED, Stats.STRENGTH, Stats.TOUGHNESS,
            Stats.WISDOM
    );

    @Override
    public List<Stats> getAvailableStats() {
        return ARMOR_STATS;
    }
}