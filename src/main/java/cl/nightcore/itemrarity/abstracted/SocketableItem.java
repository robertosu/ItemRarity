package cl.nightcore.itemrarity.abstracted;

import cl.nightcore.itemrarity.GemManager;
import cl.nightcore.itemrarity.config.ItemConfig;
import cl.nightcore.itemrarity.item.GemObject;
import cl.nightcore.itemrarity.model.GemModel;
import cl.nightcore.itemrarity.util.ItemUtil;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.StatModifier;
import dev.aurelium.auraskills.api.stat.Stats;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cl.nightcore.itemrarity.ItemRarity.PLUGIN;
import static cl.nightcore.itemrarity.util.ItemUtil.random;

public abstract class SocketableItem extends EnhancedSocketableItem {
    protected static final int MAX_POSSIBLE_SOCKETS = 3;
    private static final String MAX_SOCKETS_KEY = "item_max_sockets";
    private static final String AVAILABLE_SOCKETS_KEY = "item_available_sockets";
    private static final String GEM_KEY_PREFIX = "gem_";

    private static final NamespacedKey MAX_SOCKETS_KEY_NS;
    private static final NamespacedKey AVAILABLE_SOCKETS_KEY_NS;
    private static final GemManager gemManager = new GemManager();

    // Cached Components
    private static final Component GEMS_HEADER;
    private static final Component EMPTY_SOCKET;

    static {
        MAX_SOCKETS_KEY_NS = new NamespacedKey(PLUGIN, MAX_SOCKETS_KEY);
        AVAILABLE_SOCKETS_KEY_NS = new NamespacedKey(PLUGIN, AVAILABLE_SOCKETS_KEY);
        GEMS_HEADER = Component.text("Gemas:")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false);

