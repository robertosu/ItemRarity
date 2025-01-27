package cl.nightcore.itemrarity.command;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.item.BlessingBall;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GetBlessingBallCommand implements CommandExecutor {
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
                player.sendMessage("Uso: /getblessingball [cantidad]");
                return true;
            }
        }

        BlessingBall blessingBall = new BlessingBall(amount, ItemRarity.getPlugin(ItemRarity.class));

        player.getInventory().addItem(blessingBall);
        player.sendMessage("¡Has obtenido " + amount + " bola(s) bendición!");

        return true;
    }
}