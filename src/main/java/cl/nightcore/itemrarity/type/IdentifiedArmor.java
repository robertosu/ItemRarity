package cl.nightcore.itemrarity.type;

import cl.nightcore.itemrarity.statprovider.ArmorStatProvider;
import cl.nightcore.itemrarity.abstracted.IdentifiedItem;
import cl.nightcore.itemrarity.abstracted.StatProvider;
import cl.nightcore.itemrarity.classes.StatValueGenerator;
import cl.nightcore.itemrarity.statprovider.WeaponStatProvider;
import dev.aurelium.auraskills.api.stat.Stats;
import dev.aurelium.auraskills.api.stat.Stat;

import org.bukkit.inventory.ItemStack;

import java.util.*;

public class IdentifiedArmor extends IdentifiedItem {
    public IdentifiedArmor(ItemStack item) {
        super(item);
        statProvider = new ArmorStatProvider();
        rollQuality = getRollQuality();
    }

    @Override
    protected void generateStats() {
        Random random = new Random();
        int statsCount = random.nextInt(2) + 4; // 4 o 5 estadísticas
        StatProvider statProvider = new ArmorStatProvider();

        List<Stats> availableStats = statProvider.getAvailableStats();
        //Adds gauss distributed stats to the item
        for(Stat stat :statProvider.getGaussStats()){
            getAddedStats().add(stat);
            int value = StatValueGenerator.generateValueForStat(getRollQuality(),statProvider.isThisStatGauss(stat));
            getStatValues().add(value);
        }
        //Adds normal stats to the item
        for (int i = 0; i < statsCount - 1; i++) {
            Stats stat;
            do {
                stat = availableStats.get(random.nextInt(availableStats.size()));
            } while (getAddedStats().contains(stat));
            getAddedStats().add(stat);
            int value = StatValueGenerator.generateValueForStat(getRollQuality(),statProvider.isThisStatGauss(stat));
            getStatValues().add(value);
        }

    }
}