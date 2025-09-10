package cl.nightcore.itemrarity.abstracted;


import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.Map;

public class EnchantmentDescriptionManager {

    private static final Map<Enchantment, String> ENCHANTMENT_DESCRIPTIONS = new HashMap<>();

    static {
        initializeDescriptions();
    }

    private static void initializeDescriptions() {
        // Encantamientos de combate
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.SHARPNESS, "Aumenta el daño contra todos los objetivos");
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.SMITE, "Aumenta el daño contra objetivos no-muertos");
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.BANE_OF_ARTHROPODS, "Aumenta el daño contra objetivos artrópodos");
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.KNOCKBACK, "Todo el daño a otros objetivos los empujará hacia atrás al ser golpeados");
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.FIRE_ASPECT, "Al atacar un objetivo, tiene una probabilidad de prenderle fuego");
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.LOOTING, "Proporciona una probabilidad de obtener botín extra al matar monstruos");
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.SWEEPING_EDGE, "Aumenta el daño contra objetivos al usar un ataque de barrido");

        // Encantamientos de protección
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.PROTECTION, "Proporciona protección contra daño ambiental");
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.FIRE_PROTECTION, "Proporciona protección contra daño por fuego");
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.BLAST_PROTECTION, "Proporciona protección contra daño explosivo");
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.PROJECTILE_PROTECTION, "Proporciona protección contra daño por proyectiles");
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.THORNS, "Daña al atacante");
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.RESPIRATION, "Disminuye la velocidad de pérdida de aire bajo el agua");
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.AQUA_AFFINITY, "Aumenta la velocidad a la que un jugador puede minar bajo el agua");
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.DEPTH_STRIDER, "Aumenta la velocidad de caminata mientras está en el agua");
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.FROST_WALKER, "Congela cualquier agua quieta adyacente al hielo/escarcha sobre la que camina el jugador");
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.FEATHER_FALLING, "Proporciona protección contra daño por caída");

        // Encantamientos de herramientas
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.EFFICIENCY, "Aumenta la velocidad a la que minas/cavas");
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.SILK_TOUCH, "Permite que los bloques se dropeen a sí mismos en lugar de fragmentos");
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.FORTUNE, "Proporciona una probabilidad de obtener botín extra al destruir bloques");

        // Encantamientos de arco
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.POWER, "Proporciona daño extra al disparar flechas desde arcos");
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.PUNCH, "Proporciona un empujón cuando una entidad es golpeada por una flecha de un arco");
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.FLAME, "Prende fuego a las entidades cuando son golpeadas por flechas disparadas desde un arco");
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.INFINITY, "Proporciona flechas infinitas al disparar un arco");

        // Encantamientos de pesca
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.LUCK_OF_THE_SEA, "Disminuye las probabilidades de pescar basura inútil");
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.LURE, "Aumenta la velocidad de mordida de los peces en tu anzuelo");

        // Encantamientos generales
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.UNBREAKING, "Disminuye la velocidad a la que una herramienta pierde durabilidad");
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.MENDING, "Permite reparar el objeto usando orbes de experiencia");
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.BINDING_CURSE, "El objeto no puede ser removido");
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.VANISHING_CURSE, "El objeto desaparece en lugar de caerse");

        // Encantamientos de tridente
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.LOYALTY, "Hace que un tridente lanzado regrese al jugador que lo lanzó");
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.IMPALING, "Causa más daño a mobs que viven en el océano");
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.RIPTIDE, "Cuando llueve, lanza al jugador en la dirección en que se lanza su tridente");
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.CHANNELING, "Golpea con un rayo cuando un mob es golpeado con un tridente si las condiciones son tormentosas");

        // Encantamientos de ballesta
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.MULTISHOT, "Dispara múltiples flechas desde ballestas");
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.QUICK_CHARGE, "Carga ballestas rápidamente");
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.PIERCING, "Los proyectiles de ballesta atraviesan entidades");

        // Encantamientos especiales
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.SOUL_SPEED, "Camina más rápido sobre bloques de alma");
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.SWIFT_SNEAK, "Camina más rápido mientras te agachas");

        // Encantamientos de maza (nuevos)
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.BREACH, "Reduce la efectividad de la armadura contra mazas");
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.DENSITY, "Aumenta el daño por caída de las mazas");
        ENCHANTMENT_DESCRIPTIONS.put(Enchantment.WIND_BURST, "Emite ráfaga de viento al golpear enemigo");
    }

    public static Component getVanillaEnchantDescription(Enchantment enchantment) {
        String description = ENCHANTMENT_DESCRIPTIONS.get(enchantment);

        if (description == null) {
            return Component.text("Descripción no disponible");
        }

        return Component.text(description);
    }

}

// Ejemplo de uso:
/*
Component description = getVanillaEnchantDescription(Enchantment.SHARPNESS);
// Resultado: "Aumenta el daño contra todos los objetivos"

Component protection = getVanillaEnchantDescription(Enchantment.PROTECTION);
// Resultado: "Proporciona protección contra daño ambiental"
*/