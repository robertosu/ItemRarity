package cl.nightcore.itemrarity.hooks;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static cl.nightcore.itemrarity.ItemRarity.PLUGIN;

public class Holograms {

    private static final double xmin = -0.4;
    private static final double xmax = 0.4;

    private static final double ymin = 0.3;
    private static final double ymax = 0.6;

    private static final double zmin = -0.4;
    private static final double zmax = 0.4;


    public static Location getLocation(Entity entity) {
        Location location = entity.getLocation();
        double x = xmin + (xmax - xmin) * ThreadLocalRandom.current().nextDouble();
        double y = ymin + (ymax - ymin) * ThreadLocalRandom.current().nextDouble();
        double z = zmin + (zmax - zmin) * ThreadLocalRandom.current().nextDouble();
        location.add(x, (entity.getHeight() - entity.getHeight() * 0.1) + y, z);
        return location;
    }

    public static void createHologram(Location location, String text) {
        Hologram hologram = DHAPI.createHologram("IR_" + UUID.randomUUID(), location, false, Collections.singletonList(text));
        deleteHologram(hologram);
    }

    public static void deleteHologram(Hologram dh) {
        PLUGIN.getServer().getScheduler().runTaskLater(PLUGIN,dh::delete,25L);
    }


}
