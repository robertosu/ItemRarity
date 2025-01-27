package cl.nightcore.itemrarity.command;

import cl.nightcore.itemrarity.GemManager;
import cl.nightcore.itemrarity.config.CombinedStats;
import cl.nightcore.itemrarity.item.GemObject;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.stat.Stat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GetGemCommand implements CommandExecutor {
    @Override
    public boolean onCommand(
            @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando solo puede ser ejecutado por jugadores.");
            return true;
        }

        // Validar argumentos
        if (args.length < 1) {
            player.sendMessage("Uso: /getgem <stat> [cantidad] [nivel]");
            return true;
        }

        // Obtener el Stat a partir del argumento
        Stat stat;
        try {
            stat = CombinedStats.valueOf(args[0].toUpperCase()); // Convertir a mayúsculas para coincidir con el enum
        } catch (IllegalArgumentException e) {
            player.sendMessage("El Stat proporcionado no es válido. Los disponibles son:");
            for (Stat availableStat : CombinedStats.values()) {
                player.sendMessage("- " + availableStat.name());
            }
            return true;
        }

        int amount = 1; // Cantidad predeterminada
        int level = 1; // Nivel predeterminado de la gema

        // Verificar si se proporcionaron argumentos para la cantidad y el nivel
        if (args.length > 1) {
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage("La cantidad debe ser un número válido.");
                return true;
            }
        }
        if (args.length > 2) {
            try {
                amount = Integer.parseInt(args[1]);
                level = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage("El nivel debe ser un número válido.");
                return true;
            }
        }

        GemManager gemManager = new GemManager();
        GemObject strengthGem = gemManager.createGem(amount, level, args[0].toUpperCase());
        player.getInventory().addItem(strengthGem);

        player.sendMessage("¡Has recibido " + amount + " gema(s) de " + stat.getDisplayName(AuraSkillsApi.get().getMessageManager().getDefaultLanguage()) + " de nivel " + level + "!");
        return true;
    }
}
