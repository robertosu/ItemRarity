package cl.nightcore.itemrarity.loot;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.config.CombinedStats;
import cl.nightcore.itemrarity.item.BlessingBall;
import cl.nightcore.itemrarity.item.ExperienceMultiplier;
import cl.nightcore.itemrarity.item.ItemUpgrader;
import cl.nightcore.itemrarity.item.gem.GemManager;
import cl.nightcore.itemrarity.item.gem.GemObject;
import cl.nightcore.itemrarity.item.gem.GemRemover;
import cl.nightcore.itemrarity.item.gem.SocketStone;
import cl.nightcore.itemrarity.item.potion.PotionManager;
import cl.nightcore.itemrarity.item.potion.StatPotion;
import cl.nightcore.itemrarity.item.roller.BlessingObject;
import cl.nightcore.itemrarity.item.roller.IdentifyScroll;
import cl.nightcore.itemrarity.item.roller.MagicObject;
import cl.nightcore.itemrarity.item.roller.RedemptionObject;

import java.util.*;

public class Items {

    private static Items instance;
    private static final int MAX_GEM_LEVEL = 5;

    // Items existentes
    private final MagicObject magicObject = new MagicObject(1, ItemRarity.PLUGIN);
    private final BlessingObject blessingObject = new BlessingObject(1, ItemRarity.PLUGIN);
    private final IdentifyScroll identifyScroll = new IdentifyScroll(1, ItemRarity.PLUGIN);
    private final RedemptionObject redemptionObject = new RedemptionObject(1, ItemRarity.PLUGIN);


    private final BlessingBall blessingBall = new BlessingBall(1,ItemRarity.PLUGIN);

    private final SocketStone socketStone = new SocketStone(1,ItemRarity.PLUGIN);

    private final Map<String,GemRemover> allGemRemovers;

    // Multiplicadores de experiencia disponibles
    private static final int[] XP_MULTIPLIERS = {100, 200, 300, 400, 500};

    // Configuraciones de upgraders: [level, type]
    private static final int[][] UPGRADER_CONFIGS = {
            {1, 1}, // Forja Inestable nivel 1
            {2, 1}, // Forja Inestable nivel 2
            {1, 2}, // Forja Activa nivel 1
            {2, 2}, // Forja Activa nivel 2
            {1, 3}, // Forja Estable nivel 1
            {2, 3}  // Forja Estable nivel 2
    };

    private  static final int[] GEMREMOVER_LEVELS = {1,2,3};

    public Map<String, ExperienceMultiplier> getAllXpMultipliers() {
        return allXpMultipliers;
    }

    public Map<String, ItemUpgrader> getAllUpgraders() {
        return allUpgraders;
    }

    public Map<String, GemObject> getAllGems() {
        return allGems;
    }


    public Map<String, StatPotion> getAllPotions() {
        return allPotions;
    }

    public Map<String,GemRemover> getAllGemRemovers() {
        return allGemRemovers;
    }

    // Mapa con TODAS las pociones instanciadas
    // Clave: "STAT_LEVEL_DURATION" (ej: "STRENGTH_THREE_HIGH")
    private final Map<String, StatPotion> allPotions;

    // Mapa con TODAS las gemas pregeneradas
    // Clave: "STAT_LEVEL" (ej: "STRENGTH_3")
    private final Map<String, GemObject> allGems;

    // Mapa con TODOS los multiplicadores de experiencia
    // Clave: "XP_MULTIPLIER" (ej: "XP_200")
    private final Map<String, ExperienceMultiplier> allXpMultipliers;

    // Mapa con TODOS los upgraders
    // Clave: "UPGRADER_LEVEL_TYPE" (ej: "UPGRADER_1_2")
    private final Map<String, ItemUpgrader> allUpgraders;


    // Manager para crear gemas
    private final GemManager gemManager;

    private Items() {
        this.allPotions = new HashMap<>();
        this.allGems = new HashMap<>();

        this.allXpMultipliers = new HashMap<>();
        this.allUpgraders = new HashMap<>();

        this.allGemRemovers = new HashMap<>();

        this.gemManager = new GemManager();

        initializeAllPotions();
        initializeAllGems();
        initializeAllXpMultipliers();
        initializeAllUpgraders();
        initializeGemRemovers();
    }

    public static synchronized Items instance(){
        if(Items.instance != null){
            return instance;
        }
        instance = new Items();
        return instance;
    }

    private  void  initializeGemRemovers(){
        for (int level : GEMREMOVER_LEVELS){
            allGemRemovers.put(String.valueOf(level),new GemRemover(1,level));
        }
    }

