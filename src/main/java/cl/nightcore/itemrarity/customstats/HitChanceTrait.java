package cl.nightcore.itemrarity.customstats;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.bukkit.BukkitTraitHandler;
import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.user.SkillsUser;
import dev.aurelium.auraskills.api.util.NumberUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.Locale;

public class HitChanceTrait implements BukkitTraitHandler, Listener {

    private final AuraSkillsApi auraSkills;

    // Inject API dependency in constructor
    public HitChanceTrait(AuraSkillsApi auraSkills) {
        this.auraSkills = auraSkills;
    }

    @Override
    public Trait[] getTraits() {
        // An array containing your CustomTrait instance
        return new Trait[] {CustomTraits.HIT_CHANCE};
    }

    @Override
    public double getBaseLevel(Player player, Trait trait) {
        // The base value of your trait when its stat is at level 0, could be a
        // Minecraft default value or values from other plugins
        return 0;
    }

    @Override
    public void onReload(Player player, SkillsUser user, Trait trait) {
        // Method called when the value of the trait's parent stat changes
    }

    @Override
    public String getMenuDisplay(double value, Trait trait, Locale locale) {
        if (CustomTraits.HIT_CHANCE.optionBoolean("use_percent")) {
            return "+" + NumberUtil.format1(value) + "%";
        } else {
            return "+" + NumberUtil.format1(value);
        }
    }
}