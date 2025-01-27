package cl.nightcore.itemrarity.customstats;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.AuraSkillsBukkit;
import dev.aurelium.auraskills.api.bukkit.BukkitTraitHandler;
import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.user.SkillsUser;
import dev.aurelium.auraskills.api.util.NumberUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class DodgeChanceTrait implements BukkitTraitHandler, Listener {

    private static final NamespacedKey levelKey = new NamespacedKey("mythicprojectiles","level");

    private final AuraSkillsApi auraSkills;

    public DodgeChanceTrait(AuraSkillsApi auraSkills) {
        this.auraSkills = auraSkills;
    }

    @Override
    public Trait[] getTraits() {
        return new Trait[] {CustomTraits.DODGE_CHANCE, CustomTraits.HIT_CHANCE};
    }

    @Override
    public double getBaseLevel(Player player, Trait trait) {
        return 0;
    }

    @Override
    public void onReload(Player player, SkillsUser user, Trait trait) {
    }

    public int getLevel(Entity entity) {
        return entity.getPersistentDataContainer().getOrDefault(levelKey, PersistentDataType.INTEGER, 0);
    }

    @Override
    public String getMenuDisplay(double value, Trait trait, Locale locale) {
        if (CustomTraits.DODGE_CHANCE.optionBoolean("use_percent")) {
            return NumberUtil.format1(value) + "%";
        } else {
            return "+" + NumberUtil.format1(value);
        }
    }

    private double getMobStats(LivingEntity entity) {
        int level = getLevel(entity);
        return level == 0 ? 0 : (double) level / 2;
    }

    @EventHandler(ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!AuraSkillsBukkit.get().getLocationManager().isPluginDisabled(event.getEntity().getLocation(),null)) {
            Entity defender = event.getEntity();
            Entity damager = event.getDamager();

            if(defender == damager){
                return;
            }
            if(damager instanceof Projectile projectile){
                if (projectile.getShooter() == defender){
                    return;
                }
            }


            // Variables para almacenar los valores de dodge y accuracy
            double defenderDodge = 0;
            double attackerAccuracy = 0;
            // Procesar estadísticas del defensor
            if (defender instanceof Player defenderPlayer) {
                SkillsUser defenderUser = auraSkills.getUser(defenderPlayer.getUniqueId());
                defenderDodge = defenderUser.getEffectiveTraitLevel(CustomTraits.DODGE_CHANCE) / 100.0;
            } else if (defender instanceof LivingEntity defenderMob) {
                defenderDodge = getMobStats(defenderMob) / 100.0;
            }
            // Procesar estadísticas del atacante y obtener el Player si es un jugador
            Player attackerPlayer = null;
            switch (damager) {
                case Projectile projectile -> {
                    ProjectileSource source = projectile.getShooter();
                    if (source instanceof Player) {
                        attackerPlayer = (Player) source;
                    } else if (source instanceof LivingEntity attackerMob) {
                        attackerAccuracy = getMobStats(attackerMob) / 100.0;
                    }
                }
                case Player player -> attackerPlayer = player;
                case LivingEntity attackerMob -> attackerAccuracy = getMobStats(attackerMob) / 100.0;
                default -> {
                }
            }
            // Si el atacante es un jugador, usar sus stats de AuraSkills
            if (attackerPlayer != null) {
                SkillsUser attackerUser = auraSkills.getUser(attackerPlayer.getUniqueId());
                attackerAccuracy = attackerUser.getEffectiveTraitLevel(CustomTraits.HIT_CHANCE) / 100.0;
            }

            // Calcular probabilidad final de esquiva
            double finalDodgeChance = calculateFinalDodgeChance(defenderDodge, attackerAccuracy);

            if (ThreadLocalRandom.current().nextDouble() < finalDodgeChance) {
                event.setCancelled(true);
                // Mostrar partículas siempre que haya una esquiva
                defender.getWorld().spawnParticle(Particle.SMALL_GUST, defender.getLocation(), 10, 0.5, 0.5, 0.5, 0.1);
                defender.getWorld().playSound(defender.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0F, 2.0F);

                if (defender instanceof Player defenderPlayer) {
                    // Si el defensor es un jugador, mostrar efectos completos
                    //defenderPlayer.getWorld().playSound(defenderPlayer.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0F, 2.0F);
                    defenderPlayer.sendActionBar(Component.text("¡Esquivaste el ataque!").color(NamedTextColor.GREEN));
                }

                // Si hay un jugador atacante, enviarle mensaje de fallo sin importar si el defensor es mob o jugador
                if (attackerPlayer != null) {

                    attackerPlayer.sendActionBar(Component.text("¡Fallaste el ataque!", NamedTextColor.RED));
                }
            }
        }
    }

    private double calculateFinalDodgeChance(double dodgeProbability, double accuracyProbability) {
        //return Math.max(0, dodgeProbability * (1 - accuracyProbability));
        return Math.max(0, dodgeProbability - accuracyProbability);
    }
}