    /**
     * Instancia TODAS las combinaciones posibles de pociones
     */
    private void initializeAllPotions() {
        System.out.println("Inicializando todas las pociones...");

        for (CombinedStats stat : CombinedStats.values()) {
            for (PotionManager.PotionLevel level : PotionManager.PotionLevel.values()) {
                for (PotionManager.PotionDuration duration : PotionManager.PotionDuration.values()) {

                    // Crear la poción
                    StatPotion potion = PotionManager.createPotionByLevel(stat, level, duration);

                    // Crear la clave única
                    String key = generatePotionKey(stat, level, duration);

                    // Almacenar en el mapa
                    allPotions.put(key, potion);
                }
            }
        }

        System.out.println("Total de pociones instanciadas: " + allPotions.size());
    }
    /**
     * Instancia TODAS las gemas desde nivel 1 hasta MAX_GEM_LEVEL para cada stat
     */
    private void initializeAllGems() {
        System.out.println("Inicializando todas las gemas...");

        for (CombinedStats stat : CombinedStats.values()) {
            for (int level = 1; level <= MAX_GEM_LEVEL; level++) {
                // Crear la gema
                GemObject gem = gemManager.createGem(1, level, stat.name());

                // Crear la clave única
                String key = generateGemKey(stat, level);

                // Almacenar en el mapa
                allGems.put(key, gem);
            }
        }

        System.out.println("Total de gemas instanciadas: " + allGems.size());
    }

    private void initializeAllXpMultipliers() {
        System.out.println("Inicializando todos los multiplicadores XP...");

        for (int multiplier : XP_MULTIPLIERS) {
            // Crear el multiplicador
            ExperienceMultiplier xpMultiplier = new ExperienceMultiplier(1, multiplier);

            // Crear la clave única
            String key = generateXpMultiplierKey(multiplier);

            // Almacenar en el mapa
            allXpMultipliers.put(key, xpMultiplier);
        }

        System.out.println("Total de multiplicadores XP instanciados: " + allXpMultipliers.size());
    }

    /**
     * Instancia TODOS los upgraders (forjas)
     */
    private void initializeAllUpgraders() {
        System.out.println("Inicializando todos los upgraders...");

        for (int[] config : UPGRADER_CONFIGS) {
            int level = config[0];
            int type = config[1];

            // Crear el upgrader
            ItemUpgrader upgrader = new ItemUpgrader(1, level, type);

            // Crear la clave única
            String key = generateUpgraderKey(level, type);

            // Almacenar en el mapa
            allUpgraders.put(key, upgrader);
        }

        System.out.println("Total de upgraders instanciados: " + allUpgraders.size());
    }

    /**
     * Genera la clave única para buscar multiplicadores XP
     */
    private String generateXpMultiplierKey(int multiplier) {
        return "XP_" + multiplier;
    }

    /**
     * Genera la clave única para buscar upgraders
     */
    private String generateUpgraderKey(int level, int type) {
        return "UPGRADER_" + level + "_" + type;
    }



    /**
     * Genera la clave única para buscar pociones
     */
    private String generatePotionKey(CombinedStats stat, PotionManager.PotionLevel level, PotionManager.PotionDuration duration) {
        return stat.name() + "_" + level.name() + "_" + duration.name();
    }

    /**
     * Genera la clave única para buscar gemas
     */
    private String generateGemKey(CombinedStats stat, int level) {
        return stat.name() + "_" + level;
    }

    // ========== MÉTODOS PARA POCIONES ==========

    /**
     * MÉTODO PRINCIPAL - Única interfaz para obtener pociones
     * @param stat La estadística deseada
     * @param level El nivel de la poción
     * @param duration La duración de la poción
     * @return La poción correspondiente, o null si no existe
     */
    public StatPotion getPotion(CombinedStats stat, PotionManager.PotionLevel level, PotionManager.PotionDuration duration) {
        String key = generatePotionKey(stat, level, duration);
        return allPotions.get(key);
    }

    // ========== MÉTODOS PARA GEMAS ==========

    /**
     * MÉTODO PRINCIPAL - Interfaz para obtener gemas pregeneradas
     * @param stat La estadística de la gema
     * @param level El nivel de la gema (1-4)
     * @return La gema correspondiente, o null si no existe o el nivel es inválido
     */
    public GemObject getGem(CombinedStats stat, int level) {
        if (level < 1 || level > MAX_GEM_LEVEL) {
            return null;
        }

        String key = generateGemKey(stat, level);
        return allGems.get(key);
    }

    // ========== MÉTODOS PARA MULTIPLICADORES XP ==========

