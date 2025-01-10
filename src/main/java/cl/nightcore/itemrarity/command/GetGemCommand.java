package cl.nightcore.itemrarity.command;

import cl.nightcore.itemrarity.GemManager;
import cl.nightcore.itemrarity.item.GemObject;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.Stats;
import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.trait.Traits;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GetGemCommand implements CommandExecutor {
 @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando solo puede ser ejecutado por jugadores.");
            return true;
        }

     // Validar argumentos
        if (args.length < 1) {
            player.sendMessage("Uso: /getgem <stat> [cantidad] [nivel]");
            return true;
        }

        // Obtener el Stat a partir del argumento
        Trait trait;
        try {
            trait = Traits.valueOf(args[0].toUpperCase()); // Convertir a mayúsculas para coincidir con el enum
        } catch (IllegalArgumentException e) {
            player.sendMessage("El Stat proporcionado no es válido. Los disponibles son:");
            for (Trait availableStat : Traits.values()) {
                player.sendMessage("- " + availableStat.name());
            }
            return true;
        }

        int amount = 1; // Cantidad predeterminada
        int level = 1;  // Nivel predeterminado de la gema

        // Verificar si se proporcionaron argumentos para la cantidad y el nivel
        if (args.length > 1) {
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage("La cantidad debe ser un número válido.");
                return true;
            }
        }
        if (args.length > 2) {
            try {
                level = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage("El nivel debe ser un número válido.");
                return true;
            }
        }
        GemManager gemManager = new GemManager();
        GemObject strengthGem = gemManager.createStrengthGem(amount,level,args[0]);
        // Crear y añadir las gemas al inventario del jugador
         // Valor escalado con el nivel
        //Component gemName = Component.text(trait.getDisplayName(AuraSkillsApi.get().getMessageManager().getDefaultLanguage())).color(NamedTextColor.GOLD); // Nombre del Stat
        player.getInventory().addItem(strengthGem);




        player.sendMessage("¡Has recibido " + amount + " gema(s) de " + trait.name() + " de nivel " + level + "!");
        return true;
    }

}
