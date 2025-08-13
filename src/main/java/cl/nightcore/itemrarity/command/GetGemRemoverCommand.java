package cl.nightcore.itemrarity.command;

import cl.nightcore.itemrarity.item.gem.GemRemover;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GetGemRemoverCommand implements CommandExecutor {
    @Override
    public boolean onCommand(
            @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando solo puede ser ejecutado por jugadores.");
            return true;
        }

        int amount = 64; // Cantidad predeterminada

        // Verificar si se proporcionó un argumento para la cantidad
        if (args.length > 0) {
            try {
                amount = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                player.sendMessage("Uso: /getremover [cantidad] [nivel]");
                return true;
            }
        }

        GemRemover gemRemover = new GemRemover(amount, Integer.parseInt(args[1]));

        player.getInventory().addItem(gemRemover);
        player.sendMessage("¡Has obtenido " + amount + " removedor(es) de gema nivel " + args[1] + "!");

        return true;
    }
}