package cl.nightcore.itemrarity.command;

import com.nexomc.nexo.api.NexoBlocks;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class NexoHelpCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        try{
            var blockId = args[0];

            System.out.println( NexoBlocks.blockData(blockId));


        }catch (Exception e){
            System.out.println("No hay bloque con esa id");
        }
        return true;
    }
}
