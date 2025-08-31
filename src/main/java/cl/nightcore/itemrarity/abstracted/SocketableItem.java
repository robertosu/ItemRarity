package cl.nightcore.itemrarity.abstracted;

import cl.nightcore.itemrarity.config.CombinedStats;
import cl.nightcore.itemrarity.config.ItemConfig;
import cl.nightcore.itemrarity.item.gem.GemManager;
import cl.nightcore.itemrarity.item.gem.GemObject;
import cl.nightcore.itemrarity.item.gem.SocketStone;
import cl.nightcore.itemrarity.model.GemModel;
import cl.nightcore.itemrarity.model.GemRemoverModel;
import cl.nightcore.itemrarity.rollquality.StatValueGenerator;
import cl.nightcore.itemrarity.util.ItemUtil;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.AuraSkillsBukkit;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.StatModifier;
import dev.aurelium.auraskills.api.util.AuraSkillsModifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
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

import static cl.nightcore.itemrarity.ItemRarity.AURA_LOCALE;
import static cl.nightcore.itemrarity.ItemRarity.PLUGIN;
import static cl.nightcore.itemrarity.util.ItemUtil.RANDOM;


public class SocketableItem extends IdentifiedItem {

    protected static final int MAX_POSSIBLE_SOCKETS = 3;

    private static final String MAX_SOCKETS_KEY = "item_max_sockets";
    private static final String AVAILABLE_SOCKETS_KEY = "item_available_sockets";

    private static final NamespacedKey MAX_SOCKETS_KEY_NS = new NamespacedKey(PLUGIN, MAX_SOCKETS_KEY);
    private static final GemManager gemManager = new GemManager();

    public SocketableItem(ItemStack item) {
        super(item);
    }

    public void initializeSocketData() {
        ItemMeta meta = getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        int sockets = RANDOM.nextInt(MAX_POSSIBLE_SOCKETS) + 1;

        container.set(MAX_SOCKETS_KEY_NS, PersistentDataType.INTEGER, sockets);

        setItemMeta(meta);
    }

    public int installGem(GemModel gem, Player player) {
        // Verificar si hay espacios disponibles para gemas
        if (!hasAvailableSockets()) {
            player.sendMessage(ItemConfig.GEMSTONE_PREFIX.append(
                    Component.text("Este objeto no tiene espacios disponibles para gemas.")
                            .color(NamedTextColor.RED)));
            return 1;
        }
        // Verificar si ya hay una gema del mismo tipo
        if (hasGemWithStat(gem.getStat())) {
            player.sendMessage(ItemConfig.GEMSTONE_PREFIX.append(
                    Component.text("El objeto ya tiene una gema de este tipo.").color(NamedTextColor.RED)));
            return 1;
        }
        // Verificar si la gema es compatible con las stats posibles para un item
        if (!gem.isCompatible(statProvider)) {
            sendCompatibleGemsForItemType(player);
            return 1;
        }
        // Verificar si la gema se rompe durante la instalación (40% de probabilidad de fallo)
        if (gemBreaks(60)) {
            player.playSound(player, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 1.0f, 0.4f);

            player.sendMessage(ItemConfig.GEMSTONE_PREFIX.append(
                    Component.text("¡La gema se rompió durante la instalación!")
                            .color(NamedTextColor.RED)));
            return 2;
        }
        // Si todas las verificaciones pasan, proceder a instalar la gema
        Stat stat = gem.getStat();

        // Aplicar la gema usando el nuevo método de la API
        this.setItemMeta(AuraSkillsBukkit.get()
                .getItemManager()
                .addStatModifier(this, modifierType, stat, gem.getValue() , AuraSkillsModifier.Operation.ADD,GEM_STATMODIFIER,false).getItemMeta());
        this.setNewLore();

        player.playSound(player, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 1.0f, 2.0f);
        player.sendMessage(ItemConfig.GEMSTONE_PREFIX.append(
                Component.text("¡Gema instalada con éxito!").color(NamedTextColor.GREEN)));

        return 0;
    }

