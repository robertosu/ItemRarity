package cl.nightcore.itemrarity.util;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;

public class MobUtil {

    private static final NamespacedKey levelKey = new NamespacedKey("mythicprojectiles", "level");

    public static int getLevel(Entity e){
        return e.getPersistentDataContainer().getOrDefault(levelKey, PersistentDataType.INTEGER, 0);
    }

    //used to calculate accuracy and evasion of based on the entity level provided by mythicprojectiles
    public static double getMobTraits(Entity e){
        int level = getLevel(e);
        return level == 0 ? 0 : (double) level / 2;

    }


}
