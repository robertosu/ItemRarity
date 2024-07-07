package cl.nightcore.itemrarity.abstracted;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.classes.*;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.AuraSkillsBukkit;
import dev.aurelium.auraskills.api.item.ModifierType;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.StatModifier;
import dev.aurelium.auraskills.api.stat.Stats;
import dev.aurelium.auraskills.api.util.AuraSkillsModifier;
import io.th0rgal.oraxen.api.OraxenItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static cl.nightcore.itemrarity.ItemRarity.isIdentified;

public abstract class IdentifiedItem extends ItemStack {

    protected static final String ROLL_IDENTIFIER_KEY = "roll_count";
    protected static final String LEVEL_KEY = "magicobject_roll_lvl";
    private static final String IDENTIFIER_KEY = "is_identify_scrolled";
    private static final String COMMON_RARITY_KEYWORD = "Común";
    private static final String RARE_RARITY_KEYWORD = "Raro";
    private static final String MYTHIC_RARITY_KEYWORD = "Mítico";
    private static final String LEGENDARY_RARITY_KEYWORD = "Legendario";
    private static final TextColor COMMON_COLOR = NamedTextColor.DARK_AQUA;
    private static final TextColor RARE_COLOR = NamedTextColor.LIGHT_PURPLE;
    private static final TextColor MYTHIC_COLOR = NamedTextColor.DARK_PURPLE;
    private static final TextColor LEGENDARY_COLOR = NamedTextColor.GOLD;
    private final List<Stat> addedStats;
    private final List<Integer> statValues;
    protected ModifierType MODIFIER_TYPE;
    private String rarity;
    protected StatProvider statProvider;
    protected RollQuality rollQuality;
    Component reset = Component.text()
            .content("")
            .color(NamedTextColor.WHITE)
            .decoration(TextDecoration.ITALIC, false)
            .build();

    public IdentifiedItem(ItemStack item) {
        super(item);
        rollQuality = getRollQuality();
        this.addedStats = new ArrayList<>();
        this.statValues = new ArrayList<>();
        this.MODIFIER_TYPE = plugin.getModifierType(item);
        if (!isIdentified(item)) {
            generateStats();
            applyStatsToItem();
            setRarity();
            setIdentifiedNBT();
            setLore();
        }
    }
    private int getItemLevel(){
        if(getRollQuality() instanceof LowRollQuality){
            return 1;
        } else if (getRollQuality() instanceof MediumRollQuality) {
            return 2;
        } else if (getRollQuality() instanceof HighRollQuality) {
            return 3;
        } else if (getRollQuality() instanceof GodRollQuality) {
            return 4;
        }
        return 0;
    }
    protected abstract void generateStats();
    public void rerollStats(){
        emptyLore();
        removeModifiers();
        generateStats();
        applyStatsToItem();
        setRarity();
        setLore();
    }
    private void removeSpecificModifier(Stat stat) {
        this.setItemMeta (AuraSkillsBukkit.get().getItemManager().removeModifier(this, MODIFIER_TYPE, stat).getItemMeta());
    }
    private void addModifier(Stat stat, int value){
       this.setItemMeta(AuraSkillsBukkit.get().getItemManager().addModifier(this, MODIFIER_TYPE, stat, value, true).getItemMeta());
    }
    private Stat getLowestModifier(){
        StatModifier lowestModifier = null;
        double lowestValue = 100;
        for (StatModifier modifier : getStatModifiers()) {
            double value = modifier.value();
            if (value < lowestValue) {
                lowestValue = value;
                lowestModifier = modifier;
            }
        }
       return lowestModifier.type();
    }
    private void removeSpecificStatLoreLine(Stat lowestStat) {
        ItemMeta meta = this.getItemMeta();
        List<String> lore = Objects.requireNonNull(meta).getLore();
        if (lore != null) {
            String statDisplayName = lowestStat.getDisplayName(AuraSkillsApi.get().getMessageManager().getDefaultLanguage());
            lore.removeIf(line -> line.contains(statDisplayName));
            meta.setLore(lore);
            this.setItemMeta(meta);
        }
    }
    private Stat getHighestStat() {
        StatModifier highestModifier = null;
        double highestValue = 0;
        for (StatModifier modifier : getStatModifiers()) {
            double value = modifier.value();
            if (value > highestValue) {
                highestValue = value;
                highestModifier = modifier;
            }
        }
        return highestModifier.type();
    }

