package cl.nightcore.itemrarity.type;

import cl.nightcore.itemrarity.abstracted.IdentifiedItem;
import cl.nightcore.itemrarity.abstracted.SocketableItem;
import cl.nightcore.itemrarity.abstracted.StatProvider;
import cl.nightcore.itemrarity.classes.StatValueGenerator;
import cl.nightcore.itemrarity.statprovider.WeaponStatProvider;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.Stats;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

public class IdentifiedWeapon extends SocketableItem {
    public IdentifiedWeapon(ItemStack item) {
        super(item);
        statProvider = new WeaponStatProvider();
        rollQuality = getRollQuality();
    }

    @Override
    protected void generateStats() {
        Random random = new Random();
        int statsCount = random.nextInt(2) + 4; // 4 o 5 estadísticas
        StatProvider statProvider = new WeaponStatProvider();
        List<Stats> availableStats = statProvider.getAvailableStats();
        for (Stat stat : statProvider.getGaussStats()) {
            getAddedStats().add(stat);
            int value = StatValueGenerator.generateValueForStat(getRollQuality(), statProvider.isThisStatGauss(stat));
            getStatValues().add(value);
        }
        for (int i = 0; i < statsCount - 1; i++) {
            Stats stat;
            do {
                stat = availableStats.get(random.nextInt(availableStats.size()));
            } while (getAddedStats().contains(stat));
            getAddedStats().add(stat);
            int value = StatValueGenerator.generateValueForStat(getRollQuality(), statProvider.isThisStatGauss(stat));
            getStatValues().add(value);
        }

    }
}