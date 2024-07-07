package cl.nightcore.itemrarity.type;

import cl.nightcore.itemrarity.ItemRarity;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class RolledArmor extends IdentifiedArmor{

    private static final int MAX_LEVEL = 30;

    public RolledArmor(ItemStack item) {
        super(item);
        setNBTTag();
        rollQuality = getRollQuality();
    }
    private void setNBTTag() {
        ItemMeta meta = this.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, ROLL_IDENTIFIER_KEY);
        container.set(key, PersistentDataType.INTEGER, 1);
        this.setItemMeta(meta);
    }
    public void incrementLevel(Player player) {
        ItemMeta meta = this.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, LEVEL_KEY);
        int lvl = container.getOrDefault(key, PersistentDataType.INTEGER, 0);

        if (lvl < MAX_LEVEL) {
            lvl += 1;
            player.sendMessage(ItemRarity.getRerollPrefix() + "El item subió su nivel de magia " + ChatColor.BLUE + lvl + ChatColor.AQUA + " / " + ChatColor.BLUE + MAX_LEVEL);

            if (lvl == 10) {
                player.sendMessage(ItemRarity.getRerollPrefix() + "Tu objeto subió a " + ChatColor.BLUE + "Nivel 2");
            } else if (lvl == 20) {
                player.sendMessage(ItemRarity.getRerollPrefix() + "Tu objeto subió a " + ChatColor.BLUE + "Nivel 3");
            } else if (lvl == 30) {
                player.sendMessage(ItemRarity.getRerollPrefix() + "Tu objeto subió a " + ChatColor.BLUE + "Nivel 4");
            }

            container.set(key, PersistentDataType.INTEGER, lvl);
            this.setItemMeta(meta);
        }
    }
}


