package cl.nightcore.itemrarity.statprovider;

import dev.aurelium.auraskills.api.stat.Stat;

import java.util.List;

public interface StatProvider {
    List<Stat> getAvailableStats();

    List<Stat> getGaussStats();

    boolean isThisStatGauss(Stat stat);
}