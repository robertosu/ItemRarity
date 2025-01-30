package cl.nightcore.itemrarity.type;

import cl.nightcore.itemrarity.config.ItemConfig;
import cl.nightcore.itemrarity.item.ItemUpgrader;
import cl.nightcore.itemrarity.item.MagicObject;
import cl.nightcore.itemrarity.model.ItemUpgraderModel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

import static cl.nightcore.itemrarity.ItemRarity.PLUGIN;
import static cl.nightcore.itemrarity.config.ItemConfig.LEVEL_KEY_NS;
import static cl.nightcore.itemrarity.config.ItemConfig.ROLLCOUNT_KEY_NS;

@SuppressWarnings("UnstableApiUsage")
public class RolledAbstract extends IdentifiedAbstract {
    private static final int MAX_ROLLCOUNT = 30;
    private static final int MAX_LEVEL = 9;
    private static final int ROLLS_PER_LEVEL = 10;
    private static final NamespacedKey DAMAGE_MODIFIER_KEY = new NamespacedKey(PLUGIN, "level_damage");


    public RolledAbstract(ItemStack item) {
        super(item);
    }

    public void incrementRollCount(Player player) {
        ItemMeta meta = this.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        int rollcount = container.getOrDefault(ROLLCOUNT_KEY_NS, PersistentDataType.INTEGER, 0);

        if (rollcount < MAX_ROLLCOUNT) {
            rollcount += 1;
            int currentLevel = getLevel();
            int rollsInCurrentLevel = rollcount - (ROLLS_PER_LEVEL * (currentLevel - 1));

            // Mensaje de progreso
            Component message = Component.text()
                    .append(Component.text("El objeto aumentó su nivel de magia ", MagicObject.getLoreColor()))
                    .append(Component.text(rollsInCurrentLevel, NamedTextColor.BLUE))
                    .append(Component.text("/", NamedTextColor.DARK_AQUA))
                    .append(Component.text(ROLLS_PER_LEVEL, NamedTextColor.BLUE))
                    .build();

            player.sendMessage(ItemConfig.REROLL_PREFIX.append(message));

            // Comprobar si debe subir de nivel
            int newLevel = (rollcount / ROLLS_PER_LEVEL) + 1;
            if (newLevel > currentLevel) {
                container.set(LEVEL_KEY_NS, PersistentDataType.INTEGER, newLevel);
                player.sendMessage(ItemConfig.REROLL_PREFIX
                        .append(Component.text("Tu objeto ahora es ", MagicObject.getLoreColor())
                                .append(Component.text("Nivel " + newLevel, NamedTextColor.DARK_GRAY))));
            }

            container.set(ROLLCOUNT_KEY_NS, PersistentDataType.INTEGER, rollcount);
            this.setItemMeta(meta);
        }
    }

