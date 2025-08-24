package cl.nightcore.itemrarity.customstats;

import dev.aurelium.auraskills.api.item.ItemContext;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.stat.CustomStat;
import dev.aurelium.auraskills.api.trait.Traits;

public class CustomStats {

    public static final CustomStat DEXTERITY = CustomStat.builder(NamespacedId.of("itemrarity", "dexterity"))
            .trait(CustomTraits.ATTACK_SPEED, 0.5)
            .trait(Traits.MOVEMENT_SPEED, 1)
            .displayName("Destreza")
            .description("La destreza aumenta tu velocidad de ataque y tu velocidad de movimiento.")
            .color("<#53C259>")
            .symbol("➾")
            .build();
    public static final CustomStat EVASION = CustomStat.builder(NamespacedId.of("itemrarity", "evasion"))
            .trait(CustomTraits.DODGE_CHANCE, 0.5) // Dodge chance will increase by 0.5 per evasion level
            .displayName("Evasión")
            .description("La evasión aumenta la probabilidad de esquivar ataques.")
            .color("<#F0F0F0>")
            .symbol("⛹")
            .build();
    public static final CustomStat ACCURACY = CustomStat.builder(NamespacedId.of("itemrarity", "accuracy"))
            .trait(CustomTraits.HIT_CHANCE, 0.5) // Hit chance will increase by 0.5 per accuracy level
            .displayName("Precisión")
            .description("La precisión aumenta tu probabilidad de acertar ataques, contrarresta directamente la evasión.")
            .color("<#FF80D5>")
            .symbol("\uD83C\uDFAF")
            .build();
    private static final ItemContext dexterityContext = ItemContext.builder()
            .material("paper")
            .set("custom_model_data", 7008)
            .group("upper_right") // A group defined in AuraSkills/menus/stats.yml
            .order(1) // The position within that group
            .build();
    private static final ItemContext evasionContext = ItemContext.builder()
            .material("paper")
            .set("custom_model_data", 7009)
            .group("upper_right") // A group defined in AuraSkills/menus/stats.yml
            .order(2) // The position within that group
            .build();
    private static final ItemContext accuracyContext = ItemContext.builder()
            .material("paper")
            .set("custom_model_data", 7011)
            .group("upper_right") // A group defined in AuraSkills/menus/stats.yml
            .order(3) // The position within that group
            .build();


}