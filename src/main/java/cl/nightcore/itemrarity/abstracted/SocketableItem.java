package cl.nightcore.itemrarity.abstracted;

import cl.nightcore.itemrarity.GemManager;
import cl.nightcore.itemrarity.classes.StatValueGenerator;
import cl.nightcore.itemrarity.config.CombinedStats;
import cl.nightcore.itemrarity.config.ItemConfig;
import cl.nightcore.itemrarity.item.GemObject;
import cl.nightcore.itemrarity.model.GemModel;
import cl.nightcore.itemrarity.model.GemRemoverModel;
import cl.nightcore.itemrarity.util.ItemUtil;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.AuraSkillsBukkit;
import dev.aurelium.auraskills.api.item.ModifierType;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.StatModifier;
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
import java.util.concurrent.ThreadLocalRandom;

import static cl.nightcore.itemrarity.ItemRarity.PLUGIN;
import static cl.nightcore.itemrarity.util.ItemUtil.getStatProvider;
import static cl.nightcore.itemrarity.util.ItemUtil.RANDOM;

public class SocketableItem extends IdentifiedItem {

    protected static final int MAX_POSSIBLE_SOCKETS = 3;
    public static final String GEM_BOOST_PREFIX = "gem_boost_";
    public static final String BASE_STAT_VALUE_PREFIX = "base_stat_";


    private static final String MAX_SOCKETS_KEY = "item_max_sockets";
    private static final String AVAILABLE_SOCKETS_KEY = "item_available_sockets";
    private static final String GEM_KEY_PREFIX = "gem_";

    private static final NamespacedKey MAX_SOCKETS_KEY_NS = new NamespacedKey(PLUGIN, MAX_SOCKETS_KEY);
    private static final NamespacedKey AVAILABLE_SOCKETS_KEY_NS = new NamespacedKey(PLUGIN, AVAILABLE_SOCKETS_KEY);
    private static final GemManager gemManager = new GemManager();

    private static final Component GEMS_HEADER = Component.text("Gemas:")
            .color(NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false);

    private static final Component EMPTY_SOCKET = Component.text(" ✧ Vacío")
            .color(NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false);



    public SocketableItem(ItemStack item) {
        super(item);
    }

    public void initializeSocketData() {
        ItemMeta meta = getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        int sockets = RANDOM.nextInt(MAX_POSSIBLE_SOCKETS) + 1;

        container.set(MAX_SOCKETS_KEY_NS,
                PersistentDataType.INTEGER, sockets);
        container.set(AVAILABLE_SOCKETS_KEY_NS,
                PersistentDataType.INTEGER, sockets);

        setItemMeta(meta);
        updateLoreWithSockets();
    }

    public boolean installGem(GemModel gem, Player player) {
        // Verificar si hay espacios disponibles para gemas
        if (!hasAvailableSockets()) {
            player.sendMessage(ItemConfig.GEMSTONE_PREFIX
                    .append(Component.text("Este objeto no tiene espacios disponibles para gemas.")
                            .color(NamedTextColor.RED)));
            return false;
        }

        // Verificar si ya hay una gema del mismo tipo
        if (hasGemWithStat(gem.getStat())) {
            player.sendMessage(ItemConfig.GEMSTONE_PREFIX
                    .append(Component.text("El objeto ya tiene una gema de este tipo.")
                            .color(NamedTextColor.RED)));
            return false;
        }

        // Verificar si la gema es compatible con las stats posibles para un item
        if (!gem.isCompatible(ItemUtil.getStatProvider(this))) {
            sendCompatibleGemsForItemType(player);
            return false;
        }

        // Si todas las verificaciones pasan, proceder a instalar la gema
        Stat stat = gem.getStat();

        // Aplicar la gema usando el nuevo método de la API
        ItemStack modifiedItem = AuraSkillsBukkit.get()
                .getItemManager()
                .addStatModifier(this, modifierType, stat, "gema", gem.getValue(), false);

        setItemMeta(modifiedItem.getItemMeta());

        // Actualizar los datos del socket con el ItemMeta más reciente
        ItemMeta meta = getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        // Store gem data
        String gemKey = GEM_KEY_PREFIX + stat.name();
        String gemLevel = String.format("%d", gem.getLevel());
        container.set(new NamespacedKey(PLUGIN, gemKey), PersistentDataType.STRING, gemLevel);

        // Update available sockets
        int availableSockets = getAvailableSockets();
        container.set(AVAILABLE_SOCKETS_KEY_NS, PersistentDataType.INTEGER, availableSockets - 1);

        setItemMeta(meta);
        updateLoreWithSockets();

        player.sendMessage(ItemConfig.GEMSTONE_PREFIX
                .append(Component.text("¡Gema instalada con éxito!")
                        .color(NamedTextColor.GREEN)));

        return true;
    }

