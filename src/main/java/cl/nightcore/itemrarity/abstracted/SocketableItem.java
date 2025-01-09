package cl.nightcore.itemrarity.abstracted;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.abstracted.IdentifiedItem;
import cl.nightcore.itemrarity.item.Gem;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class SocketableItem extends IdentifiedItem implements Socketable {
    private static final String SOCKETS_KEY = "itemrarity_socket_count";
    private static final String GEMS_KEY = "itemrarity_installed_gems";
    protected static final int MAX_SOCKETS = 3;

    private final List<Gem> installedGems;

    public SocketableItem(ItemStack item) {
        super(item);
        this.installedGems = loadInstalledGems();
        if (!hasSocketData()) {
            initializeSocketData();
        }
    }

    private boolean hasSocketData() {
        PersistentDataContainer container = getItemMeta().getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(ItemRarity.plugin, SOCKETS_KEY);
        return container.has(key, PersistentDataType.INTEGER);
    }

    private void initializeSocketData() {
        ItemMeta meta = getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(ItemRarity.plugin, SOCKETS_KEY);
        container.set(key, PersistentDataType.INTEGER, 1); // Start with 1 socket
        setItemMeta(meta);
        updateSocketDisplay();
    }

    @Override
    public int getMaxSockets() {
        return MAX_SOCKETS;
    }

    @Override
    public int getCurrentSockets() {
        PersistentDataContainer container = getItemMeta().getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(ItemRarity.plugin, SOCKETS_KEY);
        return container.getOrDefault(key, PersistentDataType.INTEGER, 0);
    }

    @Override
    public List<Gem> getInstalledGems() {
        return new ArrayList<>(installedGems);
    }

    @Override
    public boolean addSocket() {
        int current = getCurrentSockets();
        if (current >= MAX_SOCKETS) return false;

        ItemMeta meta = getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(ItemRarity.plugin, SOCKETS_KEY);
        container.set(key, PersistentDataType.INTEGER, current + 1);
        setItemMeta(meta);
        updateSocketDisplay();
        return true;
    }
    public void generateSockets(){
        ItemMeta meta = getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(ItemRarity.plugin, SOCKETS_KEY);
        Random random = new Random();
        container.set(key, PersistentDataType.INTEGER,random.nextInt(3) + 1);
        setItemMeta(meta);
        updateSocketDisplay();

    }

    @Override
    public boolean hasEmptySockets() {
        return installedGems.size() < getCurrentSockets();
    }

    @Override
    public boolean installGem(Gem gem) {
        if (!hasEmptySockets() || gem == null) return false;

        installedGems.add(gem);
        addModifier(gem.getStat(), gem.getValue());
        updateSocketDisplay();
        return true;
    }



    @Override
    public Gem removeGem(int socketIndex) {
        if (socketIndex < 0 || socketIndex >= installedGems.size()) return null;

        Gem removed = installedGems.remove(socketIndex);
        removeSpecificModifier(removed.getStat());
        updateSocketDisplay();
        return removed;
    }

    @Override
    public void updateSocketDisplay() {
        ItemMeta meta = getItemMeta();
        List<Component> lore = meta.lore();
        if (lore == null) lore = new ArrayList<>();

        // Remover información existente de sockets
        lore.removeIf(line ->
                line.toString().contains("◇") ||
                        line.toString().contains("◈") ||
                        line.toString().contains("Engarzados:"));

        // Crear nueva información de sockets
        List<Component> socketInfo = new ArrayList<>();
        socketInfo.add(Component.text("Engarzados:").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));

        for (int i = 0; i < getCurrentSockets(); i++) {
            if (i < installedGems.size()) {
                Gem gem = installedGems.get(i);
                socketInfo.add(Component.text(" ◈ ").color(NamedTextColor.GREEN)
                        .append(gem.getGemName())
                        .append(Component.text(String.format(" (+%d %s)",
                                gem.getValue(),
                                gem.getStat().getDisplayName(AuraSkillsApi.get().getMessageManager().getDefaultLanguage()))))
                        .decoration(TextDecoration.ITALIC, false));
            } else {
                socketInfo.add(Component.text(" ◇ Vacío").color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false));
            }
        }

        // Encontrar la posición donde insertar los sockets
        int insertIndex = 1; // Después de la línea de rareza
        for (int i = 0; i < lore.size(); i++) {
            String line = PlainTextComponentSerializer.plainText().serialize(lore.get(i));
            if (line.contains("En la mano principal:") || line.contains("          ")) {
                insertIndex = i;
                break;
            }
        }

        // Insertar la información de sockets en la posición correcta
        lore.addAll(insertIndex, socketInfo);

        meta.lore(lore);
        setItemMeta(meta);
    }

    private void saveInstalledGems() {
        // Implementation for saving gems to NBT
    }

    private List<Gem> loadInstalledGems() {
        // Implementation for loading gems from NBT
        return new ArrayList<>();
    }
}