    public boolean extractAllGems(Player player, GemRemoverModel gemRemover) {
        var installedGems = getInstalledGems();
        Stat stat;
        int gemLevel;

        if (installedGems.isEmpty()) {
            player.sendMessage(ItemConfig.GEMSTONE_PREFIX.append(
                    Component.text("Este objeto no tiene gemas instaladas.").color(NamedTextColor.GRAY)));
            return false;
        }
        // Recolectar las gemas extraídas
        List<GemObject> extractedGems = new ArrayList<>();


        for (StatModifier statModifier : installedGems){
            stat = statModifier.stat();
            gemLevel = calculateGemLevel((int)statModifier.value());
            // Verificar si la gema se rompe
            if (gemBreaks(gemRemover.getPercentage())) {
                player.playSound(player, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 1.0f, 0.4f);
                player.sendMessage(ItemConfig.GEMSTONE_PREFIX.append(Component.text("La gema de ")
                        .color(NamedTextColor.RED)
                        .append(Component.text(stat.getDisplayName(AURA_LOCALE)).color(ItemUtil.getColorOfStat(stat)))
                        .append(Component.text(" se rompió al extraer."))));
            } else {
                // Crear una instancia de la gema extraída
                player.playSound(player, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 1.0f, 2.0f);
                player.sendMessage(ItemConfig.GEMSTONE_PREFIX.append(Component.text("La gema de ")
                        .color(NamedTextColor.GREEN)
                        .append(Component.text(stat.getDisplayName(AURA_LOCALE)).color(ItemUtil.getColorOfStat(stat)))
                        .append(Component.text(" se extrajo correctamente."))));
                GemObject extractedGem = gemManager.createGem(1, gemLevel, stat.name());
                extractedGems.add(extractedGem);
            }
            // Remover el modificador de la gema
            removeStatModifierByName(stat, GEM_STATMODIFIER);

        }
        // SOLUCIÓN: En lugar de solo llamar setLore(), regenerar todo correctamente
        setNewLore();
        // Entregar las gemas al jugador
        for (GemObject gem : extractedGems) {
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(gem);
            if (!leftover.isEmpty()) {
                player.getWorld().dropItemNaturally(player.getLocation(), gem);
            }
        }
        return true;
    }


    public List<StatModifier> getInstalledGems() {
        try{
            var gemsmodifiers = AuraSkillsBukkit.get().getItemManager().getStatModifiersById(this, modifierType,GEM_STATMODIFIER);
            return gemsmodifiers;
        }catch (Exception e){
            throw new IllegalStateException("Error al obtener gemas de este items (AS API)");
        }
    }



    @Override
    protected int calculateGemValue(int level) {
        return 4 + (level - 1) * level / 2;
    }


    protected int calculateGemLevel(int value) {
        return switch (value) {
            case 4 -> 1;
            case 5 -> 2;
            case 7 -> 3;
            case 10 -> 4;
            case 14 -> 5;
            case 19 -> 6;
            default -> throw new IllegalStateException("Valor inválido de la gema");
        };
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
        double successchance = percentage / 100.0;
        return successchance < ThreadLocalRandom.current().nextDouble();
    }