    /**
     * MÉttoDO PRINCIPAL - Interfaz para obtener multiplicadores de experiencia
     * @param multiplier El valor del multiplicador (100, 200, 300, 400, 500)
     * @return El multiplicador correspondiente, o null si no existe
     */
    public ExperienceMultiplier getXpMultiplier(int multiplier) {
        String key = generateXpMultiplierKey(multiplier);
        return allXpMultipliers.get(key);
    }

    // ========== MÉTODOS PARA UPGRADERS ==========

    /**
     * MÉttODO PRINCIPAL - Interfaz para obtener upgraders (forjas)
     * @param level El nivel del upgrader (1 o 2)
     * @param type El tipo del upgrader (1=Inestable, 2=Activa, 3=Estable)
     * @return El upgrader correspondiente, o null si no existe
     */
    public ItemUpgrader getUpgrader(int level, int type) {
        String key = generateUpgraderKey(level, type);
        return allUpgraders.get(key);
    }


    public GemRemover getGemremover(int level){
        return allGemRemovers.get(String.valueOf(level));
    }

    /**
     * Sobrecarga para obtener gema por nombre de stat como String
     * @param statName El nombre de la estadística como String
     * @param level El nivel de la gema (1-4)
     * @return La gema correspondiente, o null si no existe
     */
    public GemObject getGem(String statName, int level) {
        try {
            CombinedStats stat = CombinedStats.valueOf(statName.toUpperCase());
            return getGem(stat, level);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }


    // ========== GETTERS PARA ITEMS EXISTENTES ==========

    public BlessingObject getBlessingObject() {
        return blessingObject;
    }

    public IdentifyScroll getIdentifyScroll() {
        return identifyScroll;
    }

    public MagicObject getMagicObject() {
        return magicObject;
    }

    public RedemptionObject getRedemptionObject() {
        return redemptionObject;
    }

    public BlessingBall getBlessingBall() { return blessingBall; }

    public SocketStone getSocketStone() { return socketStone; }


    // ========== MÉTODOS DE INFORMACIÓN/DEBUGGING ==========

    /**
     * Obtiene el total de pociones instanciadas
     */
    public int getTotalPotions() {
        return allPotions.size();
    }

    /**
     * Obtiene el total de gemas instanciadas
     */
    public int getTotalGems() {
        return allGems.size();
    }

    /**
     * Verifica si existe una combinación específica de poción
     */
    public boolean hasPotion(CombinedStats stat, PotionManager.PotionLevel level, PotionManager.PotionDuration duration) {
        String key = generatePotionKey(stat, level, duration);
        return allPotions.containsKey(key);
    }

    /**
     * Verifica si existe una combinación específica de gema
     */
    public boolean hasGem(CombinedStats stat, int level) {
        String key = generateGemKey(stat, level);
        return allGems.containsKey(key);
    }

    /**
     * Verifica si existe una gema por String
     */
    public boolean hasGem(String statName, int level) {
        try {
            CombinedStats stat = CombinedStats.valueOf(statName.toUpperCase());
            return hasGem(stat, level);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Lista todas las claves de pociones disponibles (para debugging)
     */
    public Set<String> getAllPotionKeys() {
        return new HashSet<>(allPotions.keySet());
    }

    /**
     * Lista todas las claves de gemas disponibles (para debugging)
     */
    public Set<String> getAllGemKeys() {
        return new HashSet<>(allGems.keySet());
    }

    /**
     * Lista todos los stats disponibles
     */
    public CombinedStats[] getAllStats() {
        return CombinedStats.values();
    }

    /**
     * Obtiene el nivel máximo de gemas
     */
    public int getMaxGemLevel() {
        return MAX_GEM_LEVEL;
    }

    /**
     * Obtiene información sobre las pociones instanciadas
     */
    public String getPotionInfo() {
        int totalStats = CombinedStats.values().length;
        int totalLevels = PotionManager.PotionLevel.values().length;
        int totalDurations = PotionManager.PotionDuration.values().length;
        int expectedTotal = totalStats * totalLevels * totalDurations;

        return String.format(
                "Pociones instanciadas: %d/%d\nStats: %d, Niveles: %d, Duraciones: %d",
                allPotions.size(), expectedTotal, totalStats, totalLevels, totalDurations
        );
    }

    /**
     * Obtiene información sobre las gemas instanciadas
     */
    public String getGemInfo() {
        int totalStats = CombinedStats.values().length;
        int expectedTotal = totalStats * MAX_GEM_LEVEL;

        return String.format(
                "Gemas instanciadas: %d/%d\nStats: %d, Niveles máximos: %d",
                allGems.size(), expectedTotal, totalStats, MAX_GEM_LEVEL
        );
    }

    /**
     * Obtiene información completa del sistema
     */
    public String getFullInfo() {
        return getPotionInfo() + "\n\n" + getGemInfo();
    }
}