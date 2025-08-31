package cl.nightcore.itemrarity.abstracted;

import dev.aurelium.auraskills.api.AuraSkillsBukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class XpBonusItem extends SocketableItem {

    public XpBonusItem(ItemStack item) {
        super(item);
    }

    public void addExperienceMultiplier(int multiplier, Player player) {
        // Obtener el multiplicador actual
        int currentMultiplier = getExperienceMultiplier();

        // Verificar si el nuevo multiplicador es mayor
        if (multiplier > currentMultiplier
                || AuraSkillsBukkit.get()
                        .getItemManager()
                        .getMultipliers(this, modifierType)
                        .isEmpty()) {
            // Aplicar el nuevo multiplicador
            setItemMeta(AuraSkillsBukkit.get()
                    .getItemManager()
                    .addMultiplier(this, modifierType, null, multiplier, true)
                    .getItemMeta());

            this.setNewLore();
        }
    }

    public int getExperienceMultiplier() {
        if (!AuraSkillsBukkit.get()
                .getItemManager()
                .getMultipliers(this, modifierType)
                .isEmpty()) {
            return (int) AuraSkillsBukkit.get()
                    .getItemManager()
                    .getMultipliers(this, modifierType)
                    .getFirst()
                    .value();
        } else {
            return 0;
        }
    }
}
