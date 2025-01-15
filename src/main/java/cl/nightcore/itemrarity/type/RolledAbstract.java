package cl.nightcore.itemrarity.type;

import cl.nightcore.itemrarity.config.ItemConfig;
import cl.nightcore.itemrarity.item.MagicObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import static cl.nightcore.itemrarity.config.ItemConfig.LEVEL_KEY_NS;
import static cl.nightcore.itemrarity.config.ItemConfig.ROLLCOUNT_KEY_NS;

public class RolledAbstract extends IdentifiedAbstract {

    private static final int MAX_LEVEL = 30;

    public RolledAbstract(ItemStack item) {
        super(item);
    }

    public void incrementLevel(Player player) {
        ItemMeta meta = this.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        int rollcount = container.getOrDefault(ROLLCOUNT_KEY_NS, PersistentDataType.INTEGER, 0);
        if (rollcount < MAX_LEVEL) {
            rollcount += 1;
            Component message = Component.text()
                    .append(Component.text("El objeto aumentó su nivel de magia ", MagicObject.getLoreColor()))
                    .append(Component.text(rollcount-(10*(getLevel()-1)), NamedTextColor.BLUE))
                    .append(Component.text("/", NamedTextColor.DARK_AQUA))
                    .append(Component.text("10", NamedTextColor.BLUE))
                    .build();

            player.sendMessage(ItemConfig.REROLL_PREFIX.append(message));

            if (rollcount == 10) {
                container.set(LEVEL_KEY_NS, PersistentDataType.INTEGER, 2);
                player.sendMessage(ItemConfig.REROLL_PREFIX
                        .append(Component.text("Tu objeto ahora es ", MagicObject.getLoreColor())
                                .append(Component.text("Nivel 2", NamedTextColor.DARK_GRAY))));
            } else if (rollcount == 20) {
                container.set(LEVEL_KEY_NS, PersistentDataType.INTEGER, 3);
                player.sendMessage(ItemConfig.REROLL_PREFIX
                        .append(Component.text("Tu objeto ahora es ", MagicObject.getLoreColor())
                                .append(Component.text("Nivel 3", NamedTextColor.DARK_GRAY))));
            } else if (rollcount == 30) {
                container.set(LEVEL_KEY_NS, PersistentDataType.INTEGER, 4);
                player.sendMessage(ItemConfig.REROLL_PREFIX
                        .append(Component.text("Tu objeto ahora es ", MagicObject.getLoreColor())
                                .append(Component.text("Nivel 4", NamedTextColor.DARK_GRAY))));
            }
            container.set(ROLLCOUNT_KEY_NS, PersistentDataType.INTEGER, rollcount);
            this.setItemMeta(meta);
        }
    }
}
