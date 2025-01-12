package cl.nightcore.itemrarity.type;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.item.MagicObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class RolledWeapon extends IdentifiedWeapon {

    private static final int MAX_LEVEL = 30;

    public RolledWeapon(ItemStack item) {
        super(item);
    }

    public void incrementLevel(Player player) {
        ItemMeta meta = this.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, LEVEL_KEY);
        int lvl = container.getOrDefault(key, PersistentDataType.INTEGER, 0);

        if (lvl < MAX_LEVEL) {
            lvl += 1;
            Component message = Component.text()
                    .append(Component.text("El item subió su nivel de magia ", MagicObject.getLoreColor()))
                    .append(Component.text(lvl, NamedTextColor.BLUE))
                    .append(Component.text(" / ", NamedTextColor.DARK_AQUA))
                    .append(Component.text(MAX_LEVEL, NamedTextColor.BLUE))
                    .build();

            player.sendMessage(ItemRarity.getRerollPrefix().append(message));

            if (lvl == 10) {
                player.sendMessage(ItemRarity.getRerollPrefix()
                        .append(Component.text("Tu objeto subió a ", MagicObject.getLoreColor())
                                .append(Component.text("Nivel 2", NamedTextColor.BLUE))));
            } else if (lvl == 20) {
                player.sendMessage(ItemRarity.getRerollPrefix()
                        .append(Component.text("Tu objeto subió a ", MagicObject.getLoreColor())
                                .append(Component.text("Nivel 3", NamedTextColor.BLUE))));
            } else if (lvl == 30) {
                player.sendMessage(ItemRarity.getRerollPrefix()
                        .append(Component.text("Tu objeto subió a ", MagicObject.getLoreColor())
                                .append(Component.text("Nivel 4", NamedTextColor.BLUE))));
            }

            container.set(key, PersistentDataType.INTEGER, lvl);
            this.setItemMeta(meta);
        }
    }
}
