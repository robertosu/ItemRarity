package cl.nightcore.itemrarity.command;

import cl.nightcore.itemrarity.item.ExperienceMultiplier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GetExperienceMultiplierCommand implements CommandExecutor {
    @Override
    public boolean onCommand(
            @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando solo puede ser ejecutado por jugadores.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage("Uso: /getxpmultiplier <porcentaje> [cantidad]");
            player.sendMessage("Porcentajes disponibles: 100, 200, 300, 400, 500");
            return true;
        }

        int multiplier;
        try {
            multiplier = Integer.parseInt(args[0]);
            if (multiplier % 100 != 0 || multiplier < 100 || multiplier > 500) {
                player.sendMessage("Porcentaje inválido. Debe ser 100, 200, 300, 400 o 500");
                return true;
            }
        } catch (NumberFormatException e) {
            player.sendMessage("Porcentaje debe ser un número válido");
            return true;
        }

        int amount = 1;
        if (args.length > 1) {
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage("Cantidad debe ser un número válido");
                return true;
            }
        }

        ExperienceMultiplier expMultiplier = new ExperienceMultiplier(amount, multiplier);
        player.getInventory().addItem(expMultiplier);
        player.sendMessage("¡Has obtenido " + amount + " multiplicador(es) de experiencia " + multiplier + "%!");

        return true;
    }
}