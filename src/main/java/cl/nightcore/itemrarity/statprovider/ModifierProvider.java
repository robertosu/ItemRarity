package cl.nightcore.itemrarity.statprovider;

import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.trait.Trait;

import java.util.List;

public interface ModifierProvider {

    List<Trait> getMonoliticTraits();

    List<Stat> getAvailableStats();

    List<Stat> getGaussStats();

    boolean isThisStatGauss(Stat stat);
}