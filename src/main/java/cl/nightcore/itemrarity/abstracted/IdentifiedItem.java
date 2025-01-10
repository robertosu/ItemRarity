package cl.nightcore.itemrarity.abstracted;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.classes.*;
import cl.nightcore.itemrarity.item.BlessingObject;
import cl.nightcore.itemrarity.item.RedemptionObject;
import cl.nightcore.itemrarity.util.ItemUtil;
import com.nexomc.nexo.api.NexoItems;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.AuraSkillsBukkit;
import dev.aurelium.auraskills.api.item.ModifierType;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.StatModifier;
import dev.aurelium.auraskills.api.stat.Stats;
import dev.aurelium.auraskills.api.util.AuraSkillsModifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
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
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.*;

import static cl.nightcore.itemrarity.util.ItemUtil.isIdentified;

public abstract class IdentifiedItem extends ItemStack {

    public static final String COMMON_RARITY_KEYWORD = "Basura";
    public static final String UNCOMMON_RARITY_KEYWORD = "Común";
    public static final String RARE_RARITY_KEYWORD = "Raro";
    public static final String EPIC_RARITY_KEYWORD = "Épico";
    public static final String LEGENDARY_RARITY_KEYWORD = "Legendario";
    public static final String GODLIKE_RARITY_KEYWORD = "Divino";
    protected static final String ROLL_IDENTIFIER_KEY = "roll_count";
    protected static final String LEVEL_KEY = "magicobject_roll_lvl";
    private static final String IDENTIFIER_KEY = "is_identify_scrolled";
    private static final TextColor COMMON_COLOR = TextColor.color(0xC5C5C5);
    private static final TextColor UNCOMMON_COLOR = TextColor.color(0x31BD2C);
    private static final TextColor RARE_COLOR = TextColor.color(0x1147FF);
    private static final TextColor EPIC_COLOR = TextColor.color(0x922ADD);
    private static final TextColor LEGENDARY_COLOR =TextColor.color(0xFFDB00);
    private static final TextColor GODLIKE_COLOR = TextColor.color(0xFF181B);


