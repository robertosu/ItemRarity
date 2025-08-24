// PartyCommands.java - Sistema de comandos completo con Adventure API
package cl.nightcore.itemrarity.command;

import cl.nightcore.itemrarity.loot.PartyManager;
import cl.nightcore.itemrarity.loot.PartyManager.PartyResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PartyCommands implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Este comando solo puede ser usado por jugadores.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create", "crear" -> handleCreate(player);
            case "invite", "invitar" -> handleInvite(player, args);
            case "accept", "aceptar" -> handleAccept(player);
            case "reject", "rechazar" -> handleReject(player);
            case "leave", "salir" -> handleLeave(player);
            case "dissolve", "disolver" -> handleDissolve(player);
            case "info", "información" -> handleInfo(player);
            case "list", "lista" -> handleList(player);
            case "kick", "expulsar" -> handleKick(player, args);
            case "stats", "estadísticas" -> handleStats(player);
            case "help", "ayuda" -> showHelp(player);
            default -> {
                player.sendMessage(Component.text("Subcomando desconocido. Usa /party help para ver la ayuda.", NamedTextColor.RED));
                return true;
            }
        }

        return true;
    }

    private void handleCreate(Player player) {
        PartyResult result = PartyManager.createParty(player);

        if (result.isSuccess()) {
            player.sendMessage(Component.text()
                    .append(Component.text("✔ ", NamedTextColor.GREEN))
                    .append(Component.text(result.getMessage(), NamedTextColor.GREEN))
                    .build());
            player.sendMessage(Component.text("Usa /party invite <jugador> para invitar miembros.", NamedTextColor.YELLOW));
        } else {
            player.sendMessage(Component.text()
                    .append(Component.text("✗ ", NamedTextColor.RED))
                    .append(Component.text(result.getMessage(), NamedTextColor.RED))
                    .build());
        }
    }

    private void handleInvite(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Component.text("Uso: /party invite <jugador>", NamedTextColor.RED));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(Component.text("Jugador no encontrado o no está online.", NamedTextColor.RED));
            return;
        }

        if (target.equals(player)) {
            player.sendMessage(Component.text("No puedes invitarte a ti mismo.", NamedTextColor.RED));
            return;
        }

        PartyResult result = PartyManager.invitePlayer(player, target);

        if (result.isSuccess()) {
            player.sendMessage(Component.text()
                    .append(Component.text("✔ ", NamedTextColor.GREEN))
                    .append(Component.text(result.getMessage(), NamedTextColor.GREEN))
                    .build());

            // Notificar al jugador invitado
            Component inviteMessage = Component.text()
                    .append(Component.text("════════════════════════════════════\n", NamedTextColor.YELLOW))
                    .append(Component.text("🎉 INVITACIÓN A PARTY �\n", NamedTextColor.GOLD, TextDecoration.BOLD))
                    .append(Component.text(player.getName(), NamedTextColor.WHITE))
                    .append(Component.text(" te ha invitado a su party!\n\n", NamedTextColor.YELLOW))
                    .append(Component.text("▶ /party accept", NamedTextColor.GREEN))
                    .append(Component.text(" - Aceptar\n", NamedTextColor.WHITE))
                    .append(Component.text("▶ /party reject", NamedTextColor.RED))
                    .append(Component.text(" - Rechazar\n\n", NamedTextColor.WHITE))
                    .append(Component.text("Esta invitación expira en 30 segundos.\n", NamedTextColor.GRAY))
                    .append(Component.text("════════════════════════════════════", NamedTextColor.YELLOW))
                    .build();

            target.sendMessage(inviteMessage);
        } else {
            player.sendMessage(Component.text()
                    .append(Component.text("✗ ", NamedTextColor.RED))
                    .append(Component.text(result.getMessage(), NamedTextColor.RED))
                    .build());
        }
    }

    private void handleAccept(Player player) {
        PartyResult result = PartyManager.acceptInvitation(player);

        if (result.isSuccess()) {
            player.sendMessage(Component.text()
                    .append(Component.text("✔ ", NamedTextColor.GREEN))
                    .append(Component.text(result.getMessage(), NamedTextColor.GREEN))
                    .build());

            // Notificar a todos los miembros de la party
            result.getParty().ifPresent(party -> {
                Component joinMessage = Component.text()
                        .append(Component.text(player.getName(), NamedTextColor.YELLOW))
                        .append(Component.text(" se ha unido a la party!", NamedTextColor.YELLOW))
                        .build();

                party.getMembers().forEach(memberId -> {
                    Player member = Bukkit.getPlayer(memberId);
                    if (member != null && member.isOnline() && !member.equals(player)) {
                        member.sendMessage(joinMessage);
                    }
                });
            });
        } else {
            player.sendMessage(Component.text()
                    .append(Component.text("✗ ", NamedTextColor.RED))
                    .append(Component.text(result.getMessage(), NamedTextColor.RED))
                    .build());
        }
    }

    private void handleReject(Player player) {
        PartyResult result = PartyManager.rejectInvitation(player);

        if (result.isSuccess()) {
            player.sendMessage(Component.text("Invitación rechazada.", NamedTextColor.YELLOW));
        } else {
            player.sendMessage(Component.text()
                    .append(Component.text("✗ ", NamedTextColor.RED))
                    .append(Component.text(result.getMessage(), NamedTextColor.RED))
                    .build());
        }
    }

    private void handleLeave(Player player) {
        PartyResult result = PartyManager.leaveParty(player);

        if (result.isSuccess()) {
            player.sendMessage(Component.text("Has salido de la party.", NamedTextColor.YELLOW));

            // Notificar a los miembros restantes si los hay
            PartyManager.getPlayerParty(player.getUniqueId()).ifPresent(party -> {
                Component leaveMessage = Component.text()
                        .append(Component.text(player.getName(), NamedTextColor.YELLOW))
                        .append(Component.text(" ha salido de la party.", NamedTextColor.YELLOW))
                        .build();

                party.getMembers().forEach(memberId -> {
                    Player member = Bukkit.getPlayer(memberId);
                    if (member != null && member.isOnline()) {
                        member.sendMessage(leaveMessage);
                    }
                });
            });
        } else {
            player.sendMessage(Component.text()
                    .append(Component.text("✗ ", NamedTextColor.RED))
                    .append(Component.text(result.getMessage(), NamedTextColor.RED))
                    .build());
        }
    }

    private void handleDissolve(Player player) {
        PartyResult result = PartyManager.dissolveParty(player);

        if (result.isSuccess()) {
            player.sendMessage(Component.text("Party disuelta.", NamedTextColor.RED));
        } else {
            player.sendMessage(Component.text()
                    .append(Component.text("✗ ", NamedTextColor.RED))
                    .append(Component.text(result.getMessage(), NamedTextColor.RED))
                    .build());
        }
    }

    private void handleInfo(Player player) {
        List<Component> info = PartyManager.getPartyInfo(player);

        Component header = Component.text("══════════════════════════════════", NamedTextColor.GOLD);
        player.sendMessage(header);
        info.forEach(player::sendMessage);
        player.sendMessage(header);
    }

    private void handleList(Player player) {
        if (!PartyManager.isInParty(player.getUniqueId())) {
            player.sendMessage(Component.text("No estás en ninguna party.", NamedTextColor.RED));
            return;
        }

        PartyManager.getPlayerParty(player.getUniqueId()).ifPresent(party -> {
            Component header = Component.text("═══ MIEMBROS DE LA PARTY ═══", NamedTextColor.YELLOW);
            player.sendMessage(header);

            party.getMembers().forEach(memberId -> {
                Player member = Bukkit.getPlayer(memberId);
                String name = member != null ? member.getName() : "Unknown";

                Component memberComponent = Component.text()
                        .append(Component.text("●",
                                (member != null && member.isOnline()) ? NamedTextColor.GREEN : NamedTextColor.RED))
                        .append(Component.text(" " + name, NamedTextColor.WHITE)).build();

                if (party.isLeader(memberId)) {
                    memberComponent = memberComponent.append(
                            Component.text(" [Líder]", NamedTextColor.GOLD));
                }

                player.sendMessage(memberComponent);
            });
        });
    }

    private void handleKick(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Component.text("Uso: /party kick <jugador>", NamedTextColor.RED));
            return;
        }

        // Verificar que es líder
        PartyManager.getPlayerParty(player.getUniqueId()).ifPresentOrElse(party -> {
            if (!party.isLeader(player.getUniqueId())) {
                player.sendMessage(Component.text("Solo el líder puede expulsar miembros.", NamedTextColor.RED));
                return;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage(Component.text("Jugador no encontrado.", NamedTextColor.RED));
                return;
            }

            if (target.equals(player)) {
                player.sendMessage(Component.text("No puedes expulsarte a ti mismo. Usa /party dissolve para disolver la party.", NamedTextColor.RED));
                return;
            }

            if (!party.getMembers().contains(target.getUniqueId())) {
                player.sendMessage(Component.text("Ese jugador no está en tu party.", NamedTextColor.RED));
                return;
            }

            // Expulsar al jugador
            PartyResult result = PartyManager.leaveParty(target.getUniqueId());

            if (result.isSuccess()) {
                player.sendMessage(Component.text()
                        .append(Component.text(target.getName(), NamedTextColor.YELLOW))
                        .append(Component.text(" ha sido expulsado de la party.", NamedTextColor.YELLOW))
                        .build());

                target.sendMessage(Component.text()
                        .append(Component.text("Has sido expulsado de la party por ", NamedTextColor.RED))
                        .append(Component.text(player.getName(), NamedTextColor.RED))
                        .build());

                // Notificar a otros miembros
                Component kickMessage = Component.text()
                        .append(Component.text(target.getName(), NamedTextColor.YELLOW))
                        .append(Component.text(" ha sido expulsado de la party.", NamedTextColor.YELLOW))
                        .build();

                party.getMembers().forEach(memberId -> {
                    Player member = Bukkit.getPlayer(memberId);
                    if (member != null && member.isOnline() &&
                            !member.equals(player) && !member.equals(target)) {
                        member.sendMessage(kickMessage);
                    }
                });
            }
        }, () -> player.sendMessage(Component.text("No estás en ninguna party.", NamedTextColor.RED)));
    }

    private void handleStats(Player player) {
        if (!player.hasPermission("party.admin")) {
            player.sendMessage(Component.text("No tienes permisos para ver las estadísticas.", NamedTextColor.RED));
            return;
        }

        var stats = PartyManager.getStats();

        Component header = Component.text("═══ ESTADÍSTICAS DEL SISTEMA ═══", NamedTextColor.YELLOW);
        player.sendMessage(header);
        player.sendMessage(Component.text()
                .append(Component.text("Parties activas: ", NamedTextColor.WHITE))
                .append(Component.text(stats.totalParties(), NamedTextColor.GREEN))
                .build());
        player.sendMessage(Component.text()
                .append(Component.text("Jugadores en party: ", NamedTextColor.WHITE))
                .append(Component.text(stats.totalPlayers(), NamedTextColor.GREEN))
                .build());
        player.sendMessage(Component.text()
                .append(Component.text("Invitaciones pendientes: ", NamedTextColor.WHITE))
                .append(Component.text(stats.pendingInvitations(), NamedTextColor.GREEN))
                .build());
        player.sendMessage(Component.text()
                .append(Component.text("Tamaño promedio: ", NamedTextColor.WHITE))
                .append(Component.text(String.format("%.1f", stats.averageSize()), NamedTextColor.GREEN))
                .build());
    }

    private void showHelp(Player player) {
        Component helpMessage = Component.text()
                .append(Component.text("══════════════════════════════════\n", NamedTextColor.GOLD))
                .append(Component.text("           COMANDOS DE PARTY\n", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text("══════════════════════════════════\n", NamedTextColor.GOLD))
                .append(Component.text("/party create", NamedTextColor.YELLOW))
                .append(Component.text(" - Crear una nueva party\n", NamedTextColor.WHITE))
                .append(Component.text("/party invite <jugador>", NamedTextColor.YELLOW))
                .append(Component.text(" - Invitar un jugador\n", NamedTextColor.WHITE))
                .append(Component.text("/party accept", NamedTextColor.YELLOW))
                .append(Component.text(" - Aceptar invitación\n", NamedTextColor.WHITE))
                .append(Component.text("/party reject", NamedTextColor.YELLOW))
                .append(Component.text(" - Rechazar invitación\n", NamedTextColor.WHITE))
                .append(Component.text("/party leave", NamedTextColor.YELLOW))
                .append(Component.text(" - Salir de la party\n", NamedTextColor.WHITE))
                .append(Component.text("/party dissolve", NamedTextColor.YELLOW))
                .append(Component.text(" - Disolver party (líder)\n", NamedTextColor.WHITE))
                .append(Component.text("/party kick <jugador>", NamedTextColor.YELLOW))
                .append(Component.text(" - Expulsar jugador (líder)\n", NamedTextColor.WHITE))
                .append(Component.text("/party info", NamedTextColor.YELLOW))
                .append(Component.text(" - Ver información de tu party\n", NamedTextColor.WHITE))
                .append(Component.text("/party list", NamedTextColor.YELLOW))
                .append(Component.text(" - Listar miembros\n", NamedTextColor.WHITE)).build();

        if (player.hasPermission("party.admin")) {
            helpMessage = helpMessage.append(Component.text("/party stats", NamedTextColor.GRAY))
                    .append(Component.text(" - Ver estadísticas (admin)\n", NamedTextColor.WHITE));
        }

        helpMessage = helpMessage
                .append(Component.text("════════════════════════════════════\n", NamedTextColor.GOLD))
                .append(Component.text("Las parties solo afectan drops de BOSSES.\n", NamedTextColor.GRAY))
                .append(Component.text("Invitaciones expiran en 30 segundos.", NamedTextColor.GRAY));

        player.sendMessage(helpMessage);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            List<String> subcommands = Arrays.asList(
                    "create", "invite", "accept", "reject", "leave",
                    "dissolve", "kick", "info", "list", "help"
            );

            if (player.hasPermission("party.admin")) {
                subcommands = new ArrayList<>(subcommands);
                subcommands.add("stats");
            }

            return subcommands.stream()
                    .filter(sub -> sub.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();

            if (subCommand.equals("invite")) {
                // Sugerir jugadores online que no están en party
                return Bukkit.getOnlinePlayers().stream()
                        .filter(p -> !p.equals(player))
                        .filter(p -> !PartyManager.isInParty(p.getUniqueId()))
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }

            if (subCommand.equals("kick")) {
                // Sugerir miembros de la party (excepto el líder)
                return PartyManager.getPlayerParty(player.getUniqueId())
                        .map(party -> {
                            if (!party.isLeader(player.getUniqueId())) {
                                return new ArrayList<String>();
                            }

                            return party.getMembers().stream()
                                    .filter(uuid -> !uuid.equals(player.getUniqueId()))
                                    .map(Bukkit::getPlayer)
                                    .filter(Objects::nonNull)
                                    .map(Player::getName)
                                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                                    .collect(Collectors.toList());
                        })
                        .orElse(new ArrayList<>());
            }
        }

        return new ArrayList<>();
    }
}