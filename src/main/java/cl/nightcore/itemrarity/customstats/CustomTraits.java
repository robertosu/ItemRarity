package cl.nightcore.itemrarity.customstats;

import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.trait.CustomTrait;

public class CustomTraits {

    public static final CustomTrait DODGE_CHANCE = CustomTrait
            .builder(NamespacedId.of("itemrarity", "dodge_chance"))
                    .displayName("Probabilidad de esquivar")
                    .build();

    public static final CustomTrait HIT_CHANCE = CustomTrait
            .builder(NamespacedId.of("itemrarity", "hit_chance"))
            .displayName("Probabilidad de acertar")
            .build();


}
