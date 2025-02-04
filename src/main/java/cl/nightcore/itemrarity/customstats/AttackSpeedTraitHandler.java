package cl.nightcore.itemrarity.customstats;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.bukkit.BukkitTraitHandler;
import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.user.SkillsUser;
import dev.aurelium.auraskills.api.util.NumberUtil;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Locale;
import java.util.UUID;

public class AttackSpeedTraitHandler implements BukkitTraitHandler, Listener {

    private final AuraSkillsApi auraSkills;
    private static final UUID ATTRIBUTE_ID = UUID.fromString("7d1423dd-91db-467a-8eb8-1886e30ca0b2");

    public AttackSpeedTraitHandler(AuraSkillsApi auraSkills) {
        this.auraSkills = auraSkills;
    }

    @Override
    public Trait[] getTraits() {
        return new Trait[] {CustomTraits.ATTACK_SPEED};
    }

    @Override
    public double getBaseLevel(Player player, Trait trait) {
        AttributeInstance attribute = player.getAttribute(org.bukkit.attribute.Attribute.ATTACK_SPEED);
        if (attribute == null) {
            return 0.0;
        }
        // Obtener el valor total del atributo
        double baseValue = attribute.getValue();
        // Restar los modificadores existentes para obtener el valor base
        for (AttributeModifier modifier : attribute.getModifiers()) {
            if (isSkillsModifier(modifier)) { // Verificar si el modificador es del trait
                baseValue -= modifier.getAmount();
            }
        }
        return baseValue;
    }

    private boolean isSkillsModifier(AttributeModifier modifier) {
        // Verificar si el modificador es del trait (por ejemplo, por nombre o UUID)
        return modifier.getName().equals("attackSpeedTrait") || modifier.getUniqueId().equals(ATTRIBUTE_ID);
    }

    @Override
    public void onReload(Player player, SkillsUser user, Trait trait) {
        applyAttackSpeed(player, user);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        applyAttackSpeed(event.getPlayer(), auraSkills.getUser(event.getPlayer().getUniqueId()));
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        applyAttackSpeed(event.getPlayer(), auraSkills.getUser(event.getPlayer().getUniqueId()));
    }

    private void applyAttackSpeed(Player player, SkillsUser user) {
        Trait trait = CustomTraits.ATTACK_SPEED;
        AttributeInstance attribute = player.getAttribute(org.bukkit.attribute.Attribute.ATTACK_SPEED);
        if (attribute == null) return;

        double modifier = user.getBonusTraitLevel(trait);
        double baseValue = attribute.getBaseValue();

        // Remove existing modifiers
        for (AttributeModifier am : attribute.getModifiers()) {
            if (am.getUniqueId().equals(ATTRIBUTE_ID)) {
                //System.out.println("[DEBUG] Removing existing modifier: " + am.getAmount()); // Debug
                attribute.removeModifier(am);
            }
        }

        // Apply new modifier only if it's not zero
        if (modifier != 0) {
            attribute.addModifier(new AttributeModifier(ATTRIBUTE_ID, "attackSpeedTrait", modifier, Operation.ADD_NUMBER));
            //System.out.println("[DEBUG] Added new modifier: " + modifier); // Debug
        }

        // Debug: Mostrar el valor final del atributo
        //System.out.println("[DEBUG] Final Attack Speed: " + attribute.getValue());
    }

    @Override
    public String getMenuDisplay(double value, Trait trait, Locale locale) {
        return /*"+" + */NumberUtil.format1(value);
    }
}