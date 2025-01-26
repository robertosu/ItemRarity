package cl.nightcore.itemrarity.customstats;

import dev.aurelium.auraskills.api.item.ItemContext;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.stat.CustomStat;
import dev.aurelium.auraskills.api.trait.Traits;

public class CustomStats {

    public static final CustomStat DEXTERITY = CustomStat
            .builder(NamespacedId.of("itemrarity", "dexterity"))
                    .trait(CustomTraits.DODGE_CHANCE, 0.5) // Dodge chance will increase by 0.5 per dexterity level
                    .trait(Traits.MOVEMENT_SPEED,1)
                    .displayName("Destreza")
                    .description("La destreza aumenta la probabilidad de esquivar un ataque y tu velocidad de movimiento.")
                    .color("<#F0F0F0>")
                    .symbol("⛹")
                    .item(ItemContext.builder()
                            .material("white_stained_glass_pane")
                            .group("lower_right") // A group defined in AuraSkills/menus/stats.yml
                            .order(1) // The position within that group
                            .build())
                    .build();

    public static final CustomStat ACCURACY = CustomStat
            .builder(NamespacedId.of("itemrarity", "accuracy"))
            .trait(CustomTraits.HIT_CHANCE, 0.5) // Dodge chance will increase by 0.5 per dexterity level
            .displayName("Precisión")
            .description("Aumenta la probabilidad de acertar tu ataque.")
            .color("<#4DF2F5>")
            .symbol("◎")
            .item(ItemContext.builder()
                    .material("light_blue_stained_glass_pane")
                    .group("lower_right") // A group defined in AuraSkills/menus/stats.yml
                    .order(2) // The position within that group
                    .build())
            .build();
}