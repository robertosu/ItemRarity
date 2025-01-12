package cl.nightcore.itemrarity.type;

import cl.nightcore.itemrarity.abstracted.SocketableItem;
import cl.nightcore.itemrarity.classes.StatValueGenerator;
import cl.nightcore.itemrarity.statprovider.ArmorStatProvider;
import cl.nightcore.itemrarity.statprovider.StatProvider;
import cl.nightcore.itemrarity.util.ItemUtil;
import dev.aurelium.auraskills.api.stat.Stat;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class IdentifiedArmor extends SocketableItem {
    public IdentifiedArmor(ItemStack item) {
        super(item);
        statProvider = new ArmorStatProvider();
        rollQuality = getRollQuality();
    }


    @Override
    protected void generateStats() {
        int statsCount = ItemUtil.random.nextInt(2) + 4; // 4 o 5 estadísticas
        StatProvider statProvider = new ArmorStatProvider();

        List<Stat> availableStats = statProvider.getAvailableStats();
        //Adds gauss distributed stats to the item
        for (Stat stat : statProvider.getGaussStats()) {
            getAddedStats().add(stat);
            int value = StatValueGenerator.generateValueForStat(getRollQuality(), statProvider.isThisStatGauss(stat));
            getStatValues().add(value);
        }
        //Adds normal stats to the item
        for (int i = 0; i < statsCount - 1; i++) {
            Stat stat;
            do {
                stat = availableStats.get(ItemUtil.random.nextInt(availableStats.size()));
            } while (getAddedStats().contains(stat));
            getAddedStats().add(stat);
            int value = StatValueGenerator.generateValueForStat(getRollQuality(), statProvider.isThisStatGauss(stat));
            getStatValues().add(value);
        }

    }

}