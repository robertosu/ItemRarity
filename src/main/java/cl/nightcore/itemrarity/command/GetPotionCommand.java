package cl.nightcore.itemrarity.command;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.config.CombinedStats;
import cl.nightcore.itemrarity.item.potion.StatPotion;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GetPotionCommand implements CommandExecutor {
    @Override
    public boolean onCommand(
            @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando solo puede ser ejecutado por jugadores.");
            return true;
        }

        // Validar argumentos
        if (args.length < 1) {
            player.sendMessage("Uso: /getpotion <stat> [cantidad] [valor] [duración]");
            return true;
        }

        // Obtener el Stat a partir del argumento
        CombinedStats stat;
        try {
            stat = CombinedStats.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage("El Stat proporcionado no es válido. Los disponibles son:");
            for (CombinedStats availableStat : CombinedStats.values()) {
                player.sendMessage("- " + availableStat.name());
            }
            return true;
        }

        int amount = 1; // Cantidad predeterminada
        double value = 10.0; // Valor predeterminado
        int duration = 300; // Duración predeterminada (5 minutos)

        // Verificar si se proporcionaron argumentos para la cantidad
        if (args.length > 1) {
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage("La cantidad debe ser un número válido.");
                return true;
            }
        }

        // Verificar si se proporcionó el valor
        if (args.length > 2) {
            try {
                value = Double.parseDouble(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage("El valor debe ser un número válido.");
                return true;
            }
        }

        // Verificar si se proporcionó la duración
        if (args.length > 3) {
            try {
                duration = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                player.sendMessage("La duración debe ser un número válido (en segundos).");
                return true;
            }
        }

        // Crear las pociones dinámicamente
        for (int i = 0; i < amount; i++) {
            StatPotion potion = new StatPotion(stat, value, duration);
            player.getInventory().addItem(potion);
        }

        player.sendMessage("¡Has recibido " + amount + " poción(es) de "
                + stat.getDisplayName(ItemRarity.AURA_LOCALE)
                + " (+" + value + ") con duración de " + formatDuration(duration) + "!");
        return true;
    }

    private String formatDuration(int seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            return (seconds / 60) + "m " + (seconds % 60) + "s";
        } else {
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            return hours + "h " + minutes + "m";
        }
    }
}