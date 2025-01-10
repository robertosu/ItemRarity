package cl.nightcore.itemrarity.abstracted;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.model.GemModel;
import cl.nightcore.itemrarity.util.ItemUtil;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.AuraSkillsBukkit;
import dev.aurelium.auraskills.api.stat.Stats;
import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.trait.Traits;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.*;

public abstract class SocketableItem extends IdentifiedItem {
    private static final String MAX_SOCKETS_KEY = "item_max_sockets";
    private static final String AVAILABLE_SOCKETS_KEY = "item_available_sockets";
    private static final String GEM_KEY_PREFIX = "gem_";

    protected static final int MAX_POSSIBLE_SOCKETS = 3;

    public SocketableItem(ItemStack item) {
        super(item);
        if (!hasSocketData()) {
            initializeSocketData();
        }
    }

    private boolean hasSocketData() {
        PersistentDataContainer container = getItemMeta().getPersistentDataContainer();
        return container.has(new NamespacedKey(ItemRarity.plugin, MAX_SOCKETS_KEY), PersistentDataType.INTEGER) &&
                container.has(new NamespacedKey(ItemRarity.plugin, AVAILABLE_SOCKETS_KEY), PersistentDataType.INTEGER);
    }

    private void initializeSocketData() {
        ItemMeta meta = getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        int sockets = new Random().nextInt(MAX_POSSIBLE_SOCKETS) + 1;

        container.set(new NamespacedKey(ItemRarity.plugin, MAX_SOCKETS_KEY),
                PersistentDataType.INTEGER, sockets);
        container.set(new NamespacedKey(ItemRarity.plugin, AVAILABLE_SOCKETS_KEY),
                PersistentDataType.INTEGER, sockets);

        setItemMeta(meta);
        updateLoreWithSockets();
    }

    public boolean installGem(GemModel gem) {
        if (gem == null || !gem.isValid() || !hasAvailableSockets()) {
            return false;
        }

        Trait stat = gem.getStat();
        if (stat == null || hasGemWithStat(stat)) {
            return false;
        }

        ItemMeta meta = getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        // Store gem data
        String gemKey = GEM_KEY_PREFIX + stat.name();
        String gemValue = String.format("%d", gem.getLevel());
        container.set(new NamespacedKey(ItemRarity.plugin, gemKey),
                PersistentDataType.STRING, gemValue);

        // Update available sockets
        int availableSockets = getAvailableSockets();
        container.set(new NamespacedKey(ItemRarity.plugin, AVAILABLE_SOCKETS_KEY),
                PersistentDataType.INTEGER, availableSockets - 1);

        setItemMeta(meta);
        addTrait(stat,gem.getValue());
        updateLoreWithSockets();
        return true;
    }
    void addTrait(Trait trait, int value) {
        this.setItemMeta(AuraSkillsBukkit.get().getItemManager().addTraitModifier(this, MODIFIER_TYPE, trait, value, true).getItemMeta());
    }


    private boolean hasGemWithStat(Trait stat) {
        return getItemMeta().getPersistentDataContainer()
                .has(new NamespacedKey(ItemRarity.plugin, GEM_KEY_PREFIX + stat.name()),
                        PersistentDataType.STRING);
    }

    public int getMaxSockets() {
        return getItemMeta().getPersistentDataContainer()
                .getOrDefault(new NamespacedKey(ItemRarity.plugin, MAX_SOCKETS_KEY),
                        PersistentDataType.INTEGER, 0);
    }

    public int getAvailableSockets() {
        return getItemMeta().getPersistentDataContainer()
                .getOrDefault(new NamespacedKey(ItemRarity.plugin, AVAILABLE_SOCKETS_KEY),
                        PersistentDataType.INTEGER, 0);
    }

    public boolean hasAvailableSockets() {
        return getAvailableSockets() > 0;
    }

    public Map<Traits, Integer> getInstalledGems() {
        Map<Traits, Integer> gems = new HashMap<>();
        PersistentDataContainer container = getItemMeta().getPersistentDataContainer();

        for (Traits trait : Traits.values()) {
            String key = GEM_KEY_PREFIX + trait.name();
            String value = container.get(new NamespacedKey(ItemRarity.plugin, key),
                    PersistentDataType.STRING);
            if (value != null) {
                gems.put(trait, Integer.parseInt(value));
            }
        }

        return gems;
    }


    public void updateLoreWithSockets() {
        ItemMeta meta = getItemMeta();
        List<Component> lore = meta.hasLore() ? meta.lore() : new ArrayList<>();
        if (lore == null) lore = new ArrayList<>();

        // Remove existing socket information
        lore.removeIf(line -> line.toString().contains("◇") ||
                line.toString().contains("◈") ||
                line.toString().contains("Engarzados:"));

        // Add socket header
        List<Component> socketInfo = new ArrayList<>();
        socketInfo.add(Component.text("Engarzados:")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));

        // Add installed gems
        Map<Traits, Integer> installedGems = getInstalledGems();
        for (Map.Entry<Traits, Integer> entry : installedGems.entrySet()) {
            Traits trait = entry.getKey();
            int level = entry.getValue();
            //int value = calculateGemValue(level);

            socketInfo.add(Component.text(" \uD83D\uDC8E ")
                    .color(TextColor.fromHexString(ItemUtil.getColorOfTrait(trait)))
                    .append(Component.text(trait.getDisplayName(
                            AuraSkillsApi.get().getMessageManager().getDefaultLanguage())))
                    .append(Component.text(String.format(" (+%d)", level)))
                    .decoration(TextDecoration.ITALIC, false));
        }

        // Add empty sockets
        for (int i = 0; i < getAvailableSockets(); i++) {
            socketInfo.add(Component.text(" ✧ Vacío")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }

        // Insert socket information at appropriate position
        int insertIndex = findSocketInfoInsertIndex(lore);
        lore.addAll(insertIndex, socketInfo);

        meta.lore(lore);
        setItemMeta(meta);
    }

    private int calculateGemValue(int level) {
        return 4 + (level - 1) * level / 2;
    }

    private int findSocketInfoInsertIndex(List<Component> lore) {
        // Buscar la línea que comienza con '[' (rareza)
        for (int i = 0; i < lore.size(); i++) {
            String line = PlainTextComponentSerializer.plainText().serialize(lore.get(i));
            if (line.startsWith("[")) {
                // Retornar la posición siguiente a la línea de rareza
                return i + 1;
            }
        }
        // Si no se encuentra la línea de rareza, insertar al inicio
        return lore.size();
    }
}