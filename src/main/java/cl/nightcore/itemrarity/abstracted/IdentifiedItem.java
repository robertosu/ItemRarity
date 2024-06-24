package cl.nightcore.itemrarity.abstracted;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.classes.*;
import de.tr7zw.nbtapi.NBTItem;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.AuraSkillsBukkit;
import dev.aurelium.auraskills.api.item.ModifierType;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.StatModifier;
import dev.aurelium.auraskills.api.stat.Stats;
import dev.aurelium.auraskills.api.util.AuraSkillsModifier;
import io.th0rgal.oraxen.api.OraxenItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextFormat;
import net.kyori.adventure.text.minimessage.tag.ParserDirective;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.Reset;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;


import java.util.*;

import static cl.nightcore.itemrarity.ItemRarity.*;

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
    protected static List<Stat> GaussStats;
    private final List<Stat> addedStats;
    private final List<Integer> statValues;
    protected ModifierType MODIFIER_TYPE;
    private String rarity;
    protected RollQuality rollQuality;
    Component reset = Component.text()
            .content("")
            .color(NamedTextColor.WHITE)
            .decoration(TextDecoration.ITALIC, false)
            .build();

    public IdentifiedItem(ItemStack item) {
        super(item);
        if (getItemType(item).equals("Weapon")){
            GaussStats =  Arrays.asList(Stats.STRENGTH,Stats.CRIT_CHANCE);
        } else if (getItemType(item).equals("Armor")) {
            GaussStats = Arrays.asList(Stats.HEALTH,Stats.TOUGHNESS);
        }
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
    public static boolean isThisStatGauss(Stat stat){
        return GaussStats.contains(stat);
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
        for (Stat stat : GaussStats) {
            if (stat != excludedStat) {
                getAddedStats().add(stat);
                int value = StatValueGenerator.generateValueForStat(getRollQuality(), isThisStatGauss(stat));
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
            int value = StatValueGenerator.generateValueForStat(getRollQuality(), isThisStatGauss(stat));
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
        int newValue = StatValueGenerator.generateValueForStat(getRollQuality(), isThisStatGauss(lowestModifier));
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
            double average = getStatModifiers().stream()
                    .mapToDouble(AuraSkillsModifier::value)
                    .average()
                    .orElse(0.0);  // Devuelve 0.0 si la lista está vacía
            int level = getLevel();

            if (rollQuality instanceof GodRollQuality) {
                if (average >= 26) {
                    rarity = ChatColor.GOLD + "" + ChatColor.ITALIC + " [Legendario]";
                } else if (average >= 24) {
                    rarity = ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + " [Mítico]";
                } else if (average >= 18) {
                    rarity = ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + " [Raro]";
                } else {
                    rarity = ChatColor.DARK_AQUA + "" + ChatColor.ITALIC + " [Común]";
                }
            } else if (rollQuality instanceof  HighRollQuality) {
                if (average >= 22) {
                    rarity = ChatColor.GOLD + "" + ChatColor.ITALIC + " [Legendario]";
                } else if (average >= 18) {
                    rarity = ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + " [Mítico]";
                } else if (average >= 16) {
                    rarity = ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + " [Raro]";
                } else {
                    rarity = ChatColor.DARK_AQUA + "" + ChatColor.ITALIC + " [Común]";
                }
            } else if (rollQuality instanceof MediumRollQuality) {
                if (average >= 20) {
                    rarity = ChatColor.GOLD + "" + ChatColor.ITALIC + " [Legendario]";
                } else if (average >= 16) {
                    rarity = ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + " [Mítico]";
                } else if (average >= 14) {
                    rarity = ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + " [Raro]";
                } else {
                    rarity = ChatColor.DARK_AQUA + "" + ChatColor.ITALIC + " [Común]";
                }
            } else {
                if (average >= 16) {
                    rarity = ChatColor.GOLD + "" + ChatColor.ITALIC + " [Legendario]";
                } else if (average >= 14) {
                    rarity = ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + " [Mítico]";
                } else if (average >= 12) {
                    rarity = ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + " [Raro]";
                } else {
                    rarity = ChatColor.DARK_AQUA + "" + ChatColor.ITALIC + " [Común]";
                }
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
    public int getLevel(){
        NBTItem nbtItem = new NBTItem(this);
        return nbtItem.getInteger(LEVEL_KEY);
    }
    void setIdentifiedNBT() {
        NBTItem nbtItem = new NBTItem(this);
        nbtItem.setBoolean(IDENTIFIER_KEY, true);
        nbtItem.mergeNBT(this);
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


        if (OraxenItems.getIdByItem(this)!=null) {
            Component plainName = Component.text(ChatColor.stripColor(meta.getDisplayName()));
            Component newName = reset.append(plainName);
            meta.displayName(newName.color(getRarityColor()));
        }else{
            // Obtener la key de traducción
            String itemTypeKey = this.getTranslationKey();
            TranslatableComponent translatedName = Component.translatable(itemTypeKey).color(getRarityColor());
            Component newName = reset.append(translatedName);
            meta.displayName(newName);
        }
        setItemMeta(meta);
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

}
