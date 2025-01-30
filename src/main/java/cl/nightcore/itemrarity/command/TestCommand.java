package cl.nightcore.itemrarity.command;

import cl.nightcore.itemrarity.abstracted.RollQuality;
import cl.nightcore.itemrarity.classes.*;
import cl.nightcore.itemrarity.test.StatDistributionTester;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class TestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(
            @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        StatDistributionTester tester =
                new StatDistributionTester(getRollQuality(Integer.parseInt(args[0])), Integer.parseInt(args[1]));
        tester.runAnalysis();
        return true;
    }

    private RollQuality getRollQuality(int level) {
        switch (level) {
            case 1 -> {
                return _1RollQuality.getInstance();
            }
            case 2 -> {
                return _2RollQuality.getInstance();
            }
            case 3 -> {
                return _3RollQuality.getInstance();
            }
            case 4 -> {
                return _4RollQuality.getInstance();
            }
            case 5 -> {
                return _5RollQuality.getInstance();
            }
            case 6 -> {
                return _6RollQuality.getInstance();
            }
            case 7 -> {
                return _7RollQuality.getInstance();
            }
            case 8 -> {
                return _8RollQuality.getInstance();
            }
            case 9 -> {
                return _9RollQuality.getInstance();
            }

            default -> throw new IllegalArgumentException();
        }
    }
}