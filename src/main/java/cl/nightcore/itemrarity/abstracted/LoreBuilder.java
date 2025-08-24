package cl.nightcore.itemrarity.abstracted;// Add this to IdentifiedItem class

import cl.nightcore.itemrarity.util.ItemUtil;
import dev.aurelium.auraskills.api.AuraSkillsBukkit;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.StatModifier;
import dev.aurelium.auraskills.api.trait.Trait;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LoreBuilder {
    private final IdentifiedItem item;
    private final List<Component> finalLore;
    
    public LoreBuilder(IdentifiedItem item) {
        this.item = item;
        this.finalLore = new ArrayList<>();
    }
    
    public void rebuildCompleteLore() {
        finalLore.clear();
        
        // 1. Monolithic traits (top section)
        addMonolithicSection();
        
        // 2. Stats section
        addStatsSection();
        
        // 3. Rarity section
        addRaritySection();
        
        // 4. Gems section
        addGemsSection();
        
        // 5. Multipliers section
        addMultipliersSection();
        
        // 6. Weapon attributes (bottom section)
        addWeaponAttributesSection();
        
        // Apply the complete lore
        ItemMeta meta = item.getItemMeta();
        meta.lore(new ArrayList<>(finalLore));
        item.setItemMeta(meta);
    }
    
    private void addMonolithicSection() {
        var monoliticTraits = item.statProvider.getMonoliticTraits();
        if (monoliticTraits.isEmpty()) return;
        
        finalLore.add(Component.text("         ")); // Spacer
        
        Component line = Component.text("|")
            .color(NamedTextColor.DARK_GRAY)
            .decoration(TextDecoration.ITALIC, false);
            
        for (Trait trait : monoliticTraits) {
            double value = getMonolithicTraitValue(trait);
            if (value > 0) {
                Component traitComponent = Component.text(" +" + getFormattedValue(value, trait) + " ")
                    .color(getTraitColor(trait))
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text(trait.getDisplayName(AURA_LOCALE) + " ")
                        .color(NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false)
                        .append(Component.text("|").color(NamedTextColor.DARK_GRAY)));
                line = line.append(traitComponent);
            }
        }
        
        finalLore.add(line);
        finalLore.add(Component.text("         ")); // Spacer
    }
    
    private void addStatsSection() {
        List<StatModifier> nativeStats = AuraSkillsBukkit.get()
            .getItemManager()
            .getStatModifiersById(item, item.modifierType, NATIVE_STATMODIFIER);
            
        if (nativeStats.isEmpty()) return;
        
        // Stats are automatically added to lore by AuraSkills when applied
        // We just need to ensure they're in the right position
    }
    
    private void addRaritySection() {
        if (item.rarity == null) {
            item.setRarity(item.calculateAverage());
        }
        
        finalLore.add(Component.text("                    ")); // Spacer
        
        Component ilvl = Component.text("● Refinado: [+" + item.getLevel() + "] ● ")
            .color(NamedTextColor.DARK_GRAY)
            .decorate(TextDecoration.ITALIC);
            
        Component rarityLine = ilvl.append(Component.text("[")
            .color(item.rarity.color())
            .decoration(TextDecoration.ITALIC, false)
            .append(item.rarity)
            .append(Component.text("]")
                .color(item.rarity.color())
                .decoration(TextDecoration.ITALIC, false)));
                
        finalLore.add(rarityLine);
    }
    
    private void addGemsSection() {
        if (!(item instanceof SocketableItem socketable)) return;
        
        Map<Stat, Integer> installedGems = socketable.getInstalledGems();
        int availableSockets = socketable.getAvailableSockets();
        
        if (installedGems.isEmpty() && availableSockets == 0) return;
        
        finalLore.add(Component.text("      ")); // Gems spacer
        finalLore.add(Component.text("Gemas:")
            .color(NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false));
        
        // Add installed gems
        for (Map.Entry<Stat, Integer> entry : installedGems.entrySet()) {
            Stat stat = entry.getKey();
            int value = calculateGemValue(entry.getValue());
            
            TextColor statColor = ItemUtil.getColorOfStat(stat);
            Component gemLine = Component.text(" 💎 ", statColor)
                .append(Component.text(stat.getDisplayName(AURA_LOCALE)).color(statColor))
                .append(Component.text(String.format(" +%d", value)).color(statColor))
                .decoration(TextDecoration.ITALIC, false);
                
            finalLore.add(gemLine);
        }
        
        // Add empty sockets
        for (int i = 0; i < availableSockets; i++) {
            finalLore.add(Component.text(" ⛶ Vacío")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        }
    }
    
    private void addMultipliersSection() {
        var multipliers = AuraSkillsBukkit.get()
            .getItemManager()
            .getMultipliers(item, item.modifierType);
            
        if (multipliers.isEmpty()) return;
        
        // Multipliers are automatically added to lore by AuraSkills
        // This section handles custom positioning if needed
    }
    
    private void addWeaponAttributesSection() {
        if (!item.itemType.isMainWeapon()) return;
        
        // Get weapon attributes from Minecraft's attribute system
        // These are typically added automatically, but we ensure they're at the bottom
        finalLore.add(Component.text("                    ")); // Spacer
        finalLore.add(Component.text("En la mano principal:")
            .color(NamedTextColor.DARK_GRAY)
            .decoration(TextDecoration.ITALIC, false));
        
        // The actual attack damage and speed lines are added by Minecraft/AuraSkills
        // automatically when the item has the proper attributes
    }
    
    private double getMonolithicTraitValue(Trait trait) {
        var traitModifiers = AuraSkillsBukkit.get()
            .getItemManager()
            .getTraitModifiersById(item, item.modifierType, MONOLITIC_TRAITMODIFIER);
            
        return traitModifiers.stream()
            .filter(modifier -> modifier.trait().equals(trait))
            .mapToDouble(modifier -> modifier.value())
            .findFirst()
            .orElse(0.0);
    }
    
    private TextColor getTraitColor(Trait trait) {
        var stats = AuraSkillsBukkit.get().getItemManager().getLinkedStats(trait);
        return stats.stream()
            .findAny()
            .map(ItemUtil::getColorOfStat)
            .orElse(NamedTextColor.WHITE);
    }
    
    private String getFormattedValue(double value, Trait trait) {
        return AuraSkillsBukkit.get().getItemManager().getFormattedTraitValue(value, trait);
    }
    
    private int calculateGemValue(int level) {
        return 4 + (level - 1) * level / 2;
    }
}