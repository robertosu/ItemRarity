package cl.nightcore.itemrarity.util;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.abstracted.IdentifiedItem;
import cl.nightcore.itemrarity.abstracted.RollQuality;
import cl.nightcore.itemrarity.statprovider.StatProvider;
import cl.nightcore.itemrarity.classes.GodRollQuality;
import cl.nightcore.itemrarity.classes.HighRollQuality;
import cl.nightcore.itemrarity.classes.MediumRollQuality;
import cl.nightcore.itemrarity.config.ItemConfig;
import cl.nightcore.itemrarity.item.BlessingObject;
import cl.nightcore.itemrarity.item.IdentifyScroll;
import cl.nightcore.itemrarity.item.MagicObject;
import cl.nightcore.itemrarity.item.RedemptionObject;
import cl.nightcore.itemrarity.model.GemModel;
import cl.nightcore.itemrarity.statprovider.ArmorStatProvider;
import cl.nightcore.itemrarity.statprovider.WeaponStatProvider;
import com.nexomc.nexo.api.NexoItems;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.item.ModifierType;
import dev.aurelium.auraskills.api.stat.Stat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.text.DecimalFormat;
import java.util.*;

public class ItemUtil {
    public static final Random random = new Random();
    public static final DecimalFormat df = new DecimalFormat("0.#");

    public static boolean isNotEmpty(ItemStack item) {
        return item != null && !item.getType().isAir();
    }

    public static boolean isIdentified(ItemStack item) {
        return checkBooleanTag(item, IdentifiedItem.IDENTIFIER_KEY);
    }

    public static boolean isIdentifyScroll(ItemStack item) {
        return checkBooleanTag(item, IdentifyScroll.getIdentifyScrollKey());
    }

    public static boolean isRedemptionObject(ItemStack item) {
        return checkBooleanTag(item, RedemptionObject.getRedeemObjectKey());
    }

    public static boolean isMagicObject(ItemStack item) {
        return checkBooleanTag(item, MagicObject.getMagicObjectKey());
    }

    public static boolean isBlessingObject(ItemStack item) {
        return checkBooleanTag(item, BlessingObject.getBlessingObjectKey());
    }

    private static boolean checkBooleanTag(ItemStack item, String key) {
        if (item == null || item.getType().isAir() || ItemRarity.plugin == null) {
            return false;
        }
        NamespacedKey namespacedKey = new NamespacedKey(ItemRarity.plugin, key);
        return item.getItemMeta() != null
                && item.getItemMeta().getPersistentDataContainer().has(namespacedKey, PersistentDataType.BOOLEAN)
                && item.getItemMeta().getPersistentDataContainer().get(namespacedKey, PersistentDataType.BOOLEAN)
                        == Boolean.TRUE;
    }

    public static StatProvider getStatProvider(ItemStack item) {
        return switch (getItemType(item)) {
            case "Weapon" -> new WeaponStatProvider();
            case "Armor" -> new ArmorStatProvider();
            default -> null;
        };
    }

    public static String getItemType(ItemStack item) {
        Material material = item.getType();
        // Verificar si es armadura
        if (material.name().endsWith("_HELMET")
                || material.name().endsWith("_CHESTPLATE")
                || material.name().endsWith("_LEGGINGS")
                || material.name().endsWith("_BOOTS")) {
            return "Armor";
        }
        // Verificar si es arma
        else if (material.name().endsWith("_SWORD")
                || material.name().endsWith("_AXE")
                || material == Material.TRIDENT
                || material == Material.BOW
                || material == Material.CROSSBOW) {
            return "Weapon";
        } else {
            // Si no es armadura ni arma, retornar vacío.
            return "Unknown";
        }
    }

    public static Boolean isIdentifiable(ItemStack item) {
        return getItemType(item).equals("Weapon") || getItemType(item).equals("Armor");
    }

    public static ModifierType getModifierType(ItemStack item) {
        return switch (getItemType(item)) {
            case "Weapon" -> ModifierType.ITEM;
            case "Armor" -> ModifierType.ARMOR;
            default -> null;
        };
    }