    public boolean extractAllGems(Player player, GemRemoverModel gemRemover) {
        Map<Stat, Integer> installedGems = getInstalledGems();

        if (installedGems.isEmpty()) {
            player.sendMessage(
                    ItemConfig.GEMSTONE_PREFIX
                            .append(Component.text("Este objeto no tiene gemas instaladas.")
                                    .color(NamedTextColor.GRAY))
            );
            return false;
        }

        // Recolectar las gemas extraídas
        List<GemObject> extractedGems = new ArrayList<>();

        // Procesar cada gema instalada
        for (Map.Entry<Stat, Integer> entry : installedGems.entrySet()) {
            Stat stat = entry.getKey();
            int gemLevel = entry.getValue();

            // Verificar si la gema se rompe
            if (gemBreaks(gemRemover.getPercentage())) {
                player.sendMessage(ItemConfig.GEMSTONE_PREFIX
                        .append(Component.text("La gema de ")
                                .color(NamedTextColor.RED)
                                .append(Component.text(stat.getDisplayName(AuraSkillsApi.get().getMessageManager().getDefaultLanguage()))
                                        .color(ItemUtil.getColorOfStat(stat)))
                                .append(Component.text(" se rompió durante la extracción."))));
            } else {
                // Crear una instancia de la gema extraída
                GemObject extractedGem = gemManager.createGem(1, gemLevel, stat.name());
                extractedGems.add(extractedGem);
            }

            // Remover el modificador de la gema
            ItemStack modifiedItem = AuraSkillsBukkit.get()
                    .getItemManager()
                    .removeStatModifier(this, modifierType, stat,"gema");

            setItemMeta(modifiedItem.getItemMeta());
        }

        ItemMeta meta = getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        for (Stat stat : CombinedStats.values()) {
            container.remove(new NamespacedKey(PLUGIN, GEM_KEY_PREFIX + stat.name()));
        }

        container.set(AVAILABLE_SOCKETS_KEY_NS, PersistentDataType.INTEGER, getMaxSockets());
        setItemMeta(meta);
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
                ItemConfig.GEMSTONE_PREFIX
                        .append(Component.text("Has extraído las gemas del objeto."))
                        .color(NamedTextColor.GRAY)
        );