    private double getHighestStatValue() {
        StatModifier highestModifier = null;
        double highestValue = 0;
        for (StatModifier modifier : getStatModifiers()) {
            double value = modifier.value();
            if (value > highestValue) {
                highestValue = value;
                highestModifier = modifier;
            }
        }
        return highestModifier.value();
    }
    public void rerollExceptHighestStat(Player player) {
        Stat highestStat = getHighestStat();
        int highestValue = (int) getHighestStatValue();
        removeAllModifierStats();
        generateStatsExceptHighestStat(highestStat);
        applyStatsToItem();
        addModifier(highestStat, highestValue);
        updateRarityLore();
        player.sendMessage(ItemRarity.getRedemptionPrefix() + "¡Cambiaron las estadísticas! Se mantuvo: " + highestStat.getColoredName(AuraSkillsApi.get().getMessageManager().getDefaultLanguage()));
    }
    protected void generateStatsExceptHighestStat(Stat excludedStat) {
        Random random = new Random();
        int statsCount = random.nextInt(2) + 4; // 4 o 5 estadísticas
        StatProvider statProvider = ItemRarity.getStatProvider(this);
        List<Stats> availableStats = statProvider.getAvailableStats();
        addedStats.clear();
        statValues.clear();
        for (Stat stat : statProvider.getGaussStats()) {
            if (stat != excludedStat) {
                getAddedStats().add(stat);
                int value = StatValueGenerator.generateValueForStat(getRollQuality(), statProvider.isThisStatGauss(stat));
                getStatValues().add(value);
            }
        }
        int gaussStatsAdded = getAddedStats().size();
        for (int i = 0; i < statsCount - gaussStatsAdded; i++) {
            Stats stat;
            do {
                stat = availableStats.get(random.nextInt(availableStats.size()));
            } while (getAddedStats().contains(stat) || stat == excludedStat);
            getAddedStats().add(stat);
            int value = StatValueGenerator.generateValueForStat(getRollQuality(), statProvider.isThisStatGauss(stat));
            getStatValues().add(value);
        }
    }
    private void removeAllModifierStats() {
        for (StatModifier stat : getStatModifiers()) {
                removeSpecificModifier(stat.type());
                removeSpecificStatLoreLine(stat.type());

        }
    }
    public void rerollLowestStat(Player player){
        Stat lowestModifier = getLowestModifier();
        removeSpecificStatLoreLine(lowestModifier);
        removeSpecificModifier(lowestModifier);
        int newValue = StatValueGenerator.generateValueForStat(getRollQuality(), statProvider.isThisStatGauss(lowestModifier));
        addModifier(lowestModifier,newValue);
        updateRarityLore();
        player.sendMessage(ItemRarity.getBlessingPrefix()+"Se cambió la stat " + lowestModifier.getColoredName(AuraSkillsApi.get().getMessageManager().getDefaultLanguage()));
    }

    protected void applyStatsToItem() {
        for (int i = this.addedStats.size() - 1; i >= 0; i--) {
            Stat stat = this.addedStats.get(i);
            double value = this.statValues.get(i);
            this.setItemMeta(AuraSkillsBukkit.get().getItemManager().addModifier(this, MODIFIER_TYPE, stat, value, true).getItemMeta());
        }
    }
    public void removeModifiers() {
        for (Stats stat : ItemRarity.STATS) {
            this.setItemMeta(AuraSkillsBukkit.get().getItemManager().removeModifier(this, MODIFIER_TYPE, stat).getItemMeta());
        }
    }
    protected ItemRarity plugin = (ItemRarity) Bukkit.getPluginManager().getPlugin("ItemRarity");

    protected  void emptyLore(){
        ItemMeta meta = getItemMeta();
        List<String> emptylore = new ArrayList<>();
        assert meta != null;
        meta.setLore(emptylore);
        setItemMeta(meta);
    }

