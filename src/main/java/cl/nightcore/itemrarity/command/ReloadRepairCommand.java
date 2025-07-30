package cl.nightcore.itemrarity.command;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.util.AnvilRepairUtil.ItemRepairManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Map;

public class ReloadRepairCommand implements CommandExecutor {
    
    private final ItemRepairManager repairManager;
    
    public ReloadRepairCommand(ItemRepairManager repairManager) {
        this.repairManager = repairManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("itemrarity.admin")) {
            sender.sendMessage(ChatColor.RED + "No tienes permisos para ejecutar este comando.");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Recargando configuraciones de reparación...");
        
        try {
            repairManager.reloadConfigs();
            
            // Mostrar estadísticas después de recargar
            Map<String, Object> stats = repairManager.getStatistics();
            
            sender.sendMessage(ChatColor.GREEN + "¡Configuraciones recargadas exitosamente!");
            sender.sendMessage(ChatColor.AQUA + "Items configurados: " + ChatColor.WHITE + stats.get("total_items"));
            sender.sendMessage(ChatColor.AQUA + "Materiales únicos: " + ChatColor.WHITE + stats.get("unique_materials"));
            
            // Log detallado para admins
            @SuppressWarnings("unchecked")
            Map<String, Integer> materialUsage = (Map<String, Integer>) stats.get("material_usage");
            
            if (materialUsage.size() <= 5) {
                sender.sendMessage(ChatColor.AQUA + "Materiales de reparación:");
                for (Map.Entry<String, Integer> entry : materialUsage.entrySet()) {
                    sender.sendMessage(ChatColor.GRAY + "- " + entry.getKey() + ": " + entry.getValue() + " items");
                }
            }
            
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Error al recargar configuraciones: " + e.getMessage());
            ItemRarity.PLUGIN.getLogger().severe("Error recargando configuraciones de reparación: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }
}