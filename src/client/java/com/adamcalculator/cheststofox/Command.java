package com.adamcalculator.cheststofox;

import com.adamcalculator.cheststofox.container.ContainerManager;
import com.adamcalculator.cheststofox.container.stat.StatCollector;
import com.adamcalculator.cheststofox.util.Files;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import java.io.File;

public final class Command {
    private static long bufferClearTimer = 0;

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("ctf")
                .then(ClientCommandManager.literal("export")
                        .then(ClientCommandManager.literal("available")
                                .executes(Command::exportAvailable))

                        .then(ClientCommandManager.literal("all")
                                .executes(Command::exportAll)))

                .then(ClientCommandManager.literal("auto_close_gui")
                        .then(ClientCommandManager.literal("off")
                                .executes(Command::autoCloseGuiOff))

                        .then(ClientCommandManager.literal("on")
                                .executes(Command::autoCloseGuiOn)))

                .then(ClientCommandManager.literal("saving")
                        .then(ClientCommandManager.literal("off")
                                .executes(Command::savingDisable))

                        .then(ClientCommandManager.literal("on")
                                .executes(Command::savingEnable)))

                .then(ClientCommandManager.literal("buffer")
                        .then(ClientCommandManager.literal("clear")
                                .executes(Command::bufferClear))

                        .then(ClientCommandManager.literal("clear_once")
                                .executes(Command::bufferClearOnce)))

                .executes(Command::printState)));
    }

    private static int autoCloseGuiOff(CommandContext<FabricClientCommandSource> context) {
        Config.CONFIG.setAutoClose(false);
        context.getSource().sendFeedback(Text.of(("§c ✔ Auto-close guis disabled")));
        return 0;
    }

    private static int autoCloseGuiOn(CommandContext<FabricClientCommandSource> context) {
        Config.CONFIG.setAutoClose(true);
        context.getSource().sendFeedback(Text.of(("§a ✔ Auto-close guis enabled")));
        return 0;
    }

    private static int bufferClearOnce(CommandContext<FabricClientCommandSource> context) {
        StateMachine.clearOnce = !StateMachine.clearOnce;
        if (StateMachine.clearOnce) {
            context.getSource().sendFeedback(Text.of(("§6 ⚠ Click to block for delete from buffer. Enter this command again for cancel deletion.")));

        } else {
            context.getSource().sendFeedback(Text.of(("§a ✔ Deletion canceled.")));
        }
        return 1;
    }

    private static int bufferClear(CommandContext<FabricClientCommandSource> context) {
        long current = System.currentTimeMillis();
        if (current - bufferClearTimer > 2500) {
            context.getSource().sendFeedback(Text.of(("§6 ⚠ Enter this command again to confirm deletion...")));
            bufferClearTimer = current;
        } else {
            ContainerManager.clearAllMemory();
            context.getSource().sendFeedback(Text.of(("§a ✔ Success buffer cleared!")));
        }
        return 1;
    }

    private static int savingEnable(CommandContext<FabricClientCommandSource> context) {
        Config.CONFIG.setSaving(true);
        context.getSource().sendFeedback(Text.of(("§a ✔ Enabled")));
        return 1;
    }

    private static int savingDisable(CommandContext<FabricClientCommandSource> context) {
        Config.CONFIG.setSaving(false);
        context.getSource().sendFeedback(Text.of(("§c ✔ Disabled")));
        return 1;
    }

    private static int printState(CommandContext<FabricClientCommandSource> context) {
        String saving;
        if (Config.CONFIG.savingEnable) {
            saving = "§aEnabled!";
        } else {
            saving = "§cDisabled";
        }

        String currBuff = "<empty>";

        if (ContainerManager.isNotEmpty()) {
            currBuff = " * Saved containers: %s".formatted(ContainerManager.getSavedPositions().length);
        }

        String stat = String.format("\n§6§lChestsToFox %s §r§dby PawVille:\n§r§5Saving: §r%s§r§6\n§r§3Current buffer:\n§r%s§r§6\n", ChestsToFox.prettyVersion(), saving, currBuff);
        context.getSource().sendFeedback(Text.of(stat));
        return 1;
    }

    private static int exportAll(CommandContext<FabricClientCommandSource> context) {
        final String content = StatCollector.statToCsv(StatCollector.collectAll(ContainerManager.containersData));
        final String filename = ChestsToFox.getCurrentNameOfExportFile();
        final File file = new File(ChestsToFox.getExportsDir(), filename);
        Files.writeFile(file, content);
        context.getSource().sendFeedback(Text.of(("§a ✔ Success exported to: §2.../mods/ChestsToFox/§l" + filename).replace("/", File.separator)));
        return 1;
    }

    private static int exportAvailable(CommandContext<FabricClientCommandSource> context) {
        final String content = StatCollector.statToCsv(StatCollector.collectAvailable(ContainerManager.containersData));
        final String filename = ChestsToFox.getCurrentNameOfExportFile();
        final File file = new File(ChestsToFox.getExportsDir(), filename);
        Files.writeFile(file, content);
        context.getSource().sendFeedback(Text.of(("§a ✔ Success exported to: §2.../mods/ChestsToFox/§l" + filename).replace("/", File.separator)));
        return 1;
    }
}