        return true;
    }

    // ... (otros métodos)

    protected void addGemStat(Stat stat, int gemValue) {
        // Aplicar el modificador de la gema usando el nuevo metodo de la API
        ItemStack modifiedItem = AuraSkillsBukkit.get()
                .getItemManager()
                .addStatModifier(this, modifierType, stat, "gema", gemValue, false);

        setItemMeta(modifiedItem.getItemMeta());
        updateLoreWithSockets();
    }




    public Map<Stat, Integer> getInstalledGems() {
        Map<Stat, Integer> gems = new HashMap<>();
        PersistentDataContainer container = getItemMeta().getPersistentDataContainer();

        for (Stat stat : CombinedStats.values()) {
            String key = GEM_KEY_PREFIX + stat.name();
            String value = container.get(new NamespacedKey(PLUGIN, key),
                    PersistentDataType.STRING);
            if (value != null) {
                gems.put(stat, Integer.parseInt(value));
            }
        }
        return gems;
    }

    @Override
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
        Map<Stat, Integer> installedGems = getInstalledGems();
        for (Map.Entry<Stat, Integer> entry : installedGems.entrySet()) {
            Stat stat = entry.getKey();
            int value = calculateGemValue(entry.getValue()); //calcular valor de la gema basado en su nivel.

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




    private boolean gemBreaks(int percentage) {
        double chance = percentage / 100.0;
        return chance > ThreadLocalRandom.current().nextDouble();
    }




    public boolean addRandomMissingStat(Player player) {

        if (this.getMaxBonuses() != 6) {
            try {
                // Verificar si ya se han alcanzado 6 stats (excluyendo las stats que son solo por gema)
                int nativeStatsCount = getNativeStatsCount();
                if (nativeStatsCount >= 6) {
                    player.sendMessage(ItemConfig.PLUGIN_PREFIX
                            .append(Component.text("El ítem ya tiene el máximo de estadísticas permitidas (6).")
                                    .color(NamedTextColor.RED)));
                    System.out.println("[ItemRarity] No se agregó una nueva stat: el ítem ya tiene 6 stats nativas.");
                    return false;
                }
                this.setMaxBonuses(6);
                // Obtener todas las stats disponibles
                List<Stat> availableStats = getStatProvider(this).getAvailableStats();
                System.out.println("[ItemRarity] Stats disponibles: " + availableStats);

                // Obtener las stats actuales en el ítem (normalizadas)
                List<String> currentStatsNormalized = getStatModifiers().stream()
                        .map(modifier -> modifier.type().name()) // Usar el nombre de la stat
                        .toList();
                System.out.println("[ItemRarity] Stats actuales en el ítem (normalizadas): " + currentStatsNormalized);

                // Filtrar las stats que no están presentes en el ítem (o que solo están presentes por una gema)
                List<Stat> missingStats = new ArrayList<>();
                PersistentDataContainer container = getItemMeta().getPersistentDataContainer();

                for (Stat stat : availableStats) {
                    // Normalizar el nombre de la stat
                    String statName = stat.name();

                    // Verificar si la stat ya está presente en el ítem (excluyendo las stats de gemas)
                    boolean isStatPresent = currentStatsNormalized.contains(statName);

                    // Verificar si la stat tiene un valor base almacenado en el NBT
                    boolean hasBaseValue = container.has(new NamespacedKey(PLUGIN, BASE_STAT_VALUE_PREFIX + statName), PersistentDataType.INTEGER);

                    // Verificar si la stat está presente solo por una gema
                    boolean isStatFromGem = container.has(new NamespacedKey(PLUGIN, GEM_BOOST_PREFIX + statName), PersistentDataType.INTEGER);

                    // Logging detallado para cada stat
                    System.out.println("[ItemRarity] Verificando stat: " + statName +
                            ", isStatPresent: " + isStatPresent +
                            ", hasBaseValue: " + hasBaseValue +
                            ", isStatFromGem: " + isStatFromGem);

                    // Si la stat no está presente en absoluto, o si está presente solo por una gema (sin valor base), es candidata para ser agregada
                    if (!isStatPresent || (isStatFromGem && !hasBaseValue)) {
                        missingStats.add(stat);
                        System.out.println("[ItemRarity] Stat añadida a missingStats: " + statName);
                    }
                }

                System.out.println("[ItemRarity] Stats faltantes: " + missingStats);

                // Si no hay stats faltantes, notificar al jugador
                if (missingStats.isEmpty()) {
                    player.sendMessage(ItemConfig.PLUGIN_PREFIX
                            .append(Component.text("No hay estadísticas faltantes para agregar.")
                                    .color(NamedTextColor.RED)));
                    System.out.println("[ItemRarity] No se agregó una nueva stat: no hay stats faltantes.");
                    return false;
                }

                // Elegir una stat aleatoria de las faltantes

                Stat statToAdd = missingStats.get(RANDOM.nextInt(missingStats.size()));
                System.out.println("[ItemRarity] Stat seleccionada para agregar: " + statToAdd.name());

                // Verificar si la stat está presente solo por una gema
                boolean isStatFromGem = container.has(new NamespacedKey(PLUGIN, GEM_BOOST_PREFIX + statToAdd.name()), PersistentDataType.INTEGER);

                if (isStatFromGem) {
                    // Si la stat está presente solo por una gema, obtener el boost de la gema
                    int gemBoost = container.getOrDefault(new NamespacedKey(PLUGIN, GEM_BOOST_PREFIX + statToAdd.name()), PersistentDataType.INTEGER, 0);
                    System.out.println("[ItemRarity] Stat presente solo por gema. Boost de gema: " + gemBoost);

                    // Generar un valor base para la stat
                    int baseValue = StatValueGenerator.generateValueForStat(getRollQuality(), statProvider.isThisStatGauss(statToAdd));
                    System.out.println("[ItemRarity] Valor base generado: " + baseValue);

                    // Guardar el valor base en NBT
                    ItemMeta meta = getItemMeta();
                    container = meta.getPersistentDataContainer();
                    container.set(new NamespacedKey(PLUGIN, BASE_STAT_VALUE_PREFIX + statToAdd.name()), PersistentDataType.INTEGER, baseValue);
                    setItemMeta(meta);

                    // Aplicar el valor total (base + boost de gema)
                    addModifier(statToAdd, baseValue + gemBoost, false);
                    addStatBreakdownToLore(statToAdd, baseValue, gemBoost);

                    // Notificar al jugador
                    Component message = Component.text("Se ha agregado la estadística: ", NamedTextColor.GREEN)
                            .append(Component.text(statToAdd.getDisplayName(AuraSkillsApi.get().getMessageManager().getDefaultLanguage()))
                                    .color(ItemUtil.getColorOfStat(statToAdd)))
                            .append(Component.text(" +" + baseValue)
                                    .color(ItemUtil.getColorOfStat(statToAdd)));
                    player.sendMessage(ItemConfig.PLUGIN_PREFIX.append(message));
                } else {
                    // Si la stat no está presente en absoluto, agregarla con un valor base
                    int baseValue = StatValueGenerator.generateValueForStat(getRollQuality(), statProvider.isThisStatGauss(statToAdd));
                    System.out.println("[ItemRarity] Valor base generado: " + baseValue);

                    // Guardar el valor base en NBT
                    ItemMeta meta = getItemMeta();
                    container = meta.getPersistentDataContainer();
                    container.set(new NamespacedKey(PLUGIN, BASE_STAT_VALUE_PREFIX + statToAdd.name()), PersistentDataType.INTEGER, baseValue);
                    setItemMeta(meta);

                    // Aplicar el valor base
                    addModifier(statToAdd, baseValue, true);

                    // Notificar al jugador
                    Component message = Component.text("Se ha agregado la estadística: ", NamedTextColor.GREEN)
                            .append(Component.text(statToAdd.getDisplayName(AuraSkillsApi.get().getMessageManager().getDefaultLanguage()))
                                    .color(ItemUtil.getColorOfStat(statToAdd)))
                            .append(Component.text(" +" + baseValue)
                                    .color(ItemUtil.getColorOfStat(statToAdd)));
                    player.sendMessage(ItemConfig.BLESSING_BALL_PREFIX.append(message));
                }

                // Actualizar el lore del ítem
                setLore();
                updateLoreWithSockets();
                System.out.println("[ItemRarity] Stat agregada exitosamente: " + statToAdd.name());
            } catch (Exception e) {
                // Manejar cualquier excepción inesperada
                System.err.println("[ItemRarity] Error al agregar una stat aleatoria al ítem:");

                player.sendMessage(ItemConfig.BLESSING_BALL_PREFIX
                        .append(Component.text("Ocurrió un error al agregar la estadística. Por favor, contacta a un administrador.")
                                .color(NamedTextColor.RED)));
                throw e;
            }
        }
        return true;
    }
    private int getNativeStatsCount() {
        int count = 0;
        PersistentDataContainer container = getItemMeta().getPersistentDataContainer();

        for (StatModifier modifier : getStatModifiers()) {
            String statName = modifier.type().name();
            // Obtener el valor base de la stat
            int baseValue = container.getOrDefault(new NamespacedKey(PLUGIN, BASE_STAT_VALUE_PREFIX + statName), PersistentDataType.INTEGER, 0);

            // Obtener el boost de gema de la stat
            int gemBoost = container.getOrDefault(new NamespacedKey(PLUGIN, GEM_BOOST_PREFIX + statName), PersistentDataType.INTEGER, 0);

            // Calcular el valor base real (restando el gemBoost al StatModifier)
            double statModifierValue = modifier.value();
            double actualBaseValue = statModifierValue - gemBoost;

            // Determinar si la stat es nativa
            boolean isNative = actualBaseValue > 0; // Es nativa si tiene un valor base real

            // Logging detallado
            System.out.println("[ItemRarity] Verificando stat: " + statName +
                    ", statModifierValue: " + statModifierValue +
                    ", baseValue: " + baseValue +
                    ", gemBoost: " + gemBoost +
                    ", actualBaseValue: " + actualBaseValue +
                    ", isNative: " + isNative);

            // Si la stat es nativa, incrementar el contador
            if (isNative) {
                count++;
            }
        }

        System.out.println("[ItemRarity] Número de stats nativas: " + count);
        return count;
    }

    private void sendCompatibleGemsForItemType(Player player) {
        List<Component> availableStatsComponents = new ArrayList<>();
        // Construir la lista de componentes
        ItemUtil.getStatProvider(this)
                .getAvailableStats()
                .forEach(stat -> availableStatsComponents.add(
                        Component.text(stat.getDisplayName(AuraSkillsApi.get().getMessageManager().getDefaultLanguage()))
                                .color(ItemUtil.getColorOfStat(stat))
                ));
        // Mostrar gemas compatibles
        Component baseMessage = Component.text("El tipo de objeto no es compatible con la gema, intenta con: ")
                .color(NamedTextColor.RED);
        for (int i = 0; i < availableStatsComponents.size(); i++) {
            baseMessage = baseMessage.append(availableStatsComponents.get(i));
            if (i < availableStatsComponents.size() - 1) {
                baseMessage = baseMessage.append(Component.text(", ").color(NamedTextColor.GRAY));
            }
        }
        player.sendMessage(ItemConfig.GEMSTONE_PREFIX.append(baseMessage));
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

}