package cl.nightcore.itemrarity.command.api;

import cl.nightcore.itemrarity.ItemRarity;
import cl.nightcore.itemrarity.abstracted.UpgradeableItem;
import cl.nightcore.itemrarity.config.CombinedStats;
import cl.nightcore.itemrarity.item.BlessingBall;
import cl.nightcore.itemrarity.item.DarkmagicObject;
import cl.nightcore.itemrarity.item.ExperienceMultiplier;
import cl.nightcore.itemrarity.item.ItemUpgrader;
import cl.nightcore.itemrarity.item.gem.GemManager;
import cl.nightcore.itemrarity.item.gem.GemObject;
import cl.nightcore.itemrarity.item.gem.GemRemover;
import cl.nightcore.itemrarity.item.gem.SocketStone;
import cl.nightcore.itemrarity.item.potion.PotionManager;
import cl.nightcore.itemrarity.item.potion.StatPotion;
import cl.nightcore.itemrarity.item.roller.*;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.Arrays;

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
                        new CommandAPICommand("gem")
                                .withArguments(
                                        new StringArgument("stat").replaceSuggestions(ArgumentSuggestions.strings(
                                                getAvailableStats()
                                        ))
                                )
                                .withOptionalArguments(
                                        new IntegerArgument("amount", 1, 64),
                                        new IntegerArgument("level", 1, 100)
                                )
                                .executesPlayer(this::handleGetGem)
                )
                .withSubcommand(
                        new CommandAPICommand("gemremover")
                                .withArguments(
                                        new IntegerArgument("level", 1, 100)
                                )
                                .withOptionalArguments(new IntegerArgument("amount", 1, 64))
                                .executesPlayer(this::handleGetGemRemover)
                )
                .withSubcommand(
                        new CommandAPICommand("itemupgrader")
                                .withArguments(
                                        new IntegerArgument("level", 1, 100),
                                        new IntegerArgument("type", 0, 10)
                                )
                                .withOptionalArguments(new IntegerArgument("amount", 1, 64))
                                .executesPlayer(this::handleGetItemUpgrader)
                )
                .withSubcommand(
                        new CommandAPICommand("help")
                                .executesPlayer(this::showHelp)
                ).withSubcommand(
                        new CommandAPICommand("updatelore")
                                .executesPlayer(this::handleUpdateLore)
                ).withSubcommand(
                        new CommandAPICommand("potion")
                                .withArguments(
                                        new StringArgument("stat").replaceSuggestions(ArgumentSuggestions.strings(
                                                PotionManager.getAvailableStatNames()
                                        )),
                                        new StringArgument("level").replaceSuggestions(ArgumentSuggestions.strings(
                                                "ONE", "TWO", "THREE", "FOUR", "FIVE"
                                        )),
                                        new StringArgument("duration").replaceSuggestions(ArgumentSuggestions.strings(
                                                "LOW", "MEDIUM", "HIGH", "HIGHEST"
                                        ))
                                )
                                .withOptionalArguments(new IntegerArgument("amount", 1, 64))
                                .executesPlayer(this::handleGivePotion)
                )
                .register();
    }


    private void handleGivePotion(Player player, CommandArguments args) {
        try {
            // Obtener argumentos
            String statName = (String) args.get("stat");
            String levelName = (String) args.get("level");
            String durationName = (String) args.get("duration");
            int amount = (Integer) args.getOrDefault("amount", 1);

            // Validar y convertir stat
            CombinedStats stat;
            try {
                stat = PotionManager.getStatFromString(statName);
            } catch (IllegalArgumentException e) {
                player.sendMessage(Component.text("Stat inválido: " + statName, NamedTextColor.RED));
                player.sendMessage(Component.text("Stats disponibles: " + String.join(", ", PotionManager.getAvailableStatNames()), NamedTextColor.GRAY));
                return;
            }

            // Validar y convertir level
            PotionManager.PotionLevel level;
            try {
                level = PotionManager.PotionLevel.valueOf(levelName.toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage(Component.text("Nivel inválido: " + levelName, NamedTextColor.RED));
                player.sendMessage(Component.text("Niveles disponibles: ONE, TWO, THREE, FOUR, FIVE", NamedTextColor.GRAY));
                return;
            }

            // Validar y convertir duration
            PotionManager.PotionDuration duration;
            try {
                duration = PotionManager.PotionDuration.valueOf(durationName.toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage(Component.text("Duración inválida: " + durationName, NamedTextColor.RED));
                player.sendMessage(Component.text("Duraciones disponibles: LOW, MEDIUM, HIGH, HIGHEST", NamedTextColor.GRAY));
                return;
            }

            // Crear y dar pociones al jugador
            for (int i = 0; i < amount; i++) {
                StatPotion potion = PotionManager.createPotion(stat, level, duration);
                player.getInventory().addItem(potion);
            }

            // Mensaje de confirmación
            String potionInfo = PotionManager.getPotionInfoWithValues(stat, level.getValue(), duration.getSeconds());
            player.sendMessage(Component.text("✓ Has obtenido " + amount + " poción(es) de " + potionInfo + "!", NamedTextColor.GREEN));

        } catch (Exception e) {
            player.sendMessage(Component.text("Error al crear la poción: " + e.getMessage(), NamedTextColor.RED));
            e.printStackTrace();
        }
    }

    private void handleUpdateLore(Player player, CommandArguments args){
        var item = player.getInventory().getItemInMainHand();

        if (item.isEmpty()){
            player.sendMessage("Debes tener un objeto en tu mano");
        }else{
            UpgradeableItem newItem = new UpgradeableItem(item);
            newItem.setNewLore();
            player.getInventory().setItemInMainHand(newItem);
        }
    }

    private void handleGetGem(Player player, CommandArguments args) {
        try {
            String statName = (String) args.get("stat");
            int amount = (Integer) args.getOrDefault("amount", 1);
            int level = (Integer) args.getOrDefault("level", 1);

            // Validar el stat
            Stat stat;
            try {
                stat = CombinedStats.valueOf(statName.toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage(Component.text("El Stat proporcionado no es válido. Los disponibles son:", NamedTextColor.RED));
                for (Stat availableStat : CombinedStats.values()) {
                    player.sendMessage(Component.text("- " + availableStat.name(), NamedTextColor.GRAY));
                }
                return;
            }

            // Crear y dar la gema
            GemManager gemManager = new GemManager();
            GemObject gem = gemManager.createGem(amount, level, statName.toUpperCase());
            player.getInventory().addItem(gem);

            String displayName = stat.getDisplayName(AuraSkillsApi.get().getMessageManager().getDefaultLanguage());
            player.sendMessage(Component.text("✓ Has recibido " + amount + " gema(s) de " + displayName + " de nivel " + level + "!", NamedTextColor.GREEN));

        } catch (Exception e) {
            player.sendMessage(Component.text("Error al crear la gema: " + e.getMessage(), NamedTextColor.RED));
            e.printStackTrace();
        }
    }

    private void handleGetGemRemover(Player player, CommandArguments args) {
        try {
            int level = (Integer) args.get("level");
            int amount = (Integer) args.getOrDefault("amount", 64);

            GemRemover gemRemover = new GemRemover(amount, level);
            player.getInventory().addItem(gemRemover);

            player.sendMessage(Component.text("✓ Has obtenido " + amount + " removedor(es) de gema nivel " + level + "!", NamedTextColor.GREEN));

        } catch (Exception e) {
            player.sendMessage(Component.text("Error al crear el removedor de gemas: " + e.getMessage(), NamedTextColor.RED));
            e.printStackTrace();
        }
    }

    private void handleGetItemUpgrader(Player player, CommandArguments args) {
        try {
            int level = (Integer) args.get("level");
            int type = (Integer) args.get("type");
            int amount = (Integer) args.getOrDefault("amount", 64);

            ItemUpgrader itemUpgrader = new ItemUpgrader(amount, level, type);
            player.getInventory().addItem(itemUpgrader);

            player.sendMessage(Component.text("✓ Has obtenido " + amount + " Runa(s) Activa(s)!", NamedTextColor.GREEN));

        } catch (Exception e) {
            player.sendMessage(Component.text("Error al crear el item upgrader: " + e.getMessage(), NamedTextColor.RED));
            e.printStackTrace();
        }
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
        player.sendMessage(Component.text("/itemget darkscroll [cantidad]", NamedTextColor.YELLOW)
                .append(Component.text(" - Obtener pergaminos prohibidos", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/itemget gem <stat> [cantidad] [nivel]", NamedTextColor.YELLOW)
                .append(Component.text(" - Obtener gemas de stats", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/itemget gemremover <nivel> [cantidad]", NamedTextColor.YELLOW)
                .append(Component.text(" - Obtener removedores de gemas", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/itemget itemupgrader <nivel> <type> [cantidad]", NamedTextColor.YELLOW)
                .append(Component.text(" - Obtener runas activas", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/itemget potion <stat> <nivel> <duración> [cantidad]", NamedTextColor.YELLOW)
                .append(Component.text(" - Obtener pociones de stats", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/itemget updatelore", NamedTextColor.YELLOW)
                .append(Component.text(" - Actualizar lore del item en mano", NamedTextColor.GRAY)));
        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("Cantidad predeterminada: 64 (excepto gem: 1, xpmultiplier: 1)", NamedTextColor.AQUA));
        player.sendMessage(Component.text("Porcentajes XP disponibles: 100, 200, 300, 400, 500", NamedTextColor.AQUA));
        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("Ejemplos:", NamedTextColor.GREEN));
        player.sendMessage(Component.text("• /itemget magic 16", NamedTextColor.WHITE));
        player.sendMessage(Component.text("• /itemget gem STRENGTH 5 10", NamedTextColor.WHITE));
        player.sendMessage(Component.text("• /itemget gemremover 5 32", NamedTextColor.WHITE));
        player.sendMessage(Component.text("• /itemget itemupgrader 3 1 16", NamedTextColor.WHITE));
        player.sendMessage(Component.text("• /itemget xpmultiplier 200 5", NamedTextColor.WHITE));
    }

    private String[] getAvailableStats() {
        return Arrays.stream(CombinedStats.values())
                .map(Enum::name)
                .toArray(String[]::new);
    }
}