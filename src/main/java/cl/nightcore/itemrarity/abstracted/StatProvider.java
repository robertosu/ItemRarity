package cl.nightcore.itemrarity.abstracted;

import dev.aurelium.auraskills.api.stat.Stats;

import java.util.List;

public interface StatProvider {
    List<Stats> getAvailableStats();
}