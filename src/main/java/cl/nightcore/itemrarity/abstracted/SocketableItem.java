package cl.nightcore.itemrarity.abstracted;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.model.GemModel;
import cl.nightcore.itemrarity.util.ItemUtil;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.Stats;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public abstract class SocketableItem extends EnhancedSocketableItem {
    protected static final int MAX_POSSIBLE_SOCKETS = 3;
    private static final String MAX_SOCKETS_KEY = "item_max_sockets";
    private static final String AVAILABLE_SOCKETS_KEY = "item_available_sockets";
    private static final String GEM_KEY_PREFIX = "gem_";

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


        Stat stat = gem.getStat();


        // Aplicar la gema primero
        addGemStat(stat, gem.getValue());

        // Luego actualizar los datos del socket con el ItemMeta más reciente
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
        updateLoreWithSockets();
        return true;
    }



    public boolean hasGemWithStat(Stat stat) {
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

    public Map<Stats, Integer> getInstalledGems() {
        Map<Stats, Integer> gems = new HashMap<>();
        PersistentDataContainer container = getItemMeta().getPersistentDataContainer();

        for (Stats stat : Stats.values()) {
            String key = GEM_KEY_PREFIX + stat.name();
            String value = container.get(new NamespacedKey(ItemRarity.plugin, key),
                    PersistentDataType.STRING);
            if (value != null) {
                gems.put(stat, Integer.parseInt(value));
            }
        }

        return gems;
    }


    public void updateLoreWithSockets() {
        ItemMeta meta = getItemMeta();
        List<Component> lore = meta.hasLore() ? meta.lore() : new ArrayList<>();
        if (lore == null) lore = new ArrayList<>();

        // Remove existing socket information
        lore.removeIf(line -> line.toString().contains("✧")
                || line.toString().contains("\uD83D\uDC8E")
                || line.toString().contains("Gemas:"));

        // Add socket header
        List<Component> socketInfo = new ArrayList<>();
        socketInfo.add(Component.text("Gemas:")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));

        // Add installed gems
        Map<Stats, Integer> installedGems = getInstalledGems();
        for (Map.Entry<Stats, Integer> entry : installedGems.entrySet()) {
            Stats stat = entry.getKey();
            int level = entry.getValue();
            int value = calculateGemValue(level);

            socketInfo.add(Component.text(" \uD83D\uDC8E ")
                    .color(ItemUtil.getColorOfStat(stat))
                    .append(Component.text(stat.getDisplayName(
                            AuraSkillsApi.get().getMessageManager().getDefaultLanguage())))
                    .append(Component.text(String.format(" +%d", value)))
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
        // Buscar la línea que comienza con (rareza)
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