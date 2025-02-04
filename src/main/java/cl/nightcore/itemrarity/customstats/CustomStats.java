package cl.nightcore.itemrarity.customstats;

import dev.aurelium.auraskills.api.item.ItemContext;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.stat.CustomStat;
import dev.aurelium.auraskills.api.trait.Traits;

public class CustomStats {

    public static final CustomStat DEXTERITY = CustomStat.builder(NamespacedId.of("itemrarity", "dexterity"))
            .trait(CustomTraits.ATTACK_SPEED,0.5)
            .trait(CustomTraits.HIT_CHANCE, 0.5) // Dodge chance will increase by 0.5 per dexterity level
            .displayName("Destreza")
            .description("La destreza aumenta tu velocidad de ataque y tu probabilidad de acertar.")
            .color("<#53C259>")
            .symbol("⛹")
            .item(ItemContext.builder()
                    .material("paper").set("custom_model_data",7008)
                    .group("lower_right") // A group defined in AuraSkills/menus/stats.yml
                    .order(1) // The position within that group
                    .build())
            .build();

    public static final CustomStat EVASION = CustomStat.builder(NamespacedId.of("itemrarity", "evasion"))
            .trait(CustomTraits.DODGE_CHANCE, 0.5) // Dodge chance will increase by 0.5 per dexterity level
            .trait(Traits.MOVEMENT_SPEED, 1)
            .displayName("Evasión")
            .description("La evasión aumenta la probabilidad de esquivar y tu velocidad de movimiento.")
            .color("<#F0F0F0>")
            .symbol("◎")
            .item(ItemContext.builder()
                    .material("paper").set("custom_model_data",7009)
                    .group("lower_right") // A group defined in AuraSkills/menus/stats.yml
                    .order(2) // The position within that group
                    .build())
            .build();
}