package cl.nightcore.itemrarity.command;

import cl.nightcore.itemrarity.config.CombinedStats;
import cl.nightcore.itemrarity.item.potion.PotionManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GetPotionCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando solo puede ser ejecutado por jugadores.");
            return true;
        }

        // Validar argumentos mínimos
        if (args.length < 1) {
            sendUsage(player);
            return true;
        }

        // Comando especial para poción aleatoria
        if ("random".equalsIgnoreCase(args[0])) {
            return handleRandomPotion(player, args);
        }

        // Comando especial para pack mixto
        if ("pack".equalsIgnoreCase(args[0])) {
            return handleMixedPack(player, args);
        }

        // Obtener y validar el stat
        CombinedStats stat;
        try {
            stat = PotionManager.getStatFromString(args[0]);
        } catch (IllegalArgumentException e) {
            sendInvalidStatMessage(player);
            return true;
        }

        // Parsear argumentos opcionales con valores por defecto
        PotionArgs potionArgs = parseArguments(player, args);
        if (potionArgs == null) return true; // Error en el parsing

        // Crear y dar las pociones usando el PotionManager
        PotionManager.givePotions(player, stat, potionArgs.value, potionArgs.duration, potionArgs.amount);

        // Mensaje de confirmación
        String potionInfo = PotionManager.getPotionInfo(stat, potionArgs.value, potionArgs.duration);
        player.sendMessage("¡Has recibido " + potionArgs.amount + " poción(es) de " + potionInfo + "!");

        return true;
    }

    /**
     * Maneja el comando de poción aleatoria
     */
    private boolean handleRandomPotion(Player player, String[] args) {
        int amount = 1;

        // Verificar si se especificó cantidad
        if (args.length > 1) {
            try {
                amount = Integer.parseInt(args[1]);
                if (amount <= 0) {
                    player.sendMessage("La cantidad debe ser mayor a 0.");
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage("La cantidad debe ser un número válido.");
                return true;
            }
        }

        PotionManager.giveRandomPotions(player, amount);
        player.sendMessage("¡Has recibido " + amount + " poción(es) aleatoria(s)!");
        return true;
    }

    /**
     * Maneja el comando de pack mixto
     */
    private boolean handleMixedPack(Player player, String[] args) {
        double value = 10.0;
        int duration = 300;
        int amountPerStat = 1;

        // Parsear argumentos: /getpotion pack [valor] [duración] [cantidadPorStat]
        if (args.length > 1) {
            try {
                value = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage("El valor debe ser un número válido.");
                return true;
            }
        }

        if (args.length > 2) {
            try {
                duration = Integer.parseInt(args[2]);
                if (duration <= 0) {
                    player.sendMessage("La duración debe ser mayor a 0.");
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage("La duración debe ser un número válido.");
                return true;
            }
        }

        if (args.length > 3) {
            try {
                amountPerStat = Integer.parseInt(args[3]);
                if (amountPerStat <= 0) {
                    player.sendMessage("La cantidad por stat debe ser mayor a 0.");
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage("La cantidad por stat debe ser un número válido.");
                return true;
            }
        }

        PotionManager.giveMixedPotionPack(player, value, duration, amountPerStat);
        player.sendMessage("¡Has recibido un pack mixto de pociones (+" + value +
                ", " + PotionManager.formatDuration(duration) +
                ", " + amountPerStat + " por stat)!");
        return true;
    }

    /**
     * Parsea los argumentos del comando
     */
    private PotionArgs parseArguments(Player player, String[] args) {
        int amount = 1;
        double value = 10.0;
        int duration = 300;

        // Parsear cantidad
        if (args.length > 1) {
            try {
                amount = Integer.parseInt(args[1]);
                if (amount <= 0) {
                    player.sendMessage("La cantidad debe ser mayor a 0.");
                    return null;
                }
            } catch (NumberFormatException e) {
                player.sendMessage("La cantidad debe ser un número válido.");
                return null;
            }
        }

        // Parsear valor
        if (args.length > 2) {
            try {
                value = Double.parseDouble(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage("El valor debe ser un número válido.");
                return null;
            }
        }

        // Parsear duración
        if (args.length > 3) {
            try {
                duration = Integer.parseInt(args[3]);
                if (duration <= 0) {
                    player.sendMessage("La duración debe ser mayor a 0.");
                    return null;
                }
            } catch (NumberFormatException e) {
                player.sendMessage("La duración debe ser un número válido (en segundos).");
                return null;
            }
        }

        return new PotionArgs(amount, value, duration);
    }

    /**
     * Envía el mensaje de uso del comando
     */
    private void sendUsage(Player player) {
        player.sendMessage("§6=== Uso del comando /getpotion ===");
        player.sendMessage("§e/getpotion <stat> [cantidad] [valor] [duración]");
        player.sendMessage("§e/getpotion random [cantidad] - Pociones aleatorias");
        player.sendMessage("§e/getpotion pack [valor] [duración] [cantidadPorStat] - Pack mixto");
        player.sendMessage("§7Ejemplo: /getpotion HEALTH 5 15.5 600");
    }

    /**
     * Envía mensaje de stat inválido con opciones disponibles
     */
    private void sendInvalidStatMessage(Player player) {
        player.sendMessage("§cEl Stat proporcionado no es válido.");
        player.sendMessage("§6Stats disponibles:");

        String[] availableStats = PotionManager.getAvailableStatNames();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < availableStats.length; i++) {
            sb.append("§e").append(availableStats[i]);
            if (i < availableStats.length - 1) {
                sb.append("§7, ");
            }

            // Nueva línea cada 6 stats para mejor legibilidad
            if ((i + 1) % 6 == 0 && i < availableStats.length - 1) {
                player.sendMessage(sb.toString());
                sb = new StringBuilder();
            }
        }

        if (sb.length() > 0) {
            player.sendMessage(sb.toString());
        }
    }

    /**
     * Record para encapsular los argumentos parseados
     */
    private record PotionArgs(int amount, double value, int duration) {}
}