    private static final DecimalFormat df = new DecimalFormat("0.#");
    private final List<Stat> addedStats;
    private final List<Integer> statValues;
    protected ModifierType MODIFIER_TYPE;
    protected StatProvider statProvider;
    protected RollQuality rollQuality;
    protected ItemRarity plugin = (ItemRarity) Bukkit.getPluginManager().getPlugin("ItemRarity");
    Component reset = Component.text().content("").color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false).build();
    private Component rarity;

    public IdentifiedItem(ItemStack item) {
        super(item);
        rollQuality = getRollQuality();
        this.addedStats = new ArrayList<>();
        this.statValues = new ArrayList<>();
        this.MODIFIER_TYPE = ItemUtil.getModifierType(item);
        if (!isIdentified(item)) {
            generateStats();
            applyStatsToItem();
            obtainRarity();
            setIdentifiedNBT();
            setLore();
        }
    }

    public static String getIdentifierKey() {
        return IDENTIFIER_KEY;
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

    private static double calculateTotalDamage(ItemStack item) {
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

    private static double calculateAttackSpeed(ItemStack item) {
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



    private int getItemLevel() {
        if (getRollQuality() instanceof LowRollQuality) {
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

    public void rerollStats() {
        emptyLore();
        removeModifiers();
        generateStats();
        applyStatsToItem();
        obtainRarity();
        setLore();
    }

    protected void removeSpecificModifier(Stat stat) {
        this.setItemMeta(AuraSkillsBukkit.get().getItemManager().removeStatModifier(this, MODIFIER_TYPE, stat).getItemMeta());
    }

    void addModifier(Stat stat, int value) {
        this.setItemMeta(AuraSkillsBukkit.get().getItemManager().addStatModifier(this, MODIFIER_TYPE, stat, value, true).getItemMeta());
    }

    private Stat getLowestModifier() {
        StatModifier lowestModifier = null;
        double lowestValue = 100;
        for (StatModifier modifier : getStatModifiers()) {
            double value = modifier.value();
            if (value < lowestValue) {
                lowestValue = value;
                lowestModifier = modifier;
            }
        }
        assert lowestModifier != null;
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
        assert highestModifier != null;
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
        assert highestModifier != null;
        return highestModifier.value();
    }

    public void rerollExceptHighestStat(Player player) {
        Stat highestStat = getHighestStat();
        int highestValue = (int) getHighestStatValue();
        removeAllModifierStats();
        generateStatsExceptHighestStat(highestStat);
        applyStatsToItem();
        addModifier(highestStat, highestValue);
        updateRarity();
        Component message = Component.text("El objeto cambió, se conservó: ")
                .color(RedemptionObject.getLoreColor()).append(Component.text(highestStat.getDisplayName(AuraSkillsApi.get().getMessageManager().getDefaultLanguage()))
                .color(TextColor.fromHexString(highestStat.getColor(AuraSkillsApi.get().getMessageManager().getDefaultLanguage()).replaceAll("[<>]", ""))));
        player.sendMessage(ItemRarity.getRedemptionPrefix().append(message));
    }

    protected void generateStatsExceptHighestStat(Stat excludedStat) {
        Random random = new Random();
        int statsCount = random.nextInt(2) + 4; // 4 o 5 estadísticas
        StatProvider statProvider = ItemUtil.getStatProvider(this);
        assert statProvider != null;
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

    public void rerollLowestStat(Player player) {
        Stat lowestModifier = getLowestModifier();
        removeSpecificStatLoreLine(lowestModifier);
        removeSpecificModifier(lowestModifier);
        int newValue = StatValueGenerator.generateValueForStat(getRollQuality(), statProvider.isThisStatGauss(lowestModifier));
        addModifier(lowestModifier, newValue);
        updateRarity();
        Component message = Component.text("Cambió la estadística: ", BlessingObject.getLoreColor())
                .append(Component.text(lowestModifier.getDisplayName(AuraSkillsApi.get().getMessageManager().getDefaultLanguage()))
                        .color(TextColor.fromHexString(lowestModifier.getColor(AuraSkillsApi.get().getMessageManager().getDefaultLanguage()).replaceAll("[<>]", ""))));
        player.sendMessage(ItemRarity.getBlessingPrefix().append(message));
    }

    protected void applyStatsToItem() {
        for (int i = this.addedStats.size() - 1; i >= 0; i--) {
            Stat stat = this.addedStats.get(i);
            double value = this.statValues.get(i);
            this.setItemMeta(AuraSkillsBukkit.get().getItemManager().addStatModifier(this, MODIFIER_TYPE, stat, value, true).getItemMeta());
        }
    }

    public void removeModifiers() {
        for (Stats stat : ItemRarity.STATS) {
            this.setItemMeta(AuraSkillsBukkit.get().getItemManager().removeStatModifier(this, MODIFIER_TYPE, stat).getItemMeta());
        }
    }

    protected void emptyLore() {
        ItemMeta meta = getItemMeta();
        List<String> emptylore = new ArrayList<>();
        assert meta != null;
        meta.setLore(emptylore);
        setItemMeta(meta);
    }

    public RollQuality getRollQuality() {
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

    private void updateRarity() {
        ItemMeta meta = getItemMeta();
        //List<String> lore = meta.getLore();
        @Nullable List<Component> lore = meta.lore();
        if (lore != null) {
            lore.removeIf(line -> line.toString().contains(COMMON_RARITY_KEYWORD)
                    || line.toString().contains(UNCOMMON_RARITY_KEYWORD)
                    || line.toString().contains(RARE_RARITY_KEYWORD)
                    || line.toString().contains(EPIC_RARITY_KEYWORD)
                    || line.toString().contains(LEGENDARY_RARITY_KEYWORD)
                    || line.toString().contains(GODLIKE_RARITY_KEYWORD));
            meta.lore(lore);
            setItemMeta(meta);
        }
        obtainRarity();
        setLore();
    }

    private void obtainRarity() {
        if (!getStatModifiers().isEmpty()) {
            double average = getStatModifiers().stream()
                    .mapToDouble(AuraSkillsModifier::value)
                    .average()
                    .orElse(0.0);

            switch (rollQuality.getClass().getSimpleName()) {
                case "GodRollQuality":
                    if (average >= 25.0) {
                        rarity = Component.text("[Divino]").color(GODLIKE_COLOR).decoration(TextDecoration.ITALIC, false);
                    } else if (average >= 23.0) {
                        rarity = Component.text("[Legendario]").color(LEGENDARY_COLOR).decoration(TextDecoration.ITALIC, false);
                    } else if (average >= 20.5) {
                        rarity = Component.text("[Épico]").color(EPIC_COLOR).decoration(TextDecoration.ITALIC, false);
                    } else if (average >= 17.5) {
                        rarity = Component.text("[Raro]").color(RARE_COLOR).decoration(TextDecoration.ITALIC, false);
                    } else if (average >= 14.0) {
                        rarity = Component.text("[Común]").color(UNCOMMON_COLOR).decoration(TextDecoration.ITALIC, false);
                    } else {
                        rarity = Component.text("[Basura]").color(COMMON_COLOR).decoration(TextDecoration.ITALIC, false);
                    }
                    break;

                case "HighRollQuality":
                    if (average >= 23.5) {
                        rarity = Component.text("[Divino]").color(GODLIKE_COLOR).decoration(TextDecoration.ITALIC, false);
                    } else if (average >= 21.5) {
                        rarity = Component.text("[Legendario]").color(LEGENDARY_COLOR).decoration(TextDecoration.ITALIC, false);
                    } else if (average >= 19) {
                        rarity = Component.text("[Épico]").color(EPIC_COLOR).decoration(TextDecoration.ITALIC, false);
                    } else if (average >= 16) {
                        rarity = Component.text("[Raro]").color(RARE_COLOR).decoration(TextDecoration.ITALIC, false);
                    } else if (average >= 12.5) {
                        rarity = Component.text("[Común]").color(UNCOMMON_COLOR).decoration(TextDecoration.ITALIC, false);
                    } else {
                        rarity = Component.text("[Basura]").color(COMMON_COLOR).decoration(TextDecoration.ITALIC, false);
                    }
                    break;

                case "MediumRollQuality":
                    if (average >= 22.0) {
                        rarity = Component.text("[Divino]").color(GODLIKE_COLOR).decoration(TextDecoration.ITALIC, false);
                    }else if (average >= 20.0) {
                        rarity = Component.text("[Legendario]").color(LEGENDARY_COLOR).decoration(TextDecoration.ITALIC, false);
                    } else if (average >= 17.5) {
                        rarity = Component.text("[Épico]").color(EPIC_COLOR).decoration(TextDecoration.ITALIC, false);
                    } else if (average >= 14.5) {
                        rarity = Component.text("[Raro]").color(RARE_COLOR).decoration(TextDecoration.ITALIC, false);
                    } else if (average >= 11.0) {
                        rarity = Component.text("[Común]").color(UNCOMMON_COLOR).decoration(TextDecoration.ITALIC, false);
                    } else {
                        rarity = Component.text("[Basura]").color(COMMON_COLOR).decoration(TextDecoration.ITALIC, false);
                    }
                    break;

                default: // LowRollQuality
                    if (average >= 19.0) {
                        rarity = Component.text("[Divino]").color(GODLIKE_COLOR).decoration(TextDecoration.ITALIC, false);
                    }else if (average >= 17.0) {
                        rarity = Component.text("[Legendario]").color(LEGENDARY_COLOR).decoration(TextDecoration.ITALIC, false);
                    } else if (average >= 14.5) {
                        rarity = Component.text("[Épico]").color(EPIC_COLOR).decoration(TextDecoration.ITALIC, false);
                    } else if (average >= 11.5) {
                        rarity = Component.text("[Raro]").color(RARE_COLOR).decoration(TextDecoration.ITALIC, false);
                    } else if (average >= 8.0) {
                        rarity = Component.text("[Común]").color(UNCOMMON_COLOR).decoration(TextDecoration.ITALIC, false);
                    } else {
                        rarity = Component.text("[Basura]").color(COMMON_COLOR).decoration(TextDecoration.ITALIC, false);
                    }
                    break;
            }
        }
    }

    public TextColor getRarityColor() {
        if (rarity.toString().contains(GODLIKE_RARITY_KEYWORD)) {
            return GODLIKE_COLOR;
        } else if (rarity.toString().contains(LEGENDARY_RARITY_KEYWORD)) {
            return LEGENDARY_COLOR;
        } else if (rarity.toString().contains(EPIC_RARITY_KEYWORD)) {
            return EPIC_COLOR;
        } else if (rarity.toString().contains(RARE_RARITY_KEYWORD)) {
            return RARE_COLOR;
        } else if (rarity.toString().contains(UNCOMMON_RARITY_KEYWORD)) {
            return UNCOMMON_COLOR;
        } else {
            return COMMON_COLOR;
        }
    }
    public Component getRarityKeyword() {
        if (rarity.toString().contains(GODLIKE_RARITY_KEYWORD)) {
            return Component.text(GODLIKE_RARITY_KEYWORD);
        } else if (rarity.toString().contains(LEGENDARY_RARITY_KEYWORD)) {
            return Component.text(LEGENDARY_RARITY_KEYWORD);
        } else if (rarity.toString().contains(EPIC_RARITY_KEYWORD)) {
            return Component.text(EPIC_RARITY_KEYWORD);
        } else if (rarity.toString().contains(RARE_RARITY_KEYWORD)) {
            return Component.text(RARE_RARITY_KEYWORD);
        } else if (rarity.toString().contains(UNCOMMON_RARITY_KEYWORD)) {
            return Component.text(UNCOMMON_RARITY_KEYWORD);
        } else {
            return Component.text(COMMON_RARITY_KEYWORD);
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
        ItemMeta meta = getItemMeta();
        @Nullable List<Component> lore;
        if (meta.lore() != null) {
            lore = meta.lore();
        } else {
            lore = new ArrayList<>();
        }

        // Encontrar el índice después de la última línea que comienza con "+"
        int lastStatIndex = -1;
        if (lore != null) {
            for (int i = 0; i < lore.size(); i++) {
                String line = PlainTextComponentSerializer.plainText().serialize(lore.get(i));
                if (line.trim().startsWith("+")) {
                    lastStatIndex = i;
                }
            }
        }

        Component ilvl = Component.text(" ● Nvl " + getItemLevel() + " ●")
                .color(NamedTextColor.DARK_GRAY)
                .decorate(TextDecoration.ITALIC);
        Component rarityLine = rarity.append(ilvl);

        // Insertar en la posición correcta
        int insertIndex = lastStatIndex != -1 ? lastStatIndex + 1 : 0;
        assert lore != null;
        if (insertIndex < lore.size()) {
            lore.add(insertIndex, rarityLine);
        } else {
            lore.add(rarityLine);
        }
        meta.lore(lore);

        // Resto del código para manejar nombres y atributos
        if (NexoItems.idFromItem(this) != null && ItemUtil.getItemType(this).equals("Weapon")) {
            String plainText = PlainTextComponentSerializer.plainText().serialize(meta.itemName());
            Component component = Component.text(plainText, getRarityColor()).decoration(TextDecoration.ITALIC, false);
            meta.customName(component);
            setItemMeta(meta);
            attributesDisplayInLore(this);
        } else if (NexoItems.idFromItem(this) != null && ItemUtil.getItemType(this).equals("Armor")) {
            String plainText = PlainTextComponentSerializer.plainText().serialize(meta.itemName());
            Component component = Component.text(plainText, getRarityColor()).decoration(TextDecoration.ITALIC, false);
            meta.customName(component);
            setItemMeta(meta);
        } else if (NexoItems.idFromItem(this) == null && !ItemUtil.getItemType(this).equals("Armor")) {
            if (!meta.hasCustomName()) {
                String itemTranslationKey = this.translationKey();
                TranslatableComponent translatedName = Component.translatable(itemTranslationKey).color(getRarityColor());
                Component newName = reset.append(translatedName);
                meta.itemName(newName);
            } else {
                Component component = meta.customName();
                assert component != null;
                component = component.color(getRarityColor()).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
                meta.displayName(component);
            }
            setItemMeta(meta);
            attributesDisplayInLore(this);
        } else if (NexoItems.idFromItem(this) == null && ItemUtil.getItemType(this).equals("Armor")) {
            if (!meta.hasCustomName()) {
                String itemTranslationKey = this.translationKey();
                TranslatableComponent translatedName = Component.translatable(itemTranslationKey).color(getRarityColor());
                Component newName = reset.append(translatedName);
                meta.itemName(newName);
            } else {
                Component component = meta.customName();
                assert component != null;
                component = component.color(getRarityColor()).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
                meta.displayName(component);
            }
            setItemMeta(meta);
        }
        updateLoreWithSockets();
    }

    private List<StatModifier> getStatModifiers() {
        return AuraSkillsBukkit.get().getItemManager().getStatModifiers(this, MODIFIER_TYPE);
    }

    public List<Integer> getStatValues() {
        return statValues;
    }

    public List<Stat> getAddedStats() {
        return addedStats;
    }
    public abstract void updateLoreWithSockets();

}