    public boolean addRandomMissingStat(Player player) {
        // Verificar máximo de stats permitidas
        if (this.getMaxBonuses() != 5) {
            this.setMaxBonuses(5);
        }else{
            player.sendMessage(ItemConfig.BLESSING_BALL_PREFIX.append(
                    Component.text("El ítem ya tiene el máximo de estadísticas permitidas (6).")
                            .color(NamedTextColor.RED)));
            return false;
        }

        // Obtener stats nativas actuales
        List<StatModifier> nativeStats = AuraSkillsBukkit.get()
                .getItemManager()
                .getStatModifiersById(this, modifierType, NATIVE_STATMODIFIER);

        // Verificar límite de stats
        if (nativeStats.size() >= 6) {
            player.sendMessage(ItemConfig.BLESSING_BALL_PREFIX.append(
                    Component.text("El ítem ya tiene el máximo de estadísticas permitidas (6).")
                            .color(NamedTextColor.RED)));
            return false;
        }

        // Obtener stats disponibles y actuales
        List<Stat> availableStats = statProvider.getAvailableStats();
        List<String> currentNativeStats = nativeStats.stream()
                .map(modifier -> modifier.type().name())
                .toList();

        // Encontrar stats faltantes
        List<Stat> missingStats = availableStats.stream()
                .filter(stat -> !currentNativeStats.contains(stat.name()))
                .toList();

        if (missingStats.isEmpty()) {
            player.sendMessage(
                    ItemConfig.BLESSING_BALL_PREFIX.append(Component.text("No hay estadísticas faltantes para agregar.")
                            .color(NamedTextColor.RED)));
            return false;
        }

        // Agregar stat aleatoria
        Stat statToAdd = missingStats.get(RANDOM.nextInt(missingStats.size()));
        int baseValue = StatValueGenerator.generateValueForStat(
                statProvider.isThisStatGauss(statToAdd)
        );
        // Aplicar modificador
        ItemStack modifiedItem = AuraSkillsBukkit.get()
                .getItemManager()
                .addStatModifier(this, modifierType, statToAdd, baseValue, AuraSkillsModifier.Operation.ADD, NATIVE_STATMODIFIER, true);

        modifiedItem.getType().getBlockTranslationKey();
        setItemMeta(modifiedItem.getItemMeta());

        // Notificar al jugador del éxito
        Component message = Component.text("Se ha agregado la estadística: ", NamedTextColor.GREEN)
                .append(Component.text(statToAdd.getDisplayName(
                                AuraSkillsApi.get().getMessageManager().getDefaultLanguage()))
                        .color(ItemUtil.getColorOfStat(statToAdd)))
                .append(Component.text(" +" + baseValue).color(ItemUtil.getColorOfStat(statToAdd)));
        player.sendMessage(ItemConfig.BLESSING_BALL_PREFIX.append(message));

        // Actualizar lore
        setItemMeta(modifiedItem.getItemMeta());

        setNewLore();

        return true;
    }

    private void sendCompatibleGemsForItemType(Player player) {
        List<Component> availableStatsComponents = new ArrayList<>();
        // Construir la lista de componentes
        statProvider
                .getAvailableStats()
                .forEach(stat -> availableStatsComponents.add(Component.text(stat.getDisplayName(
                                AuraSkillsApi.get().getMessageManager().getDefaultLanguage()))
                        .color(ItemUtil.getColorOfStat(stat))));
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

    public boolean addSocket(Player player){
        int maxSockets = getMaxSockets();
        if(maxSockets < MAX_POSSIBLE_SOCKETS){
            ItemMeta meta = getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(MAX_SOCKETS_KEY_NS, PersistentDataType.INTEGER, maxSockets + 1);
            setItemMeta(meta);
            this.setNewLore();
            player.playSound(player, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 1.0f, 0.4f);
            player.sendMessage(ItemConfig.GEMSTONE_PREFIX.append(
                    Component.text("Se añadió una ranura de gema a tu objeto.")
                            .color(SocketStone.getLoreColor())));
            return true;
        }
        player.sendMessage(ItemConfig.GEMSTONE_PREFIX.append(
                Component.text("El ítem ya tiene el máximo de ranuras de gemas permitidas (3).")
                        .color(NamedTextColor.RED)));
        return false;
    }



    public boolean hasGemWithStat(Stat stat) {
        var modifiers = AuraSkillsBukkit.get().getItemManager().getStatModifiersById(this, modifierType, GEM_STATMODIFIER);
        if (modifiers.isEmpty()){
            return false;
        }
        for (StatModifier modifier : modifiers){
            System.out.println(modifier.stat().name());

            if(modifier.stat().name().equals(stat.name())) return true;
        }
        return  false;
    }




    public int getMaxSockets() {
        return getItemMeta()
                .getPersistentDataContainer()
                .getOrDefault(MAX_SOCKETS_KEY_NS, PersistentDataType.INTEGER, 0);
    }

    public int getAvailableSockets() {
        var modifiers = AuraSkillsBukkit.get().getItemManager().getStatModifiersById(this,modifierType,GEM_STATMODIFIER);
        return (getMaxSockets() - modifiers.size());
    }

    public boolean hasAvailableSockets() {
        return getAvailableSockets() > 0;
    }

    @Override
    protected void setMonoliticStats(int level){

    }
}