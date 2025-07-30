package cl.nightcore.itemrarity.util.AnvilRepairUtil;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NexoConfigLoader {
    private final Plugin plugin;
    private final Logger logger;
    private final String nexoItemsPath;

    public NexoConfigLoader(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.nexoItemsPath = "plugins/Nexo/items";
    }

    /**
     * Carga todas las configuraciones de items de Nexo y extrae las relaciones de reparación.
     *
     * @return Map con itemId -> repairMaterial
     */
    public Map<String, String> loadRepairRelations() {
        Map<String, String> repairRelations = new HashMap<>();
        
        File nexoItemsDir = new File(nexoItemsPath);
        
        if (!nexoItemsDir.exists() || !nexoItemsDir.isDirectory()) {
            logger.warning("La carpeta de items de Nexo no existe: " + nexoItemsPath);
            logger.warning("Asegúrate de que Nexo esté instalado y configurado correctamente.");
            return repairRelations;
        }

        logger.info("Cargando configuraciones de reparación desde: " + nexoItemsPath);
        
        int itemsProcessed = 0;
        int repairRelationsFound = 0;
        
        itemsProcessed = processDirectory(nexoItemsDir, repairRelations);
        repairRelationsFound = repairRelations.size();
        
        logger.info("Procesamiento completado:");
        logger.info("- Archivos procesados: " + itemsProcessed);
        logger.info("- Relaciones de reparación encontradas: " + repairRelationsFound);
        
        return repairRelations;
    }

    /**
     * Procesa recursivamente un directorio buscando archivos YAML.
     *
     * @param directory Directorio a procesar
     * @param repairRelations Map donde almacenar las relaciones encontradas
     * @return Número de archivos procesados
     */
    private int processDirectory(File directory, Map<String, String> repairRelations) {
        int filesProcessed = 0;
        
        File[] files = directory.listFiles();
        if (files == null) {
            return filesProcessed;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                // Procesar subdirectorios recursivamente
                filesProcessed += processDirectory(file, repairRelations);
            } else if (file.isFile() && isYamlFile(file)) {
                try {
                    processYamlFile(file, repairRelations);
                    filesProcessed++;
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error procesando archivo: " + file.getPath(), e);
                }
            }
        }
        
        return filesProcessed;
    }

    /**
     * Verifica si un archivo es un archivo YAML válido.
     *
     * @param file Archivo a verificar
     * @return true si es un archivo YAML
     */
    private boolean isYamlFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".yml") || name.endsWith(".yaml");
    }

    /**
     * Procesa un archivo YAML individual buscando items con repairableByNexoItem.
     *
     * @param file Archivo YAML a procesar
     * @param repairRelations Map donde almacenar las relaciones encontradas
     */
    private void processYamlFile(File file, Map<String, String> repairRelations) {
        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            
            // Obtener todas las claves del archivo (cada clave es un item)
            for (String itemId : config.getKeys(false)) {
                if (config.isConfigurationSection(itemId)) {
                    String repairMaterial = config.getString(itemId + ".repairableByNexoItem");
                    
                    if (repairMaterial != null && !repairMaterial.trim().isEmpty()) {
                        repairRelations.put(itemId, repairMaterial.trim());
                        logger.fine("Encontrada relación de reparación: " + itemId + " -> " + repairMaterial);
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error al cargar archivo YAML: " + file.getPath(), e);
        }
    }

    /**
     * Valida que la configuración de Nexo esté disponible.
     *
     * @return true si Nexo está disponible y configurado
     */
    public boolean isNexoAvailable() {
        File nexoItemsDir = new File(nexoItemsPath);
        return nexoItemsDir.exists() && nexoItemsDir.isDirectory();
    }

    /**
     * Obtiene estadísticas sobre los archivos de configuración de Nexo.
     *
     * @return Map con estadísticas
     */
    public Map<String, Integer> getStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        
        if (!isNexoAvailable()) {
            stats.put("available", 0);
            return stats;
        }
        
        stats.put("available", 1);
        stats.put("directories", countDirectories(new File(nexoItemsPath)));
        stats.put("yaml_files", countYamlFiles(new File(nexoItemsPath)));
        
        return stats;
    }

    private int countDirectories(File directory) {
        int count = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    count++;
                    count += countDirectories(file);
                }
            }
        }
        return count;
    }

    private int countYamlFiles(File directory) {
        int count = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    count += countYamlFiles(file);
                } else if (isYamlFile(file)) {
                    count++;
                }
            }
        }
        return count;
    }
}