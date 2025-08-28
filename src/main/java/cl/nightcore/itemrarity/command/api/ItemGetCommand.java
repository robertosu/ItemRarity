package cl.nightcore.itemrarity.command;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.item.BlessingBall;
import cl.nightcore.itemrarity.item.DarkmagicObject;
import cl.nightcore.itemrarity.item.ExperienceMultiplier;
import cl.nightcore.itemrarity.item.gem.SocketStone;
import cl.nightcore.itemrarity.item.roller.*;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class ItemGetCommand {

    private final ItemRarity plugin;

    public ItemGetCommand(ItemRarity plugin) {
        this.plugin = plugin;
        registerCommand();
    }

    private void registerCommand() {
        new CommandAPICommand("itemget")
                .withPermission("itemrarity.admin")
                .withSubcommand(
                        new CommandAPICommand("magic")
                                .withOptionalArguments(new IntegerArgument("amount", 1, 64))
                                .executesPlayer(this::handleMagic)
                )
                .withSubcommand(
                        new CommandAPICommand("redemption")
                                .withOptionalArguments(new IntegerArgument("amount", 1, 64))
                                .executesPlayer(this::handleRedemption)
                )
                .withSubcommand(
                        new CommandAPICommand("scroll")
                                .withOptionalArguments(new IntegerArgument("amount", 1, 64))
                                .executesPlayer(this::handleScroll)
                )
                .withSubcommand(
                        new CommandAPICommand("blessing")
                                .withOptionalArguments(new IntegerArgument("amount", 1, 64))
                                .executesPlayer(this::handleBlessing)
                )
                .withSubcommand(
                        new CommandAPICommand("blessingball")
                                .withOptionalArguments(new IntegerArgument("amount", 1, 64))
                                .executesPlayer(this::handleBlessingBall)
                )
                .withSubcommand(
                        new CommandAPICommand("xpmultiplier")
                                .withArguments(
                                        new StringArgument("percentage").replaceSuggestions(ArgumentSuggestions.strings(
                                                "100", "200", "300", "400", "500"
                                        ))
                                )
                                .withOptionalArguments(new IntegerArgument("amount", 1, 64))
                                .executesPlayer(this::handleXpMultiplier)
                )
                .withSubcommand(
                        new CommandAPICommand("socketstone")
                                .withOptionalArguments(new IntegerArgument("amount", 1, 64))
                                .executesPlayer(this::handleSocketStone)
                )
                .withSubcommand(
                        new CommandAPICommand("darkmagic")
                                .withOptionalArguments(new IntegerArgument("amount", 1, 64))
                                .executesPlayer(this::handleDarkmagic)
                ).withSubcommand(
                        new CommandAPICommand("darkscroll")
                                .withOptionalArguments(new IntegerArgument("amount", 1, 64))
                                .executesPlayer(this::handleDarkscroll)
                )
                .withSubcommand(
                        new CommandAPICommand("help")
                                .executesPlayer(this::showHelp)
                )
                .register();
    }

    private void handleMagic(Player player, CommandArguments args) {
        int amount = (Integer) args.getOrDefault("amount", 64);

        MagicObject magic = new MagicObject(amount, plugin);
        player.getInventory().addItem(magic);
        player.sendMessage(Component.text("✓ Has obtenido " + amount + " objeto(s) mágico(s)!", NamedTextColor.GREEN));
    }

    private void handleRedemption(Player player, CommandArguments args) {
        int amount = (Integer) args.getOrDefault("amount", 64);

        RedemptionObject redemption = new RedemptionObject(amount, plugin);
        player.getInventory().addItem(redemption);
        player.sendMessage(Component.text("✓ Has obtenido " + amount + " objeto(s) redención!", NamedTextColor.GREEN));
    }

    private void handleScroll(Player player, CommandArguments args) {
        int amount = (Integer) args.getOrDefault("amount", 64);

        IdentifyScroll scroll = new IdentifyScroll(amount, plugin);
        player.getInventory().addItem(scroll);
        player.sendMessage(Component.text("✓ Has obtenido " + amount + " scroll(s)!", NamedTextColor.GREEN));
    }

    private void handleBlessing(Player player, CommandArguments args) {
        int amount = (Integer) args.getOrDefault("amount", 64);

        BlessingObject blessing = new BlessingObject(amount, plugin);
        player.getInventory().addItem(blessing);
        player.sendMessage(Component.text("✓ Has obtenido " + amount + " objeto(s) bendición!", NamedTextColor.GREEN));
    }

    private void handleBlessingBall(Player player, CommandArguments args) {
        int amount = (Integer) args.getOrDefault("amount", 64);

        BlessingBall blessingBall = new BlessingBall(amount, plugin);
        player.getInventory().addItem(blessingBall);
        player.sendMessage(Component.text("✓ Has obtenido " + amount + " bola(s) bendición!", NamedTextColor.GREEN));
    }

    private void handleXpMultiplier(Player player, CommandArguments args) {
        String percentageStr = (String) args.get("percentage");
        int amount = (Integer) args.getOrDefault("amount", 1);

        int percentage;
        try {
            percentage = Integer.parseInt(percentageStr);
            if (percentage % 100 != 0 || percentage < 100 || percentage > 500) {
                player.sendMessage(Component.text("Porcentaje inválido. Debe ser 100, 200, 300, 400 o 500", NamedTextColor.RED));
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("Porcentaje debe ser un número válido", NamedTextColor.RED));
            return;
        }

        ExperienceMultiplier expMultiplier = new ExperienceMultiplier(amount, percentage);
        player.getInventory().addItem(expMultiplier);
        player.sendMessage(Component.text("✓ Has obtenido " + amount + " multiplicador(es) de experiencia " + percentage + "%!", NamedTextColor.GREEN));
    }

    private void handleSocketStone(Player player, CommandArguments args) {
        int amount = (Integer) args.getOrDefault("amount", 64);

        SocketStone socketStone = new SocketStone(amount, plugin);
        player.getInventory().addItem(socketStone);
        player.sendMessage(Component.text("✓ Has obtenido " + amount + " piedra(s) afilada(s)!", NamedTextColor.GREEN));
    }

    private void handleDarkmagic(Player player, CommandArguments args) {
        int amount = (Integer) args.getOrDefault("amount", 64);

        DarkmagicObject darkmagic = new DarkmagicObject(amount, plugin);
        player.getInventory().addItem(darkmagic);
        player.sendMessage(Component.text("✓ Has obtenido " + amount + " objeto(s) de magia oscura!", NamedTextColor.GREEN));
    }


    private void handleDarkscroll(Player player, CommandArguments args) {
        int amount = (Integer) args.getOrDefault("amount", 64);

        DarkScroll darkScroll = new DarkScroll(amount, plugin);
        player.getInventory().addItem(darkScroll);
        player.sendMessage(Component.text("✓ Has obtenido " + amount + " objeto(s) de pergamino prohibido!", NamedTextColor.GREEN));
    }

    private void showHelp(Player player, CommandArguments args) {
        player.sendMessage(Component.text("========== ItemGet Command Help ==========", NamedTextColor.GOLD));
        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("/itemget magic [cantidad]", NamedTextColor.YELLOW)
                .append(Component.text(" - Obtener objetos mágicos", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/itemget redemption [cantidad]", NamedTextColor.YELLOW)
                .append(Component.text(" - Obtener objetos de redención", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/itemget scroll [cantidad]", NamedTextColor.YELLOW)
                .append(Component.text(" - Obtener scrolls de identificación", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/itemget blessing [cantidad]", NamedTextColor.YELLOW)
                .append(Component.text(" - Obtener objetos de bendición", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/itemget blessingball [cantidad]", NamedTextColor.YELLOW)
                .append(Component.text(" - Obtener bolas de bendición", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/itemget xpmultiplier <porcentaje> [cantidad]", NamedTextColor.YELLOW)
                .append(Component.text(" - Obtener multiplicadores de XP", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/itemget socketstone [cantidad]", NamedTextColor.YELLOW)
                .append(Component.text(" - Obtener piedras afiladas", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/itemget darkmagic [cantidad]", NamedTextColor.YELLOW)
                .append(Component.text(" - Obtener objetos de magia oscura", NamedTextColor.GRAY)));
        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("Cantidad predeterminada: 64 (excepto xpmultiplier: 1)", NamedTextColor.AQUA));
        player.sendMessage(Component.text("Porcentajes XP disponibles: 100, 200, 300, 400, 500", NamedTextColor.AQUA));
        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("Ejemplos:", NamedTextColor.GREEN));
        player.sendMessage(Component.text("• /itemget magic 16", NamedTextColor.WHITE));
        player.sendMessage(Component.text("• /itemget xpmultiplier 200 5", NamedTextColor.WHITE));
        player.sendMessage(Component.text("• /itemget darkmagic 32", NamedTextColor.WHITE));
    }
}