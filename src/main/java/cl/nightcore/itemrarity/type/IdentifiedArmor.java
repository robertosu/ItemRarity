package cl.nightcore.itemrarity.type;

import cl.nightcore.itemrarity.statprovider.ArmorStatProvider;
import cl.nightcore.itemrarity.abstracted.IdentifiedItem;
import cl.nightcore.itemrarity.abstracted.StatProvider;
import cl.nightcore.itemrarity.classes.StatValueGenerator;
import dev.aurelium.auraskills.api.stat.Stats;
import dev.aurelium.auraskills.api.stat.Stat;

import org.bukkit.inventory.ItemStack;

import java.util.*;

public class IdentifiedArmor extends IdentifiedItem {
    public IdentifiedArmor(ItemStack item) {
        super(item);
        rollQuality = getRollQuality();
    }

    @Override
    protected void generateStats() {
        Random random = new Random();
        int statsCount = random.nextInt(2) + 4; // 4 o 5 estadísticas
        StatProvider statProvider = new ArmorStatProvider();
        List<Stats> availableStats = statProvider.getAvailableStats();
        //Adds gauss stats to the item
        for(Stat stat :GaussStats){
            getAddedStats().add(stat);
            int value = StatValueGenerator.generateValueForStat(getRollQuality(),isThisStatGauss(stat));
            getStatValues().add(value);
        }
        //Adds normal stats to the item
        for (int i = 0; i < statsCount - 1; i++) {
            Stat stat;
            do {
                stat = availableStats.get(random.nextInt(availableStats.size()));
            } while (getAddedStats().contains(stat));
            getAddedStats().add(stat);
            int value = StatValueGenerator.generateValueForStat(getRollQuality(),isThisStatGauss(stat));
            getStatValues().add(value);
        }

    }
}