    public boolean incrementLevel(Player player, ItemUpgraderModel itemUpgrader) {


        ItemMeta meta = this.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        int level = container.get(LEVEL_KEY_NS, PersistentDataType.INTEGER);
        int type = itemUpgrader.getType();
        int percentage = itemUpgrader.getPercentage();

        // Aumentar la probabilidad de fallo en un 5% por cada nivel del objeto
        double adjustedPercentage = percentage - (level * 5);
        if (adjustedPercentage < 0) adjustedPercentage = 0; // Asegurar que no sea negativo

        if (level < MAX_LEVEL) {
            if (rollthedice(adjustedPercentage)) {
                int newlevel = level + 1;
                container.set(LEVEL_KEY_NS, PersistentDataType.INTEGER, newlevel);
                player.sendMessage(ItemConfig.ITEM_UPGRADER_PREFIX.color(ItemUpgraderModel.getPrimaryColor(itemUpgrader.getType()))
                        .append(Component.text("Mejora exitosa, tu objeto subió a: ", ItemUpgrader.getLoreColor())
                                .append(Component.text("Nivel " + newlevel, NamedTextColor.DARK_GRAY))));
                this.setItemMeta(meta);
                this.modifyDamage(0.2*level);
                this.setLore();
                // Reproducir sonido de éxito
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            } else {
                switch (type) {
                    case 1 -> { // Inestable
                        player.sendMessage(ItemConfig.ITEM_UPGRADER_PREFIX.color(ItemUpgraderModel.getPrimaryColor(itemUpgrader.getType()))
                                .append(Component.text("La mejora falló y tu objeto se rompió.", NamedTextColor.RED)));
                        this.setAmount(0); // Romper el objeto

                        // Reproducir sonido de rotura
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                    }
                    case 2 -> { // Activa
                        if (level > 1) {
                            int newlevel = level - 1;
                            container.set(LEVEL_KEY_NS, PersistentDataType.INTEGER, newlevel);
                            player.sendMessage(ItemConfig.ITEM_UPGRADER_PREFIX.color(ItemUpgraderModel.getPrimaryColor(itemUpgrader.getType()))
                                    .append(Component.text("La mejora falló, tu objeto bajó a: ", NamedTextColor.RED)
                                            .append(Component.text("Nivel " + newlevel, ItemUpgrader.getActiveColor()))));
                            this.setItemMeta(meta);
                            this.modifyDamage(0.2*newlevel);
                            this.setLore();

                            // Reproducir sonido de fallo (puedes usar un sonido diferente si lo deseas)
                            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 1.0f, 1.0f);
                        } else {
                            player.sendMessage(ItemConfig.ITEM_UPGRADER_PREFIX.color(ItemUpgraderModel.getPrimaryColor(itemUpgrader.getType()))
                                    .append(Component.text("La mejora falló.", NamedTextColor.RED)));

                            // Reproducir sonido de fallo
                            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 1.0f, 1.0f);
                        }
                    }
                    case 3 -> { // Estable
                        player.sendMessage(ItemConfig.ITEM_UPGRADER_PREFIX.color(ItemUpgraderModel.getPrimaryColor(itemUpgrader.getType()))
                                .append(Component.text("La mejora falló, pero tu objeto no cambió.", NamedTextColor.YELLOW)));

                        // Reproducir sonido de fallo (puedes usar un sonido diferente si lo deseas)
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
                    }
                }
            }
            return true;
        } else {
            player.sendMessage(ItemConfig.ITEM_UPGRADER_PREFIX
                    .append(Component.text("Tu objeto ya es del nivel máximo. ", ItemUpgrader.getLoreColor())));

            // Reproducir sonido de error o advertencia
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            return false;
        }
    }

    private void modifyDamage(double amount) {
        if (!this.hasItemMeta()) {
            return; // Si el ItemStack no tiene meta, no hacemos nada.
        }

        ItemMeta meta = this.getItemMeta();


        // Obtener los modificadores existentes (puede ser null si no hay modificadores)
        Collection<AttributeModifier> modifiers = meta.getAttributeModifiers(Attribute.ATTACK_DAMAGE);

        // Buscar el modificador existente (si existe)
        AttributeModifier existingModifier = null;
        if (modifiers != null) {
            existingModifier = modifiers.stream()
                    .filter(modifier -> DAMAGE_MODIFIER_KEY.equals(modifier.getKey()))
                    .findFirst()
                    .orElse(null);
        }

        if (existingModifier != null) {
            meta.removeAttributeModifier(Attribute.ATTACK_DAMAGE, existingModifier);
        }

        // Crear un nuevo modificador con el valor actualizado
        AttributeModifier damageModifier = new AttributeModifier(
                DAMAGE_MODIFIER_KEY, // Usamos la misma clave
                amount, // Nuevo valor de daño
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.HAND// Operación (sumar)
        );

        // Agregar el modificador al ItemMeta
        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, damageModifier);

        // Establecer el ItemMeta modificado en el ItemStack
        this.setItemMeta(meta);
    }


    private boolean rollthedice(double percentage){
        double chance = percentage / 100.0;
        return chance > ThreadLocalRandom.current().nextDouble();
    }
}