        EMPTY_SOCKET = Component.text(" ✧ Vacío")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false);
    }

    public SocketableItem(ItemStack item) {
        super(item);
        if (!hasSocketData()) {
            initializeSocketData();
        }
    }

    private boolean hasSocketData() {
        PersistentDataContainer container = getItemMeta().getPersistentDataContainer();
        return container.has(MAX_SOCKETS_KEY_NS, PersistentDataType.INTEGER);
    }

    private void initializeSocketData() {
        ItemMeta meta = getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        int sockets = random.nextInt(MAX_POSSIBLE_SOCKETS) + 1;

        container.set(MAX_SOCKETS_KEY_NS,
                PersistentDataType.INTEGER, sockets);
        container.set(AVAILABLE_SOCKETS_KEY_NS,
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
        String gemLevel = String.format("%d", gem.getLevel());
        container.set(new NamespacedKey(PLUGIN, gemKey),
                PersistentDataType.STRING, gemLevel);

        // Update available sockets
        int availableSockets = getAvailableSockets();
        container.set(AVAILABLE_SOCKETS_KEY_NS,
                PersistentDataType.INTEGER, availableSockets - 1);

        setItemMeta(meta);
        updateLoreWithSockets();
        return true;
    }



    public boolean hasGemWithStat(Stat stat) {
        return getItemMeta().getPersistentDataContainer()
                .has(new NamespacedKey(PLUGIN, GEM_KEY_PREFIX + stat.name()),
                        PersistentDataType.STRING);
    }

    public int getMaxSockets() {
        return getItemMeta().getPersistentDataContainer()
                .getOrDefault(MAX_SOCKETS_KEY_NS,
                        PersistentDataType.INTEGER, 0);
    }

    public int getAvailableSockets() {
        return getItemMeta().getPersistentDataContainer()
                .getOrDefault(AVAILABLE_SOCKETS_KEY_NS,
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
            String value = container.get(new NamespacedKey(PLUGIN, key),
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
        socketInfo.add(GEMS_HEADER);

        // Add installed gems
        Map<Stats, Integer> installedGems = getInstalledGems();
        for (Map.Entry<Stats, Integer> entry : installedGems.entrySet()) {
            Stats stat = entry.getKey();
            int level = entry.getValue();
            int value = calculateGemValue(level);

            // Color del stat
            TextColor statColor = ItemUtil.getColorOfStat(stat);

            // Construcción del texto con colores aplicados explícitamente, otherwise ExcellentEnchants bugea el lore
            Component gemLine = Component.text(" \uD83D\uDC8E ", statColor) // Color explícito para el símbolo
                    .append(Component.text(stat.getDisplayName(AuraSkillsApi.get().getMessageManager().getDefaultLanguage()))
                            .color(statColor)) // Color explícito para el nombre de la stat
                    .append(Component.text(String.format(" +%d", value))
                            .color(statColor)) // Color explícito para el valor
                    .decoration(TextDecoration.ITALIC, false); // Quitar cursiva

            socketInfo.add(gemLine);
        }

        // Add empty sockets
        for (int i = 0; i < getAvailableSockets(); i++) {
            socketInfo.add(EMPTY_SOCKET);
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
            if (line.startsWith("●")) {
                // Retornar la posición siguiente a la línea de rareza
                return i + 1;
            }
        }
        // Si no se encuentra la línea de rareza, insertar al inicio
        return lore.size();
    }
    public void extractAllGems(Player player) {
        Map<Stats, Integer> installedGems = getInstalledGems();

        if (installedGems.isEmpty()) {
            player.sendMessage(
                    ItemConfig.PLUGIN_PREFIX
                            .append(Component.text("Este objeto no tiene gemas instaladas.")
                                    .color(NamedTextColor.GRAY))
            );
            return;
        }
        // Recolectar las gemas extraídas
        List<GemObject> extractedGems = new ArrayList<>();

        // Procesar cada gema instalada
        for (Map.Entry<Stats, Integer> entry : installedGems.entrySet()) {
            Stats stat = entry.getKey();
            int gemLevel = entry.getValue();

            // Crear una instancia de la gema extraída
            GemObject extractedGem = gemManager.createGem(1, gemLevel, stat.name());
            extractedGems.add(extractedGem);

            // Obtener el valor aportado por la gema usando su nivel y fórmula
            int gemValue = 4 + (gemLevel - 1) * gemLevel / 2;

            // Remover el modificador actual
            StatModifier currentModifier = getStatModifiers().stream()
                    .filter(modifier -> modifier.type().equals(stat))
                    .findFirst()
                    .orElse(null);

            if (currentModifier != null) {
                removeSpecificModifier(stat);
                removeSpecificStatLoreLine(stat);
                // Calcular el nuevo valor base sin el boost de gema
                double baseValue = currentModifier.value() - gemValue;
                // Solo reaplicar el modificador si el valor base es mayor a 0
                if (baseValue > 0) {
                    addModifier(stat, (int) baseValue, true);
                }
            }
        }
        ItemMeta meta = getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        for (Stat stat : Stats.values()){
            container.remove(new NamespacedKey(PLUGIN, GEM_KEY_PREFIX + stat.name()));
            container.remove(new NamespacedKey(PLUGIN, GEM_BOOST_PREFIX + stat.name()));
            container.remove(new NamespacedKey(PLUGIN, BASE_STAT_VALUE_PREFIX + stat.name()));

        }
        container.set(AVAILABLE_SOCKETS_KEY_NS, PersistentDataType.INTEGER, getMaxSockets());
        setItemMeta(meta);

        // Actualizar el lore después de todos los cambios
        updateLoreWithSockets();
        setLore();

        // Entregar las gemas al jugador
        for (GemObject gem : extractedGems) {
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(gem);
            if (!leftover.isEmpty()) {
                player.getWorld().dropItemNaturally(player.getLocation(), gem);
            }
        }

        // Notificar al jugador
        player.sendMessage(
                ItemConfig.PLUGIN_PREFIX
                        .append(Component.text("Has extraído todas las gemas del objeto."))
                        .color(NamedTextColor.GRAY)
        );
    }
}