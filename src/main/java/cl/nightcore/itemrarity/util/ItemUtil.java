package cl.nightcore.itemrarity.util;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.config.ItemConfig;
import cl.nightcore.itemrarity.item.*;
import cl.nightcore.itemrarity.model.GemModel;
import cl.nightcore.itemrarity.statprovider.*;
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
import java.util.concurrent.ThreadLocalRandom;

import static cl.nightcore.itemrarity.ItemRarity.AURA_LOCALE;

public class ItemUtil {

    public static final Random RANDOM = new Random();
    public static final DecimalFormat DF = new DecimalFormat("0.#");
    public static Component reset = Component.text().content("").color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false).build();

    public static boolean isNotEmpty(ItemStack item) {
        return item != null && !item.getType().isAir();
    }

    public static boolean isIdentified(ItemStack item) {
        return checkBooleanTag(item, ItemConfig.SCROLLED_IDENTIFIER_KEY);
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
    public static boolean isBlessingBall(ItemStack item) {
        return checkBooleanTag(item, ItemConfig.BLESSING_BALL_KEY);
    }

    private static boolean checkBooleanTag(ItemStack item, String key) {
        if (item == null || item.getType().isAir() || ItemRarity.PLUGIN == null) {
            return false;
        }
        NamespacedKey namespacedKey = new NamespacedKey(ItemRarity.PLUGIN, key);
        return item.getItemMeta() != null
                && item.getItemMeta().getPersistentDataContainer().has(namespacedKey, PersistentDataType.BOOLEAN)
                && item.getItemMeta().getPersistentDataContainer().get(namespacedKey, PersistentDataType.BOOLEAN)
                        == Boolean.TRUE;
    }

    public static ModifierProvider getProvider(ItemStack item){
        Material material = item.getType();
        // Verificar si es armadura
        if (material.name().endsWith("_HELMET")){
            return new HelmetModifierProvider();
        }
        else if (material.name().endsWith("_CHESTPLATE")){
            return new ChestplateModifierProvider();
        }
        else if (material.name().endsWith("_LEGGINGS")){
            return new LeggingsModifierProvider();
        }
        else if (material.name().endsWith("_BOOTS")){
            return new BootsModifierProvider();
        }
        else if (material.name().endsWith("_SWORD")
                || material.name().endsWith("_AXE")
                || material == Material.TRIDENT
                || material == Material.BOW
                || material == Material.CROSSBOW) {
            return new WeaponModifierProvider();
        } else {
            // Si no es armadura ni arma, retornar vacío.
            throw new IllegalArgumentException("Llamada ilegal de metodo");
        }
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
                || material == Material.TRIDENT){
            return "Weapon";
        }
        else if(material == Material.BOW || material == Material.CROSSBOW){
            return "OtherWeapon";
        }
        else {
            // Si no es armadura ni arma, retornar vacío.
            return "Unknown";
        }
    }

    public static boolean isIdentifiable(ItemStack item) {
        return getItemType(item).equals("Weapon") || getItemType(item).equals("Armor") || getItemType(item).equals("OtherWeapon");
    }

    public static ModifierType getModifierType(ItemStack item) {
        return switch (getItemType(item)) {
            case "Weapon","OtherWeapon" -> ModifierType.ITEM;
            case "Armor" -> ModifierType.ARMOR;
            default -> null;
        };
    }

    public static boolean isGem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        return container.has(GemModel.getGemStatKeyNs(), PersistentDataType.STRING);

    }

    public static boolean isItemUpgrader(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        return container.has(ItemUpgrader.getItemUpgraderKeyNs(), PersistentDataType.INTEGER);
    }

    public static boolean isSocketStone(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        return container.has(SocketStone.getSocketGemKeyNs(), PersistentDataType.BOOLEAN);
    }

    public static boolean isGemRemover(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        return container.has(GemRemover.getGemRemoverKeyNs(), PersistentDataType.INTEGER);
    }

    public static TextColor getColorOfStat(Stat stat) {
        return TextColor.fromHexString(stat.getColor(AURA_LOCALE).replaceAll("[<>]", ""));
    }

    public static double calculateTotalDamage(ItemStack item, double baseDamage) {

        double sharpnessDamage = calculateSharpnessDamage(item);

        return baseDamage + sharpnessDamage;
    }

    private static double calculateSharpnessDamage(ItemStack item) {
        int sharpnessLevel = item.getEnchantmentLevel(Enchantment.SHARPNESS);
        return sharpnessLevel > 0 ? (0.5 * sharpnessLevel + 0.5) : 0;
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

    public static void attributesDisplayInLore(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        //set the default modifiers so minecraft don't mess with the green vanilla attributes
        if(meta.getAttributeModifiers() == null){
            //System.out.println("Set the default modifiers");
            var defaultModifiers = item.getType().getDefaultAttributeModifiers();
            meta.setAttributeModifiers(defaultModifiers);
            item.setItemMeta(meta);
        }
        double baseDamage = 1;

        Collection<AttributeModifier> modifiers = meta.getAttributeModifiers(Attribute.ATTACK_DAMAGE);
        if (modifiers != null) {
            for (AttributeModifier modifier : modifiers) {
                if (modifier.getOperation() == AttributeModifier.Operation.ADD_NUMBER) {
                    baseDamage = baseDamage + modifier.getAmount(); // Usar el valor del modificador
                }
            }
        }

        double totalDamage = calculateTotalDamage(item, baseDamage);
        double attackSpeed = calculateAttackSpeed(item);
        String attackSpeedDisplay = String.format("%.1f", attackSpeed);

        // Recuperar o inicializar la lore como componentes
        List<Component> lore = meta.hasLore() ? meta.lore() : new ArrayList<>();
        if (lore == null) lore = new ArrayList<>();
        // Filtrar líneas existentes que contengan atributos sin quitar la primera

        lore.removeIf(line ->  !line.toString().contains("|") && (line.toString().contains("Daño p") || line.toString().contains("Velocidad d") || line.toString().contains("En la mano") || line.toString().contains("          ")));
        // Añadir nuevas líneas
        lore.add(Component.text("          "));
        lore.add(Component.text("En la mano principal:", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(" " + DF.format(totalDamage) + " ", NamedTextColor.BLUE).append(Component.text("Daño por ataque", NamedTextColor.BLUE)).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(" " + attackSpeedDisplay + " ", NamedTextColor.BLUE).append(Component.text("Velocidad de ataque", NamedTextColor.BLUE)).decoration(TextDecoration.ITALIC, false));

        // Aplicar la nueva lore
        meta.lore(lore);
        // Ocultar atributos por defecto
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        item.setItemMeta(meta);
    }

    public static boolean rollthedice(double percentage){
        double chance = percentage / 100.0;
        return chance > ThreadLocalRandom.current().nextDouble();
    }
}
