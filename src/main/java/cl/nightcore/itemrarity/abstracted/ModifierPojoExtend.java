package cl.nightcore.itemrarity.abstracted;

import dev.aurelium.auraskills.api.item.ItemManager;
import dev.aurelium.auraskills.api.item.ModifierType;
import dev.aurelium.auraskills.api.registry.NamespaceIdentified;
import dev.aurelium.auraskills.api.util.AuraSkillsModifier;

public class ModifierPojoExtend extends ItemManager.ModifierPojo {
    public ModifierPojoExtend(ModifierType type, NamespaceIdentified stat, double value, AuraSkillsModifier.Operation operation, String identifier, boolean lore, boolean gaussian) {
        super(type, stat, value, operation, identifier, lore);
        this.gaussian = gaussian;
    }

    public boolean gaussian;

}
