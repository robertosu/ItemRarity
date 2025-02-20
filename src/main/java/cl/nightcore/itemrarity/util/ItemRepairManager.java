package cl.nightcore.itemrarity.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ItemRepairManager {
    private final Map<String, String> repairMaterials = new HashMap<>(); // Mapa de ID de ítem -> Material de reparación

    /**
     * Registra un grupo de ítems que pueden ser reparados con un material específico.
     *
     * @param itemIds       IDs de los ítems que forman parte del grupo.
     * @param repairMaterial ID del material de reparación.
     */
    public void registerRepairGroup(Set<String> itemIds, String repairMaterial) {
        for (String itemId : itemIds) {
            repairMaterials.put(itemId, repairMaterial);
        }
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
    public boolean isValidRepair(String itemId, String materialId) {
        String repairMaterial = getRepairMaterial(itemId);
        return repairMaterial != null && repairMaterial.equals(materialId);
    }
}