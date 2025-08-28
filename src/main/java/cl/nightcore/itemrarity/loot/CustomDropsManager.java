package cl.nightcore.itemrarity.loot;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.listener.UnifiedExperienceManager;
import cl.nightcore.itemrarity.loot.chest.ChestDropConfig;
import cl.nightcore.itemrarity.loot.chest.ChestDrops;
import cl.nightcore.itemrarity.loot.mob.ConditionalDropConfig;
import cl.nightcore.itemrarity.loot.mob.MobDrops;
import cl.nightcore.itemrarity.util.MobUtil;
import cl.nightcore.mythicProjectiles.boss.BossDifficulty;
import cl.nightcore.mythicProjectiles.boss.WorldBoss;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.DecoratedPotInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootTable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class CustomDropsManager implements Listener {

    private final Random random = new Random();

    private final List<ChestDropConfig> chestDrops = new ArrayList<>();
    private final Map<String, List<ChestDropConfig>> specificChestDrops = new HashMap<>();

    private final List<ConditionalDropConfig> conditionalDrops = new ArrayList<>();

    private final Set<UUID> processedEntities = ConcurrentHashMap.newKeySet();
    private static final long CLEANUP_DELAY_TICKS = 100L; // 5 segundos


    private static final int MIN_ITEMS_FOR_BUNDLE = 3; // Mínimo de items para crear bundle
    private static final int MAX_ITEMS_PER_BUNDLE = 64; // Máximo de items por bundle
    private static final boolean ENABLE_BUNDLES_FOR_BOSSES = true;
    private static final boolean ENABLE_BUNDLES_FOR_NORMAL_MOBS = false;




    public CustomDropsManager() {
        loadChestDrops();
        loadConditionalMobDrops();
    }


    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (!(event.getDamager() instanceof Player player)) return;

        int mobLevel = MobUtil.getLevel(entity);
        if (mobLevel == 0) return;

        double damage = event.getFinalDamage();
        DamageTracker.recordDamage(entity, player, damage);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();

        if (killer == null) return;

        UUID entityId = entity.getUniqueId();

        synchronized (processedEntities) {
            if (!processedEntities.add(entityId)) {
                ItemRarity.PLUGIN.getLogger().warning(String.format(
                        "[DUPLICATE-PREVENTED] Entity %s (UUID: %s) already processed - skipping",
                        entity.getType().name(),
                        entityId.toString().substring(0, 8) + "..."
                ));
                return; // Ya procesado, salir inmediatamente
            }

            // NUEVO: Logging para confirmar que se agregó
            ItemRarity.PLUGIN.getLogger().info(String.format(
                    "[ENTITY-REGISTERED] %s %s (UUID: %s) registered for processing",
                    WorldBoss.isBoss(entity) ? "Boss" : "Mob",
                    entity.getType().name(),
                    entityId.toString().substring(0, 8) + "..."
            ));
        }

        int mobLevel = MobUtil.getLevel(entity);
        boolean isBoss = WorldBoss.isBoss(entity);
        BossDifficulty bossDifficulty = WorldBoss.getBossDifficulty(entity);

        // IMPROVED: Logging más detallado para debugging
        if (isBoss) {
            ItemRarity.PLUGIN.getLogger().info(String.format(
                    "[DROP-PROCESSING] Starting drop processing for %s %s (Level %d, Difficulty: %s, UUID: %s)",
                    "Boss",
                    entity.getType().name(),
                    mobLevel,
                    bossDifficulty != null ? bossDifficulty.name() : "None",
                    entityId.toString().substring(0, 8) + "..."
            ));
        }

        DamageTracker.forceProcessEntity(entity)
                .thenAccept(lootReceivers -> {
                    //SAFETY CHECK: Verificar que aún estamos procesando esta entidad
                    synchronized (processedEntities) {
                        if (!processedEntities.contains(entityId)) {
                            ItemRarity.PLUGIN.getLogger().warning(String.format(
                                    "[ASYNC-CANCELLED] Entity %s was removed during async processing - skipping",
                                    entityId.toString().substring(0, 8) + "..."
                            ));
                            return;
                        }
                    }

                    Set<Player> eligiblePlayers = new HashSet<>();

                    if (lootReceivers.isEmpty()) {
                        if (isBoss) {
                            eligiblePlayers.addAll(PartyManager.getPartyMembersForBoss(killer));
                        } else {
                            eligiblePlayers.addAll(PartyManager.getPartyMembersForNormalMob(killer));
                        }
                    } else {
                        for (var receiver : lootReceivers) {
                            Player damageDealer = Bukkit.getPlayer(receiver.playerId());
                            if (damageDealer != null && damageDealer.isOnline()) {
                                if (isBoss) {
                                    eligiblePlayers.addAll(PartyManager.getPartyMembersForBoss(damageDealer));
                                } else {
                                    eligiblePlayers.addAll(PartyManager.getPartyMembersForNormalMob(damageDealer));
                                }
                            }
                        }
                    }

                    //NUEVO: Filtrar jugadores por distancia
                    Location deathLocation = entity.getLocation();
                    Set<Player> playersInRange = PartyManager.filterPlayersByDistance(eligiblePlayers, deathLocation);

                    // Logging de jugadores filtrados por distancia
                    if (UnifiedExperienceManager.isDistanceCheckEnabled() && playersInRange.size() < eligiblePlayers.size()) {
                        Set<Player> playersOutOfRange = new HashSet<>(eligiblePlayers);
                        playersOutOfRange.removeAll(playersInRange);

                        ItemRarity.PLUGIN.getLogger().fine(String.format(
                                "[DROPS] %s %s: %d players filtered out by distance (>%.1f blocks): %s",
                                isBoss ? "Boss" : "Mob",
                                entity.getType().name(),
                                playersOutOfRange.size(),
                                UnifiedExperienceManager.getMaxRewardDistance(),
                                playersOutOfRange.stream().map(Player::getName).collect(Collectors.joining(", "))
                        ));
                    }

                    // Procesar drops en el hilo principal
                    if (!playersInRange.isEmpty()) {
                        Bukkit.getScheduler().runTask(ItemRarity.PLUGIN, () -> {
                            // TRIPLE-CHECK: Verificar una última vez antes de procesar
                            if (processedEntities.contains(entityId)) {
                                processDropsForPlayers(playersInRange, entity, mobLevel, isBoss, bossDifficulty, entityId, deathLocation);

                                //  NUEVO: Programar limpieza con delay de seguridad
                                scheduleEntityCleanup(entityId, entity.getType().name());
                            } else {
                                ItemRarity.PLUGIN.getLogger().warning(String.format(
                                        "Entity %s was removed during final processing check - drops cancelled",
                                        entityId.toString().substring(0, 8) + "..."
                                ));
                            }
                        });
                    } else {
                        if (isBoss) {
                            ItemRarity.PLUGIN.getLogger().info(String.format(
                                    "[DROP-PROCESSING] No players in range for %s %s (distance check: %s)",
                                    "Boss",
                                    entity.getType().name(),
                                    UnifiedExperienceManager.isDistanceCheckEnabled() ? "enabled" : "disabled"
                            ));
                        }

                        // NUEVO: Limpiar también cuando no hay players en rango
                        scheduleEntityCleanup(entityId, entity.getType().name());
                    }
                })
                .exceptionally(throwable -> {
                    ItemRarity.PLUGIN.getLogger().severe("Error processing drops with damage tracking: " + throwable.getMessage());

                    // IMPROVED: Fallback también respeta el sistema de prevención de duplicados y distancia
                    if (processedEntities.contains(entityId)) {
                        Set<Player> fallbackPlayers;
                        if (isBoss) {
                            fallbackPlayers = PartyManager.getPartyMembersForBoss(killer);
                        } else {
                            fallbackPlayers = PartyManager.getPartyMembersForNormalMob(killer);
                        }

                        // NUEVO: Filtrar fallback también por distancia
                        Location deathLocation = entity.getLocation();
                        Set<Player> fallbackInRange = PartyManager.filterPlayersByDistance(fallbackPlayers, deathLocation);

                        Bukkit.getScheduler().runTask(ItemRarity.PLUGIN, () -> {
                            if (processedEntities.contains(entityId)) {
                                processDropsForPlayers(fallbackInRange, entity, mobLevel, isBoss, bossDifficulty, entityId, deathLocation);
                            }

                            // NUEVO: Limpiar también en caso de error/fallback
                            scheduleEntityCleanup(entityId, entity.getType().name());
                        });
                    }

                    return null;
                });
    }

    // MÉTODO DE APOYO: Programar limpieza con delay
    private void scheduleEntityCleanup(UUID entityId, String entityType) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(ItemRarity.PLUGIN, () -> {
            boolean removed = processedEntities.remove(entityId);

            if (removed) {
                ItemRarity.PLUGIN.getLogger().fine(String.format(
                        "[CLEANUP] Removed %s (UUID: %s) from processed entities after %d seconds",
                        entityType,
                        entityId.toString().substring(0, 8) + "...",
                        CLEANUP_DELAY_TICKS / 20
                ));
            }
        }, CLEANUP_DELAY_TICKS);
    }


    // UPDATED: Añadido parámetro deathLocation para verificación de distancia
    private void processDropsForPlayers(Set<Player> eligiblePlayers, LivingEntity entity, int mobLevel,
                                        boolean isBoss, BossDifficulty bossDifficulty, UUID entityId, Location deathLocation) {

        if (isBoss) {
            ItemRarity.PLUGIN.getLogger().info(String.format(
                    "[DROP-EXECUTION] Processing drops for %s %s (Level %d) - %d players in range: %s",
                    "Boss",
                    entity.getType().name(),
                    mobLevel,
                    eligiblePlayers.size(),
                    eligiblePlayers.stream().map(Player::getName).collect(Collectors.joining(", "))
            ));
        }

        int totalDropsProcessed = 0;

        for (Player player : eligiblePlayers) {
            // NUEVO: Verificación adicional de distancia por si acaso
            if (UnifiedExperienceManager.isDistanceCheckEnabled() &&
                    !PartyManager.isWithinRewardDistance(player, deathLocation)) {

                ItemRarity.PLUGIN.getLogger().fine(String.format(
                        "Player %s moved out of range during drop processing - skipping",
                        player.getName()
                ));
                continue;
            }

            double levelPenalty = LevelGapManager.getLevelPenaltyMultiplier(player, mobLevel);

            if (levelPenalty != 1.0){
                player.sendMessage("§cMucha diferencia de nivel con el jefe, % de drops * "+levelPenalty);

            }

            if (levelPenalty <= 0) continue;

            // Aplicar bonus de party si está en una (solo para bosses)
            double partyBonus = isBoss ? PartyManager.getPartyExpBonus(player.getUniqueId()) : 1.0;
            double finalMultiplier = levelPenalty * partyBonus;

            // PROCESAMIENTO INDIVIDUAL: Drops condicionales para cada jugador
            List<ItemStack> playerDrops = processConditionalDropsForPlayer(
                    entity, mobLevel, isBoss, bossDifficulty, finalMultiplier);

            totalDropsProcessed += playerDrops.size();

            // ENTREGA SEGURA: Inventario primero, luego dropear naturalmente
            deliverItemsToPlayer(player, playerDrops);

            // Notificación de drops mejorados por party (solo bosses)
            if (isBoss && partyBonus > 1.0) {
                player.sendMessage(String.format("§e+%.0f%% drops por estar en party!", (partyBonus - 1.0) * 100));
            }

            // NUEVO: Notificar verificación de distancia si está habilitada
            if (UnifiedExperienceManager.isDistanceCheckEnabled() && !playerDrops.isEmpty()) {
                double distance = player.getLocation().distance(deathLocation);
                ItemRarity.PLUGIN.getLogger().fine(String.format(
                        "%s received %d drops at distance %.1f blocks (max: %.1f)",
                        player.getName(), playerDrops.size(), distance,
                        UnifiedExperienceManager.getMaxRewardDistance()
                ));
            }
        }

        // COMPLETION LOG
        if (isBoss) {
            ItemRarity.PLUGIN.getLogger().info(String.format(
                    "[DROP-COMPLETED] %s %s (UUID: %s) - Total items processed: %d, Distance check: %s",
                    "Boss",
                    entity.getType().name(),
                    entityId.toString().substring(0, 8) + "...",
                    totalDropsProcessed,
                    UnifiedExperienceManager.isDistanceCheckEnabled() ? "enabled" : "disabled"
            ));
        }
    }


    /**
     *  Procesa drops condicionales para UN JUGADOR específico
     * Retorna una lista de items que el jugador debe recibir
     */
    private List<ItemStack> processConditionalDropsForPlayer(LivingEntity entity, int mobLevel, boolean isBoss,
                                                             BossDifficulty bossDifficulty, double multiplier) {
        List<ItemStack> playerDrops = new ArrayList<>();

        List<ConditionalDropConfig> validConditionalDrops = getValidConditionalDrops(
                entity.getType(), mobLevel, isBoss, bossDifficulty);

        for (ConditionalDropConfig dropConfig : validConditionalDrops) {
            double finalChance = dropConfig.getChance() * multiplier;

            if (random.nextDouble() < finalChance) {
                ItemStack itemToDrop = dropConfig.getItem().clone();
                playerDrops.add(itemToDrop);

                if (isBoss) {
                    ItemRarity.PLUGIN.getLogger().fine(String.format(
                            "Conditional drop generated: %s (Level: %d, Boss: %s, Difficulty: %s) - %s x%d (Final Multiplier: %.2f, Chance: %.4f)",
                            entity.getType().name(),
                            mobLevel,
                            "Yes",
                            bossDifficulty != null ? bossDifficulty.name() : "None",
                            itemToDrop.getType().name(),
                            itemToDrop.getAmount(),
                            multiplier,
                            finalChance
                    ));
                }
            }
        }

        return playerDrops;
    }

    /**
     *  Entrega items al jugador - inventario primero, luego drop natural
     */
    private void deliverItemsToPlayer(Player player, List<ItemStack> items) {
        if (items.isEmpty()) {
            player.sendMessage(Component.text("No hubo suerte con los drops, prueba a matar niveles mas altos", NamedTextColor.RED));
            return;
        }

        List<ItemStack> itemsReceivedInInventory = new ArrayList<>();
        List<ItemStack> itemsDroppedToFloor = new ArrayList<>();
        List<ItemStack> remainingItemsForBundle = new ArrayList<>();

        for (ItemStack item : items) {
            // Intentar agregar al inventario del jugador
            HashMap<Integer, ItemStack> leftOver = player.getInventory().addItem(item);

            if (leftOver.isEmpty()) {
                // Item agregado completamente al inventario
                itemsReceivedInInventory.add(item);
            } else {
                // Inventario lleno - separar lo que cabe y lo que no
                int addedAmount = item.getAmount() - leftOver.values().stream().mapToInt(ItemStack::getAmount).sum();

                if (addedAmount > 0) {
                    ItemStack addedItem = item.clone();
                    addedItem.setAmount(addedAmount);
                    itemsReceivedInInventory.add(addedItem);
                }

                // Los items restantes van para el bundle
                remainingItemsForBundle.addAll(leftOver.values());
            }
        }

        // Notificar items recibidos en el inventario (formato estándar)
        for (ItemStack item : itemsReceivedInInventory) {
            player.sendMessage(Component.text("¡")
                    .append(item.displayName())
                    .append(Component.text(" x" + item.getAmount() + " Recibido!", TextColor.color(0x82FF70))));
        }

        // Si hay items restantes, crear bundle(s) con delay de 3 segundos
        if (!remainingItemsForBundle.isEmpty()) {
            // Ejecutar de forma síncrona en el hilo principal del servidor
            CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS).execute(() -> {
                Bukkit.getScheduler().runTask(ItemRarity.PLUGIN, () -> {
                    createAndDropBundles(player, remainingItemsForBundle);
                });
            });

            // Notificar que los items restantes llegarán en una bolsa
            int totalRemainingItems = remainingItemsForBundle.stream().mapToInt(ItemStack::getAmount).sum();
            player.sendMessage(Component.text("⚠ Recibirás un saco con " + totalRemainingItems + " items, asegura tener espacio ⚠", TextColor.color(0xFF4A00)));
        }
    }

    private void createAndDropBundles(Player player, List<ItemStack> items) {
        List<List<ItemStack>> bundles = new ArrayList<>();
        List<ItemStack> currentBundle = new ArrayList<>();
        int currentBundleSlots = 0; // Número de "slots" ocupados en el bundle actual

        for (ItemStack item : items) {
            // Calcular cuántos slots necesita este item
            int maxStackSize = item.getMaxStackSize();
            int slotsNeeded;

            if (maxStackSize == 1) {
                // Item no stackeable (herramientas, armaduras, etc.)
                slotsNeeded = item.getAmount(); // Cada item ocupa 1 slot
            } else {
                // Item stackeable
                slotsNeeded = (int) Math.ceil((double) item.getAmount() / maxStackSize);
            }

            // Si agregar este item excede los 64 slots del bundle
            if (currentBundleSlots + slotsNeeded > 64) {
                // Guardar bundle actual si no está vacío
                if (!currentBundle.isEmpty()) {
                    bundles.add(new ArrayList<>(currentBundle));
                    currentBundle.clear();
                    currentBundleSlots = 0;
                }

                // Si el item solo no cabe en un bundle, dividirlo
                ItemStack remainingItem = item.clone();
                while (remainingItem.getAmount() > 0) {
                    if (maxStackSize == 1) {
                        // Items no stackeables: máximo 64 por bundle
                        int itemsToAdd = Math.min(remainingItem.getAmount(), 64);
                        ItemStack bundleItem = remainingItem.clone();
                        bundleItem.setAmount(itemsToAdd);
                        bundles.add(List.of(bundleItem));
                        remainingItem.setAmount(remainingItem.getAmount() - itemsToAdd);
                    } else {
                        // Items stackeables: calcular cuántos stacks completos caben
                        int maxStacksInBundle = 64;
                        int totalItemsInBundle = Math.min(remainingItem.getAmount(), maxStacksInBundle * maxStackSize);

                        List<ItemStack> bundleItems = new ArrayList<>();
                        int itemsLeft = totalItemsInBundle;

                        while (itemsLeft > 0) {
                            int stackAmount = Math.min(itemsLeft, maxStackSize);
                            ItemStack stackItem = remainingItem.clone();
                            stackItem.setAmount(stackAmount);
                            bundleItems.add(stackItem);
                            itemsLeft -= stackAmount;
                        }

                        bundles.add(bundleItems);
                        remainingItem.setAmount(remainingItem.getAmount() - totalItemsInBundle);
                    }
                }
            } else {
                // El item cabe en el bundle actual
                currentBundle.add(item);
                currentBundleSlots += slotsNeeded;
            }
        }

        // Agregar el último bundle si no está vacío
        if (!currentBundle.isEmpty()) {
            bundles.add(currentBundle);
        }

        // Contadores para el mensaje final
        int bundlesAddedToInventory = 0;
        int bundlesDroppedToFloor = 0;

        // Crear y entregar cada bundle
        for (List<ItemStack> bundleItems : bundles) {
            ItemStack bundle = createLootBundle(bundleItems);

            HashMap<Integer, ItemStack> leftOver = player.getInventory().addItem(bundle);

            if (leftOver.isEmpty()) {
                bundlesAddedToInventory++;
            } else {
                player.getWorld().dropItemNaturally(player.getLocation(), bundle);
                bundlesDroppedToFloor++;
            }
        }

        // Enviar mensaje único consolidado
        int totalBundles = bundlesAddedToInventory + bundlesDroppedToFloor;
        if (bundlesDroppedToFloor > 0) {
            player.sendMessage(Component.text("¡Bolsa de loot x" + totalBundles + " Recibido (" + bundlesDroppedToFloor + " en el suelo por inventario lleno)!", TextColor.color(0x82FF70)));
        } else {
            player.sendMessage(Component.text("¡Bolsa de loot x" + totalBundles + " Recibido!", TextColor.color(0x82FF70)));
        }
    }

    private ItemStack createLootBundle(List<ItemStack> items) {
        ItemStack bundle = new ItemStack(Material.BUNDLE);
        ItemMeta meta = bundle.getItemMeta();
        // Crear gradiente de café medio-oscuro a café medio-claro
        Component displayName = Component.text("Bolsa de loot")
                .color(TextColor.fromHexString("#8B4513"));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Contenido: ",NamedTextColor.GRAY).decoration(TextDecoration.ITALIC,false));
        for (ItemStack item : items){
            lore.add(item.displayName().append(Component.text(" x"+ item.getAmount(),TextColor.color(0x82FF70)).decoration(TextDecoration.ITALIC,false)));
        }
        meta.lore(lore);
        meta.displayName(displayName);
        bundle.setItemMeta(meta);

        // Si tu servidor soporta bundles, agregar los items al bundle
        if (meta instanceof BundleMeta bundleMeta) {
            for (ItemStack item : items) {
                bundleMeta.addItem(item);
            }
            bundle.setItemMeta(bundleMeta);
        }

        return bundle;
    }

    private void loadConditionalMobDrops() {
        conditionalDrops.addAll(MobDrops.instance().getMobDrops());
    }

    private void loadChestDrops(){
        chestDrops.addAll(ChestDrops.instance().getChestDrops());
        specificChestDrops.putAll(ChestDrops.instance().getSpecificChestDrops());
    }


    public List<ConditionalDropConfig> getValidConditionalDrops(EntityType entityType, int mobLevel,
                                                                boolean isBoss, BossDifficulty bossDifficulty) {
        return conditionalDrops.stream()
                .filter(drop -> drop.meetsConditions(entityType, mobLevel, isBoss, bossDifficulty))
                .toList();
    }

    private int getRandomAmount(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    @EventHandler
    public void onLootGenerate(LootGenerateEvent event) {

        if(event.getInventoryHolder() instanceof DecoratedPotInventory invholder){
            return;
        }

        LootTable lootTable = event.getLootTable();

        List<ItemStack> loot = event.getLoot();

        for (ChestDropConfig chestDropConfig : chestDrops) {
            if (random.nextDouble() < chestDropConfig.chance()) {
                ItemStack itemToAdd = chestDropConfig.item().clone();
                int amount = getRandomAmount(chestDropConfig.minAmount(), chestDropConfig.maxAmount());
                itemToAdd.setAmount(amount);
                loot.add(itemToAdd);
            }
        }

        List<ChestDropConfig> specificDrops = specificChestDrops.get(lootTable.getKey().toString());
        if (specificDrops != null) {
            for (ChestDropConfig chestDropConfig : specificDrops) {
                if (random.nextDouble() < chestDropConfig.chance()) {
                    ItemStack itemToAdd = chestDropConfig.item().clone();
                    int amount = getRandomAmount(chestDropConfig.minAmount(), chestDropConfig.maxAmount());
                    itemToAdd.setAmount(amount);
                    loot.add(itemToAdd);
                }
            }
        }
    }
}