    public static boolean isGem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(ItemRarity.plugin, GemModel.getGemStatKey());
        return container.has(key, PersistentDataType.STRING);
    }

    public static TextColor getColorOfStat(Stat stat) {
        return TextColor.fromHexString(
                stat.getColor(AuraSkillsApi.get().getMessageManager().getDefaultLanguage())
                        .replaceAll("[<>]", ""));
    }

    public static double calculateTotalDamage(ItemStack item) {
        double baseDamage;
        ItemMeta meta = item.getItemMeta();

        // Manejo de ítems vanilla
        if (NexoItems.idFromItem(item) == null) {
            baseDamage = getDefaultDamage(item.getType());
            Collection<AttributeModifier> baseModifiers = item.getItemMeta().getAttributeModifiers(Attribute.ATTACK_DAMAGE);
            if (baseModifiers != null) {
                for (AttributeModifier modifier : baseModifiers) {
                    if (modifier.getOperation() == AttributeModifier.Operation.ADD_NUMBER) {
                        baseDamage = modifier.getAmount() + 1;
                    }
                }
            }
        }
        // Manejo de ítems Oraxen
        else {
            baseDamage = getDefaultDamage(item.getType());
            if (meta.hasAttributeModifiers()) {
                Collection<AttributeModifier> modifiers = meta.getAttributeModifiers(Attribute.ATTACK_DAMAGE);
                if (modifiers != null) {
                    for (AttributeModifier modifier : modifiers) {
                        if (modifier.getOperation() == AttributeModifier.Operation.ADD_NUMBER) {
                            baseDamage = modifier.getAmount() + 1; // Reemplaza el valor base para ítems Oraxen
                            break; // Asumimos que solo hay un modificador relevante
                        }
                    }
                }
            }
        }

        // Aplicar encantamiento de Sharpness
        int sharpnessLevel = item.getEnchantmentLevel(Enchantment.SHARPNESS);
        double sharpnessDamage = sharpnessLevel > 0 ? (0.5 * sharpnessLevel + 0.5) : 0;

        return baseDamage + sharpnessDamage;
    }

    public static double calculateAttackSpeed(ItemStack item) {
        double baseSpeed = 0;

        Collection<AttributeModifier> baseModifiers = item.getType().getDefaultAttributeModifiers().get(Attribute.ATTACK_SPEED);

        if (NexoItems.idFromItem(item) == null) {
            for (AttributeModifier modifier : baseModifiers) {
                baseSpeed = modifier.getAmount();
            }
            return baseSpeed + 4;
        }

        ItemMeta meta = item.getItemMeta();
        if (NexoItems.idFromItem(item) != null) {
            if (meta.getAttributeModifiers(Attribute.ATTACK_SPEED) != null) {
                for (AttributeModifier modifier : Objects.requireNonNull(meta.getAttributeModifiers(Attribute.ATTACK_SPEED))) {
                    // DEBUG ATTACK SPEED System.out.println(modifier.getAmount());
                    baseSpeed = modifier.getAmount() + 4;
                    return baseSpeed;
                }
            } else if (meta.getAttributeModifiers(Attribute.ATTACK_SPEED) == null) {
                for (AttributeModifier modifier : baseModifiers) {
                    baseSpeed = modifier.getAmount();
                }
                return baseSpeed + 4;
            }
        }
        return baseSpeed;
    }

    private static double getDefaultDamage(Material material) {
        return switch (material) {
            case WOODEN_SWORD, GOLDEN_SWORD -> 4;
            case STONE_SWORD -> 5;
            case IRON_SWORD -> 6;
            case DIAMOND_SWORD, WOODEN_AXE, GOLDEN_AXE -> 7;
            case NETHERITE_SWORD -> 8;
            case STONE_AXE, IRON_AXE, DIAMOND_AXE -> 9;
            case NETHERITE_AXE -> 10;
            default -> 1;
        };
    }

    public static Component calculateRarity(RollQuality rollQuality, double average) {
        TextColor color;
        String rarityText;

        switch (rollQuality) {
            case GodRollQuality godRollQuality -> {
                if (average >= 25.0) {
                    color = ItemConfig.GODLIKE_COLOR;
                    rarityText = "[Divino]";
                } else if (average >= 23.0) {
                    color = ItemConfig.LEGENDARY_COLOR;
                    rarityText = "[Legendario]";
                } else if (average >= 20.5) {
                    color = ItemConfig.EPIC_COLOR;
                    rarityText = "[Épico]";
                } else if (average >= 17.5) {
                    color = ItemConfig.RARE_COLOR;
                    rarityText = "[Raro]";
                } else if (average >= 14.0) {
                    color = ItemConfig.UNCOMMON_COLOR;
                    rarityText = "[Común]";
                } else {
                    color = ItemConfig.COMMON_COLOR;
                    rarityText = "[Basura]";
                }
            }
            case HighRollQuality highRollQuality -> {
                if (average >= 23.5) {
                    color = ItemConfig.GODLIKE_COLOR;
                    rarityText = "[Divino]";
                } else if (average >= 21.5) {
                    color = ItemConfig.LEGENDARY_COLOR;
                    rarityText = "[Legendario]";
                } else if (average >= 19) {
                    color = ItemConfig.EPIC_COLOR;
                    rarityText = "[Épico]";
                } else if (average >= 16) {
                    color = ItemConfig.RARE_COLOR;
                    rarityText = "[Raro]";
                } else if (average >= 12.5) {
                    color = ItemConfig.UNCOMMON_COLOR;
                    rarityText = "[Común]";
                } else {
                    color = ItemConfig.COMMON_COLOR;
                    rarityText = "[Basura]";
                }
            }
            case MediumRollQuality mediumRollQuality -> {
                if (average >= 22.0) {
                    color = ItemConfig.GODLIKE_COLOR;
                    rarityText = "[Divino]";
                } else if (average >= 20.0) {
                    color = ItemConfig.LEGENDARY_COLOR;
                    rarityText = "[Legendario]";
                } else if (average >= 17.5) {
                    color = ItemConfig.EPIC_COLOR;
                    rarityText = "[Épico]";
                } else if (average >= 14.5) {
                    color = ItemConfig.RARE_COLOR;
                    rarityText = "[Raro]";
                } else if (average >= 11.0) {
                    color = ItemConfig.UNCOMMON_COLOR;
                    rarityText = "[Común]";
                } else {
                    color = ItemConfig.COMMON_COLOR;
                    rarityText = "[Basura]";
                }
            }
            case null, default -> {
                if (average >= 19.0) {
                    color = ItemConfig.GODLIKE_COLOR;
                    rarityText = "[Divino]";
                } else if (average >= 17.0) {
                    color = ItemConfig.LEGENDARY_COLOR;
                    rarityText = "[Legendario]";
                } else if (average >= 14.5) {
                    color = ItemConfig.EPIC_COLOR;
                    rarityText = "[Épico]";
                } else if (average >= 11.5) {
                    color = ItemConfig.RARE_COLOR;
                    rarityText = "[Raro]";
                } else if (average >= 8.0) {
                    color = ItemConfig.UNCOMMON_COLOR;
                    rarityText = "[Común]";
                } else {
                    color = ItemConfig.COMMON_COLOR;
                    rarityText = "[Basura]";
                }
            }
        }

        return Component.text(rarityText).color(color).decoration(TextDecoration.ITALIC, false);
    }

    public static void attributesDisplayInLore(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // Restablecer modificadores de atributo si el ítem no es personalizado
        if (NexoItems.idFromItem(item) == null) {
            var defaultModifiers = item.getType().getDefaultAttributeModifiers();
            meta.setAttributeModifiers(defaultModifiers);
            item.setItemMeta(meta);
        }

        double totalDamage = calculateTotalDamage(item);
        double attackSpeed = calculateAttackSpeed(item);
        String attackSpeedDisplay = String.format("%.1f", attackSpeed);

        // Recuperar o inicializar la lore como componentes
        List<Component> lore = meta.hasLore() ? meta.lore() : new ArrayList<>();
        if (lore == null) lore = new ArrayList<>();

        // Filtrar líneas existentes que contengan atributos
        lore.removeIf(line -> line.toString().contains("Daño de ataque") || line.toString().contains("Velocidad de ataque") || line.toString().contains("En la mano") || line.toString().contains("En la mano") || line.toString().contains("          "));

        // Añadir nuevas líneas
        lore.add(Component.text("          "));
        lore.add(Component.text("En la mano principal:", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(" " + df.format(totalDamage) + " ", NamedTextColor.BLUE).append(Component.text("Daño de ataque", NamedTextColor.BLUE)).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(" " + attackSpeedDisplay + " ", NamedTextColor.BLUE).append(Component.text("Velocidad de ataque", NamedTextColor.BLUE)).decoration(TextDecoration.ITALIC, false));

        // Aplicar la nueva lore
        meta.lore(lore);

        // Ocultar atributos por defecto
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
    }
}
