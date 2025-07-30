package cl.nightcore.itemrarity.command;

import cl.nightcore.itemrarity.rollquality.MainRollQuality;
import cl.nightcore.itemrarity.rollquality.RollQuality;
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
        if (level == 1) {
            return MainRollQuality.getInstance();
        }
        throw new IllegalArgumentException();
    }
}