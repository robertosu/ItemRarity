package cl.nightcore.itemrarity.abstracted;

import cl.nightcore.itemrarity.config.CombinedTraits;
import cl.nightcore.itemrarity.config.ItemConfig;
import cl.nightcore.itemrarity.item.ItemUpgrader;
import cl.nightcore.itemrarity.model.ItemUpgraderModel;
import cl.nightcore.itemrarity.util.ItemUtil;
import dev.aurelium.auraskills.api.AuraSkillsBukkit;
import dev.aurelium.auraskills.api.trait.Trait;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import static cl.nightcore.itemrarity.ItemRarity.AURA_LOCALE;
import static cl.nightcore.itemrarity.ItemRarity.PLUGIN;
import static cl.nightcore.itemrarity.config.ItemConfig.LEVEL_KEY_NS;

public class UpgradeableItem extends SocketableItem {
    private static final int MAX_LEVEL = 9;
    private static final NamespacedKey DAMAGE_MODIFIER_KEY = new NamespacedKey(PLUGIN, "level_damage");


    public UpgradeableItem(ItemStack item) {
        super(item);
    }


    public boolean incrementLevel(Player player, ItemUpgraderModel itemUpgrader) {
        ItemMeta meta = this.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        int level = container.get(LEVEL_KEY_NS, PersistentDataType.INTEGER);
        int type = itemUpgrader.getType();
        int percentage = itemUpgrader.getPercentage();

        // Aumentar la probabilidad de fallo en un 5% por cada nivel del objeto
        double adjustedPercentage = percentage - (level * 5);
        if (adjustedPercentage < 0) adjustedPercentage = 0; // Asegurar que no sea negativo

        if (level < MAX_LEVEL) {
            if (ItemUtil.rollthedice(adjustedPercentage)) {
                int newlevel = level + 1;
                this.setNewLevel(newlevel);
                this.setLore();
                this.setMonoliticStats(newlevel);
                this.reApplyMultipliers();
                player.sendMessage(ItemConfig.ITEM_UPGRADER_PREFIX.color(ItemUpgraderModel.getPrimaryColor(itemUpgrader.getType()))
                        .append(Component.text("Mejora exitosa, tu objeto subió a: ", ItemUpgrader.getLoreColor())
                                .append(Component.text("Nivel " + newlevel, NamedTextColor.DARK_GRAY))));

                // Reproducir sonido de éxito
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            } else {
                switch (type) {
                    case 1 -> { // Inestable
                        player.sendMessage(ItemConfig.ITEM_UPGRADER_PREFIX.color(ItemUpgraderModel.getPrimaryColor(itemUpgrader.getType()))
                                .append(Component.text("La mejora falló y tu objeto se rompió.", NamedTextColor.RED)));
                        this.setAmount(0); // Romper el objeto

                        // Reproducir sonido de rotura
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                    }
                    case 2 -> { // Activa
                        if (level > 1) {
                            int newlevel = level - 1;
                            container.set(LEVEL_KEY_NS, PersistentDataType.INTEGER, newlevel);
                            player.sendMessage(ItemConfig.ITEM_UPGRADER_PREFIX.color(ItemUpgraderModel.getPrimaryColor(itemUpgrader.getType()))
                                    .append(Component.text("La mejora falló, tu objeto bajó a: ", NamedTextColor.RED)
                                            .append(Component.text("Nivel " + newlevel, ItemUpgrader.getActiveColor()))));
                            this.setNewLevel(newlevel);
                            this.setLore();
                            this.setMonoliticStats(newlevel);
                            this.reApplyMultipliers();

                            // Reproducir sonido de fallo (puedes usar un sonido diferente si lo deseas)
                            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 1.0f, 1.0f);
                        } else {
                            player.sendMessage(ItemConfig.ITEM_UPGRADER_PREFIX.color(ItemUpgraderModel.getPrimaryColor(itemUpgrader.getType()))
                                    .append(Component.text("La mejora falló.", NamedTextColor.RED)));

                            // Reproducir sonido de fallo
                            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 1.0f, 1.0f);
                        }
                    }
                    case 3 -> { // Estable
                        player.sendMessage(ItemConfig.ITEM_UPGRADER_PREFIX.color(ItemUpgraderModel.getPrimaryColor(itemUpgrader.getType()))
                                .append(Component.text("La mejora falló, pero tu objeto no cambió.", NamedTextColor.YELLOW)));

                        // Reproducir sonido de fallo (puedes usar un sonido diferente si lo deseas)
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
                    }
                }
            }
            return true;
        } else {
            player.sendMessage(ItemConfig.ITEM_UPGRADER_PREFIX
                    .append(Component.text("Tu objeto ya es del nivel máximo. ", ItemUpgrader.getLoreColor())));

            // Reproducir sonido de error o advertencia
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            return false;
        }
    }

    private void setNewLevel(int newlevel) {
        ItemMeta meta;
        PersistentDataContainer container;
        meta = this.getItemMeta();
        container = meta.getPersistentDataContainer();
        container.set(LEVEL_KEY_NS, PersistentDataType.INTEGER, newlevel);
        this.setItemMeta(meta);
    }

    private TextColor getTraitColor(Trait trait) {
        // Primero convertimos el CombinedTrait a su equivalente en Traits si es necesario
        Trait originalTrait = (trait instanceof CombinedTraits) ?
                ((CombinedTraits) trait).getDelegateTrait() : trait;

        var stats = AuraSkillsBukkit.get().getItemManager().getLinkedStats(originalTrait);
        if (stats.stream().findAny().isPresent()) {
            return ItemUtil.getColorOfStat(stats.stream().findAny().get());
        }
        return NamedTextColor.WHITE;
    }


    @Override
    protected void setMonoliticStats(int level) {

        var line = Component.text("|")
                .color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);

        var monoliticTraits = this.statProvider.getMonoliticTraits();

        for (Trait trait : monoliticTraits) {
            var added = determineValueIncreasePerLevelForTrait(trait);
            var value = level * added + added;
            removeTraitModifierByName(this, trait, MONOLITIC_TRAITMODIFIER);
            addMonoliticTraitModifier(this, trait, value);
            var component =
                    Component.text(" +" + getFormattedValue(value,trait) + " ").color(getTraitColor(trait)).decoration(TextDecoration.ITALIC,TextDecoration.State.FALSE)
                            .append(Component.text(trait.getDisplayName(AURA_LOCALE) + " ")
                                    .color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC,TextDecoration.State.FALSE)
                                    .append(Component.text("|").color(NamedTextColor.DARK_GRAY))
                                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            line = line.append(component);
        }
        var meta = this.getItemMeta();
        var lore = meta.lore();
        // 9 trait = SPACES
        // Remove any line that contains exactly 9 spaces
        lore.removeIf(component -> {
            return PlainTextComponentSerializer.plainText().serialize(component).equals("         "); // 9 spaces
        });

        lore.addFirst(Component.text("         ")); // 9 spaces
        lore.addFirst(line);
        meta.lore(lore);
        this.setItemMeta(meta);
    }



    private String getFormattedValue(double value, Trait trait){

        Trait originalTrait = (trait instanceof CombinedTraits) ?
                ((CombinedTraits) trait).getDelegateTrait() : trait;

        return AuraSkillsBukkit.get().getItemManager().getFormattedTraitValue(value, originalTrait);
    }

    private double determineValueIncreasePerLevelForTrait(Trait trait){
        switch(trait.name()){
            case "ATTACK_DAMAGE" -> { return 1.0; }
            case "ATTACK_SPEED" -> { return 0.01; }
            case "DAMAGE_REDUCTION", "HP" -> { return 0.5; }
        }
        return 0;
    }

}
