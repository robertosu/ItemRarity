package cl.nightcore.itemrarity.config;

import cl.nightcore.itemrarity.customstats.CustomStats;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.Stats;
import dev.aurelium.auraskills.api.trait.Trait;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public enum CombinedStats implements Stat {
    // Original Stats
    STRENGTH(Stats.STRENGTH),
    HEALTH(Stats.HEALTH),
    //(Stats.REGENERATION),
    LUCK(Stats.LUCK),
    WISDOM(Stats.WISDOM),
    TOUGHNESS(Stats.TOUGHNESS),
    CRIT_CHANCE(Stats.CRIT_CHANCE),
    CRIT_DAMAGE(Stats.CRIT_DAMAGE),

    // Custom Stats
    DEXTERITY(CustomStats.DEXTERITY),
    EVASION(CustomStats.EVASION),
    ACCURACY(CustomStats.ACCURACY);

    private final Stat delegateStat;

    CombinedStats(Stat delegateStat) {
        this.delegateStat = delegateStat;
    }

    public Stat getDelegateStat() {
        return delegateStat;
    }

    @Override
    public NamespacedId getId() {
        return delegateStat.getId();
    }

    @Override
    public boolean isEnabled() {
        return delegateStat.isEnabled();
    }

    @Override
    public List<Trait> getTraits() {
        return delegateStat.getTraits();
    }

    @Override
    public double getTraitModifier(Trait trait) {
        return delegateStat.getTraitModifier(trait);
    }

    @Override
    public String getDisplayName(Locale locale) {
        return delegateStat.getDisplayName(locale);
    }

    @Override
    public String getDisplayName(Locale locale, boolean formatted) {
        return delegateStat.getDisplayName(locale, formatted);
    }

    @Override
    public String getDescription(Locale locale) {
        return delegateStat.getDescription(locale);
    }

    @Override
    public String getDescription(Locale locale, boolean formatted) {
        return delegateStat.getDescription(locale, formatted);
    }

    @Override
    public String getColor(Locale locale) {
        return delegateStat.getColor(locale);
    }

    @Override
    public String getColoredName(Locale locale) {
        return delegateStat.getColoredName(locale);
    }

    @Override
    public String getSymbol(Locale locale) {
        return delegateStat.getSymbol(locale);
    }

    @Override
    public boolean optionBoolean(String key) {
        return delegateStat.optionBoolean(key);
    }

    @Override
    public boolean optionBoolean(String key, boolean def) {
        return delegateStat.optionBoolean(key, def);
    }

    @Override
    public int optionInt(String key) {
        return delegateStat.optionInt(key);
    }

    @Override
    public int optionInt(String key, int def) {
        return delegateStat.optionInt(key, def);
    }

    @Override
    public double optionDouble(String key) {
        return delegateStat.optionDouble(key);
    }

    @Override
    public double optionDouble(String key, double def) {
        return delegateStat.optionDouble(key, def);
    }

    @Override
    public String optionString(String key) {
        return delegateStat.optionString(key);
    }

    @Override
    public String optionString(String key, String def) {
        return delegateStat.optionString(key, def);
    }

    @Override
    public List<String> optionStringList(String key) {
        return delegateStat.optionStringList(key);
    }

    @Override
    public Map<String, Object> optionMap(String key) {
        return delegateStat.optionMap(key);
    }
}