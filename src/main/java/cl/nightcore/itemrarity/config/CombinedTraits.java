package cl.nightcore.itemrarity.config;

import cl.nightcore.itemrarity.customstats.CustomTraits;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.trait.Traits;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public enum CombinedTraits implements Trait {
    // Original Traits
    ATTACK_DAMAGE(Traits.ATTACK_DAMAGE),
    HP(Traits.HP),
    SATURATION_REGEN(Traits.SATURATION_REGEN),
    HUNGER_REGEN(Traits.HUNGER_REGEN),
    MANA_REGEN(Traits.MANA_REGEN),
    LUCK(Traits.LUCK),
    FARMING_LUCK(Traits.FARMING_LUCK),
    FORAGING_LUCK(Traits.FORAGING_LUCK),
    MINING_LUCK(Traits.MINING_LUCK),
    FISHING_LUCK(Traits.FISHING_LUCK),
    EXCAVATION_LUCK(Traits.EXCAVATION_LUCK),
    DOUBLE_DROP(Traits.DOUBLE_DROP),
    EXPERIENCE_BONUS(Traits.EXPERIENCE_BONUS),
    ANVIL_DISCOUNT(Traits.ANVIL_DISCOUNT),
    MAX_MANA(Traits.MAX_MANA),
    DAMAGE_REDUCTION(Traits.DAMAGE_REDUCTION),
    CRIT_CHANCE(Traits.CRIT_CHANCE),
    CRIT_DAMAGE(Traits.CRIT_DAMAGE),
    MOVEMENT_SPEED(Traits.MOVEMENT_SPEED),

    // Custom Traits
    DODGE_CHANCE(CustomTraits.DODGE_CHANCE),
    HIT_CHANCE(CustomTraits.HIT_CHANCE),
    ATTACK_SPEED(CustomTraits.ATTACK_SPEED);

    private final Trait delegateTrait;

    CombinedTraits(Trait delegateTrait) {
        this.delegateTrait = delegateTrait;
    }


    public Trait getDelegateTrait() {
        return delegateTrait;
    }

    @Override
    public NamespacedId getId() {
        return delegateTrait.getId();
    }

    @Override
    public boolean isEnabled() {
        return delegateTrait.isEnabled();
    }

    @Override
    public String getDisplayName(Locale locale) {
        return delegateTrait.getDisplayName(locale);
    }

    @Override
    public String getDisplayName(Locale locale, boolean formatted) {
        return delegateTrait.getDisplayName(locale, formatted);
    }

    @Override
    public String getMenuDisplay(double value, Locale locale) {
        return delegateTrait.getMenuDisplay(value, locale);
    }

    @Override
    public boolean optionBoolean(String key) {
        return delegateTrait.optionBoolean(key);
    }

    @Override
    public boolean optionBoolean(String key, boolean def) {
        return delegateTrait.optionBoolean(key, def);
    }

    @Override
    public int optionInt(String key) {
        return delegateTrait.optionInt(key);
    }

    @Override
    public int optionInt(String key, int def) {
        return delegateTrait.optionInt(key, def);
    }

    @Override
    public double optionDouble(String key) {
        return delegateTrait.optionDouble(key);
    }

    @Override
    public double optionDouble(String key, double def) {
        return delegateTrait.optionDouble(key, def);
    }

    @Override
    public String optionString(String key) {
        return delegateTrait.optionString(key);
    }

    @Override
    public String optionString(String key, String def) {
        return delegateTrait.optionString(key, def);
    }

    @Override
    public List<String> optionStringList(String key) {
        return delegateTrait.optionStringList(key);
    }

    @Override
    public Map<String, Object> optionMap(String key) {
        return delegateTrait.optionMap(key);
    }

    @Override
    public String toString() {
        return delegateTrait.toString();
    }
}