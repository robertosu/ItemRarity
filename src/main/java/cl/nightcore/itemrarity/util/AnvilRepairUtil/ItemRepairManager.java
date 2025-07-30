package cl.nightcore.itemrarity.util.AnvilRepairUtil;

import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ItemRepairManager {
    private final Map<String, String> repairMaterials = new HashMap<>();
    private final Plugin plugin;
    private final Logger logger;

    public ItemRepairManager(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    /**
     * Carga las relaciones de reparación desde las configuraciones de Nexo.
     */
    public void loadFromNexoConfigs() {
        logger.info("Iniciando carga de configuraciones de reparación desde Nexo...");
        
        NexoConfigLoader configLoader = new NexoConfigLoader(plugin);
        
        // Verificar si Nexo está disponible
        if (!configLoader.isNexoAvailable()) {
            logger.warning("Nexo no está disponible. No se cargarán configuraciones de reparación.");
            return;
        }

        // Mostrar estadísticas antes de cargar
        Map<String, Integer> stats = configLoader.getStatistics();
        logger.info("Estadísticas de Nexo:");
        logger.info("- Directorios encontrados: " + stats.get("directories"));
        logger.info("- Archivos YAML encontrados: " + stats.get("yaml_files"));

        // Limpiar configuraciones anteriores
        repairMaterials.clear();

        // Cargar nuevas configuraciones
        Map<String, String> newRelations = configLoader.loadRepairRelations();
        repairMaterials.putAll(newRelations);

        logger.info("Configuraciones de reparación cargadas exitosamente.");
        logger.info("Total de items configurados para reparación: " + repairMaterials.size());
        
        // Log detallado de las relaciones cargadas (solo en modo debug)
        if (logger.isLoggable(java.util.logging.Level.FINE)) {
            logger.fine("Relaciones de reparación cargadas:");
            for (Map.Entry<String, String> entry : repairMaterials.entrySet()) {
                logger.fine("- " + entry.getKey() + " -> " + entry.getValue());
            }
        }
    }

    /**
     * Recarga las configuraciones desde Nexo.
     * Útil para comandos de reload.
     */
    public void reloadConfigs() {
        logger.info("Recargando configuraciones de reparación...");
        loadFromNexoConfigs();
    }

    /**
     * Obtiene el material de reparación para un ítem específico.
     *
     * @param itemId ID del ítem.
     * @return ID del material de reparación, o null si no está registrado.
     */
    public String getRepairMaterial(String itemId) {
        return repairMaterials.get(itemId);
    }

    /**
     * Verifica si un ítem puede ser reparado con otro ítem.
     *
     * @param itemId       ID del ítem a reparar.
     * @param materialId   ID del material de reparación.
     * @return true si el material es válido para reparar el ítem.
     */
    public boolean isValidRepair(@Nullable String itemId,@Nullable String materialId) {
        if (itemId==null || materialId ==null){
            return false;
        }
        String repairMaterial = getRepairMaterial(itemId);
        return repairMaterial != null && repairMaterial.equals(materialId);
    }

    /**
     * Verifica si un ítem está configurado para ser reparable.
     *
     * @param itemId ID del ítem.
     * @return true si el ítem tiene un material de reparación configurado.
     */
    public boolean isRepairable(String itemId) {
        return repairMaterials.containsKey(itemId);
    }

    /**
     * Obtiene todas las relaciones de reparación cargadas.
     *
     * @return Map inmutable con todas las relaciones itemId -> repairMaterial
     */
    public Map<String, String> getAllRepairRelations() {
        return new HashMap<>(repairMaterials);
    }

    /**
     * Obtiene estadísticas sobre las configuraciones cargadas.
     *
     * @return Map con estadísticas
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_items", repairMaterials.size());
        
        // Contar materiales únicos
        Map<String, Integer> materialCounts = new HashMap<>();
        for (String material : repairMaterials.values()) {
            materialCounts.put(material, materialCounts.getOrDefault(material, 0) + 1);
        }
        stats.put("unique_materials", materialCounts.size());
        stats.put("material_usage", materialCounts);
        
        return stats;
    }

    /**
     * Busca todos los items que pueden ser reparados con un material específico.
     *
     * @param materialId ID del material de reparación
     * @return Lista de IDs de items que pueden ser reparados con este material
     */
    public java.util.List<String> getItemsRepairableWith(String materialId) {
        return repairMaterials.entrySet().stream()
                .filter(entry -> entry.getValue().equals(materialId))
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toList());
    }
}