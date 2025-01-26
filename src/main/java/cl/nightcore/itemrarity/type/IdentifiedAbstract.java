package cl.nightcore.itemrarity.type;

import cl.nightcore.itemrarity.abstracted.SocketableItem;
import cl.nightcore.itemrarity.classes.StatValueGenerator;
import dev.aurelium.auraskills.api.stat.Stat;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class IdentifiedAbstract extends SocketableItem {
    public IdentifiedAbstract(ItemStack item) {
        super(item);
    }

    @Override
    protected void generateStats() {
        int statsCount = getMaxBonuses(); // Número total de stats a generar
        List<Stat> availableStats = statProvider.getAvailableStats();

        // 1. Agregar stats gaussianas
        for (Stat stat : statProvider.getGaussStats()) {
            getAddedStats().add(stat);
            int value = StatValueGenerator.generateValueForStat(getRollQuality(), statProvider.isThisStatGauss(stat));
            getStatValues().add(value);
        }

        // 2. Calcular cuántas stats adicionales se necesitan
        int remainingStats = statsCount - getAddedStats().size();

        // 3. Agregar stats aleatorias hasta alcanzar el límite
        for (int i = 0; i < remainingStats; i++) {
            Stat stat;
            do {
                stat = availableStats.get(ThreadLocalRandom.current().nextInt(availableStats.size()));
            } while (getAddedStats().contains(stat)); // Evitar duplicados

            getAddedStats().add(stat);
            int value = StatValueGenerator.generateValueForStat(getRollQuality(), statProvider.isThisStatGauss(stat));
            getStatValues().add(value);
        }
    }
}