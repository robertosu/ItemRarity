package cl.nightcore.itemrarity.loot;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.item.BlessingObject;
import cl.nightcore.itemrarity.item.IdentifyScroll;
import cl.nightcore.itemrarity.item.MagicObject;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;

import java.util.*;

public class CustomDropsManager implements Listener {

    private final Random random = new Random();
    private final List<DropConfig> globalDrops = new ArrayList<>();

    private final BlessingObject blessingObject = new BlessingObject(1, ItemRarity.plugin);
    private final IdentifyScroll identifyScroll = new IdentifyScroll(1, ItemRarity.plugin);
    private final MagicObject magicObject = new MagicObject(1, ItemRarity.plugin);

    private record DropConfig(ItemStack item, double chance, int minAmount, int maxAmount) {
    }

    private final Map<EntityType, List<DropConfig>> mobDrops = new HashMap<>();
    private final Map<String, List<DropConfig>> chestDrops = new HashMap<>();

    public CustomDropsManager() {
        loadDropConfigurations();
    }

    private void loadDropConfigurations() {
        // Configuración de drops de mobs
        addMobDrop(EntityType.ZOMBIE, blessingObject, 0.50, 1, 3);
        addMobDrop(EntityType.ZOMBIE, identifyScroll, 0.30, 1, 2);
        addMobDrop(EntityType.ZOMBIE, magicObject, 0.30, 1, 2);

        // Configuración de drops globales para todos los cofres
        addGlobalChestDrop(blessingObject, 0.30, 1, 4);
        addGlobalChestDrop(identifyScroll, 0.30, 1, 2);
        addGlobalChestDrop(magicObject, 0.30, 1, 2);

        // Drops específicos adicionales para ciertos cofres
        addChestDrop("minecraft:chests/simple_dungeon", blessingObject, 0.50, 1, 4);
    }

    public void addGlobalChestDrop(ItemStack item, double chance, int minAmount, int maxAmount) {
        globalDrops.add(new DropConfig(item, chance, minAmount, maxAmount));
    }

    public void addMobDrop(EntityType entityType, ItemStack item, double chance, int minAmount, int maxAmount) {
        DropConfig dropConfig = new DropConfig(item, chance, minAmount, maxAmount);
        mobDrops.computeIfAbsent(entityType, k -> new ArrayList<>()).add(dropConfig);
    }

    public void addChestDrop(String lootTableKey, ItemStack item, double chance, int minAmount, int maxAmount) {
        DropConfig dropConfig = new DropConfig(item, chance, minAmount, maxAmount);
        chestDrops.computeIfAbsent(lootTableKey, k -> new ArrayList<>()).add(dropConfig);
    }

    private int getRandomAmount(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        List<DropConfig> possibleDrops = mobDrops.get(entity.getType());

        if (possibleDrops != null) {
            Location location = entity.getLocation();

            for (DropConfig dropConfig : possibleDrops) {
                if (random.nextDouble() < dropConfig.chance) {
                    ItemStack itemToDrop = dropConfig.item.clone();
                    itemToDrop.setAmount(getRandomAmount(dropConfig.minAmount, dropConfig.maxAmount));
                    entity.getWorld().dropItemNaturally(location, itemToDrop);
                }
            }
        }
    }

    @EventHandler
    public void onLootGenerate(LootGenerateEvent event) {
        LootTable lootTable = event.getLootTable();
        String lootTableKey = lootTable.getKey().toString();
        List<ItemStack> loot = event.getLoot();

        // Procesar drops globales
        for (DropConfig dropConfig : globalDrops) {
            if (random.nextDouble() < dropConfig.chance) {
                ItemStack itemToAdd = dropConfig.item.clone();
                int amount = getRandomAmount(dropConfig.minAmount, dropConfig.maxAmount);
                itemToAdd.setAmount(amount);
                loot.add(itemToAdd);
            }
        }

        // Procesar drops específicos para esta loottable
        List<DropConfig> specificDrops = chestDrops.get(lootTableKey);
        if (specificDrops != null) {
            for (DropConfig dropConfig : specificDrops) {
                if (random.nextDouble() < dropConfig.chance) {
                    ItemStack itemToAdd = dropConfig.item.clone();
                    int amount = getRandomAmount(dropConfig.minAmount, dropConfig.maxAmount);
                    itemToAdd.setAmount(amount);
                    loot.add(itemToAdd);
                }
            }
        }
    }
}