    public RollQuality getRollQuality(){
        int level = getLevel();
        if (level >= 30) {
            return new GodRollQuality();
        } else if (level >= 20) {
            return new HighRollQuality();
        } else if (level >= 10) {
            return new MediumRollQuality();
        } else {
            return new LowRollQuality();
        }
    }
    private void updateRarityLore() {
        ItemMeta meta = getItemMeta();
        List<String> lore = meta.getLore();
        if (lore != null) {
            lore.removeIf(line -> line.contains(COMMON_RARITY_KEYWORD) ||
                    line.contains(RARE_RARITY_KEYWORD) ||
                    line.contains(MYTHIC_RARITY_KEYWORD) ||
                    line.contains(LEGENDARY_RARITY_KEYWORD));
            meta.setLore(lore);
            setItemMeta(meta);
        }
        setRarity();
        setLore();
    }
    private void setRarity() {
        if (!getStatModifiers().isEmpty()) {
            double average = getStatModifiers().stream().mapToDouble(AuraSkillsModifier::value).average().orElse(0.0);  // Devuelve 0.0 si la lista está vacía
            switch (rollQuality.getClass().getSimpleName()) {
                case "GodRollQuality":
                    if (average >= 22.5) {
                        rarity = ChatColor.GOLD + "" + ChatColor.ITALIC + " [Legendario]";
                    } else if (average >= 18.75) {
                        rarity = ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + " [Mítico]";
                    } else if (average >= 16.75) {
                        rarity = ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + " [Raro]";
                    } else {
                        rarity = ChatColor.DARK_AQUA + "" + ChatColor.ITALIC + " [Común]";
                    }
                    break;

                case "HighRollQuality":
                    if (average >= 19.5) {
                        rarity = ChatColor.GOLD + "" + ChatColor.ITALIC + " [Legendario]";
                    } else if (average >= 17) {
                        rarity = ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + " [Mítico]";
                    } else if (average >= 14.75) {
                        rarity = ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + " [Raro]";
                    } else {
                        rarity = ChatColor.DARK_AQUA + "" + ChatColor.ITALIC + " [Común]";
                    }
                    break;

                case "MediumRollQuality":
                    if (average >= 17) {
                        rarity = ChatColor.GOLD + "" + ChatColor.ITALIC + " [Legendario]";
                    } else if (average >= 15.75) {
                        rarity = ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + " [Mítico]";
                    } else if (average >= 12.75) {
                        rarity = ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + " [Raro]";
                    } else {
                        rarity = ChatColor.DARK_AQUA + "" + ChatColor.ITALIC + " [Común]";
                    }
                    break;

                default: // LowRollQuality
                    if (average >= 16) {
                        rarity = ChatColor.GOLD + "" + ChatColor.ITALIC + " [Legendario]";
                    } else if (average >= 14.75) {
                        rarity = ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + " [Mítico]";
                    } else if (average >= 11.75) {
                        rarity = ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + " [Raro]";
                    } else {
                        rarity = ChatColor.DARK_AQUA + "" + ChatColor.ITALIC + " [Común]";
                    }
                    break;
            }
        }

    }
    private TextColor getRarityColor() {
        if (rarity.contains("Legendario")) {
            return LEGENDARY_COLOR;
        } else if (rarity.contains("Mítico")) {
            return MYTHIC_COLOR;
        } else if (rarity.contains("Raro")) {
            return RARE_COLOR;
        } else {
            return COMMON_COLOR;
        }
    }
    public int getLevel() {
        PersistentDataContainer container = this.getItemMeta().getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, LEVEL_KEY);
        return container.getOrDefault(key, PersistentDataType.INTEGER, 0);
    }
    public void setIdentifiedNBT() {
        ItemMeta meta = this.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, IDENTIFIER_KEY);
        container.set(key, PersistentDataType.BOOLEAN, true);
        this.setItemMeta(meta);
    }
    private void setLore() {
        int itemlevel = getItemLevel();
        ItemMeta meta = getItemMeta();
        List<String> lore;
        if (meta.getLore() != null) {
            lore = new ArrayList<>(meta.getLore());
        } else {
            lore = new ArrayList<>();
        }
        lore.add(rarity + ChatColor.DARK_GRAY + " - Nvl " + itemlevel + " -");
        meta.setLore(lore);

        //oraxen weapon
        if (OraxenItems.getIdByItem(this)!=null && ItemRarity.getItemType(this).equals("Weapon")) {
            Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(ChatColor.stripColor(meta.getItemName()).toString());
            component = component.decoration(TextDecoration.ITALIC,false);
            meta.displayName(component.color(getRarityColor()));
            setItemMeta(meta);
            attributesDisplayInLore(this);
        //oraxen armor
        }else if (OraxenItems.getIdByItem(this)!=null && ItemRarity.getItemType(this).equals("Armor")) {

            Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(ChatColor.stripColor(meta.getDisplayName()).toString());
            component = component.decoration(TextDecoration.ITALIC,false);
            meta.displayName(component.color(getRarityColor()));
            setItemMeta(meta);
            //normal identifiable item
        }
        else if (OraxenItems.getIdByItem(this)==null){
            // Obtener la key de traducción
            if (!meta.hasDisplayName()) {
                String itemTranslationKey = this.getTranslationKey();
                TranslatableComponent translatedName = Component.translatable(itemTranslationKey).color(getRarityColor());
                Component newName = reset.append(translatedName);
                meta.displayName(newName);
                setItemMeta(meta);
                attributesDisplayInLore(this);
            }else{
                Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(ChatColor.stripColor(meta.getDisplayName()).toString());
                component = component.decoration(TextDecoration.ITALIC,false);
                meta.displayName(component.color(getRarityColor()));
                setItemMeta(meta);
                attributesDisplayInLore(this);
            }
        }
    }

    private List<StatModifier> getStatModifiers() {
        return AuraSkillsBukkit.get().getItemManager().getModifiers(this, MODIFIER_TYPE);
    }
    public List<Integer> getStatValues() {
        return statValues;
    }
    public List<Stat> getAddedStats() {
        return addedStats;
    }
    public String getItemRarity() {
        return rarity;
    }
    public static String getIdentifierKey(){
        return IDENTIFIER_KEY;
    }
    public static void attributesDisplayInLore(ItemStack item) {
        ItemMeta meta = item.getItemMeta();

        double totalDamage = calculateTotalDamage(item);
        double attackSpeed = calculateAttackSpeed(item);

        List<String> lore = meta.getLore();
        if(lore==null){
            lore = new ArrayList<>();
        }
        List<String> aEliminar = new ArrayList<>();
        for (String linea : lore) {
            if (linea.contains("Daño de ataque")||linea.contains("Velocidad de ataque")||linea.contains("En la mano")||linea.contains("          ")) {
                aEliminar.add(linea);
            }
        }
        lore.removeAll(aEliminar);
        lore.add("          ");
        lore.add(ChatColor.GRAY + "En la mano principal:");
        lore.add(ChatColor.BLUE + "Daño de ataque: " + "+" + String.format("%.1f", Math.round(totalDamage * 10.0) / 10.0));
        lore.add(ChatColor.BLUE + "Velocidad de ataque: " + "+" + String.format("%.1f", attackSpeed));

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        //meta.setHideTooltip(true);
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    private static double calculateTotalDamage(ItemStack item) {
        double baseDamage = 0;
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasAttributeModifiers()) {
            for (AttributeModifier modifier : meta.getAttributeModifiers(Attribute.GENERIC_ATTACK_DAMAGE)) {
                if (modifier.getOperation() == AttributeModifier.Operation.ADD_NUMBER) {
                    baseDamage += modifier.getAmount();
                }
            }
        }

        if (baseDamage == 0) {
            baseDamage = getDefaultDamage(item.getType());
        }

        int sharpnessLevel = item.getEnchantmentLevel(Enchantment.SHARPNESS);
        double sharpnessDamage = sharpnessLevel > 0 ? (0.5 * sharpnessLevel + 0.5) : 0;

        return baseDamage + sharpnessDamage;
    }

    private static double calculateAttackSpeed(ItemStack item) {
        double baseSpeed = getDefaultAttackSpeed(item.getType());
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasAttributeModifiers()) {
            for (AttributeModifier modifier : meta.getAttributeModifiers(Attribute.GENERIC_ATTACK_SPEED)) {
                if (modifier.getOperation() == AttributeModifier.Operation.ADD_NUMBER) {
                    baseSpeed = modifier.getAmount();
                }
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

    private static double getDefaultAttackSpeed(Material material) {
        return switch (material) {
            case WOODEN_SWORD, STONE_SWORD, IRON_SWORD, DIAMOND_SWORD, NETHERITE_SWORD, GOLDEN_SWORD -> 1.6;
            case WOODEN_AXE, STONE_AXE, IRON_AXE, DIAMOND_AXE, NETHERITE_AXE, GOLDEN_AXE -> 1.0;
            default -> 4.0; // Valor por defecto para otros items
        };
    }

}
