package cl.nightcore.itemrarity.util;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.abstracted.IdentifiedItem;
import cl.nightcore.itemrarity.abstracted.StatProvider;
import cl.nightcore.itemrarity.item.BlessingObject;
import cl.nightcore.itemrarity.item.IdentifyScroll;
import cl.nightcore.itemrarity.item.MagicObject;
import cl.nightcore.itemrarity.item.RedemptionObject;
import cl.nightcore.itemrarity.model.GemModel;
import cl.nightcore.itemrarity.statprovider.ArmorStatProvider;
import cl.nightcore.itemrarity.statprovider.WeaponStatProvider;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.item.ModifierType;
import dev.aurelium.auraskills.api.stat.Stat;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ItemUtil {
    public static boolean isNotEmpty(ItemStack item) {
        return item != null && !item.getType().isAir();
    }

    public static boolean isIdentified(ItemStack item) {
        return checkBooleanTag(item, IdentifiedItem.getIdentifierKey());
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
        return item.getItemMeta() != null && item.getItemMeta().getPersistentDataContainer().has(namespacedKey, PersistentDataType.BOOLEAN) && item.getItemMeta().getPersistentDataContainer().get(namespacedKey, PersistentDataType.BOOLEAN) == Boolean.TRUE;
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
        if (material.name().endsWith("_HELMET") || material.name().endsWith("_CHESTPLATE") || material.name().endsWith("_LEGGINGS") || material.name().endsWith("_BOOTS")) {
            return "Armor";
        }
        // Verificar si es arma
        else if (material.name().endsWith("_SWORD") || material.name().endsWith("_AXE") || material == Material.TRIDENT || material == Material.BOW || material == Material.CROSSBOW) {
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

    public static TextColor getColorOfStat(Stat stat){
        return TextColor.fromHexString(stat.getColor(AuraSkillsApi.get().getMessageManager().getDefaultLanguage())
                .replaceAll("[<>]", ""));

    }
}
