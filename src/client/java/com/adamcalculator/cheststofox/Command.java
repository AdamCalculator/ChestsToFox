package com.adamcalculator.cheststofox;

import com.adamcalculator.cheststofox.container.ContainerManager;
import com.adamcalculator.cheststofox.container.stat.StatCollector;
import com.adamcalculator.cheststofox.util.Files;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.io.File;
import java.util.HashMap;

public final class Command {
    private static long bufferClearTimer = 0;

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("ctf")
                .then(ClientCommandManager.literal("export")
                        .then(ClientCommandManager.literal("available")
                                .executes(Command::exportAvailable))

                        .then(ClientCommandManager.literal("all")
                                .executes(Command::exportAll)))

                .then(ClientCommandManager.literal("auto_close_gui")
                        .executes(Command::toggleAutoCloseGuis))

                .then(ClientCommandManager.literal("saving")
                        .executes(Command::toggleSaving))

                .then(ClientCommandManager.literal("buffer")
                        .then(ClientCommandManager.literal("clear")
                                .executes(Command::bufferClear))

                        .then(ClientCommandManager.literal("clear_once")
                                .executes(Command::bufferClearOnce)))

                .executes(Command::printState)));
    }



    private static int toggleAutoCloseGuis(CommandContext<FabricClientCommandSource> context) {
        if (Config.CONFIG.isAutoCloseGuis()) {
            autoCloseGuiOff(context);
        } else {
            autoCloseGuiOn(context);
        }
        return 0;
    }

    private static void autoCloseGuiOff(CommandContext<FabricClientCommandSource> context) {
        Config.CONFIG.setAutoClose(false);
        context.getSource().sendFeedback(Text.translatable("cheststofox.command.ctf.auto_close_guis.disabled").formatted(Formatting.RED));
    }

    private static void autoCloseGuiOn(CommandContext<FabricClientCommandSource> context) {
        Config.CONFIG.setAutoClose(true);
        context.getSource().sendFeedback(Text.translatable("cheststofox.command.ctf.auto_close_guis.enabled").formatted(Formatting.GREEN));
    }

    private static int bufferClearOnce(CommandContext<FabricClientCommandSource> context) {
        StateMachine.clearOnce = !StateMachine.clearOnce;
        if (StateMachine.clearOnce) {
            context.getSource().sendFeedback(Text.translatable("cheststofox.command.ctf.buffer.clearOnce.howto").formatted(Formatting.GOLD));

        } else {
            context.getSource().sendFeedback(Text.translatable("cheststofox.command.ctf.buffer.clearOnce.canceled").formatted(Formatting.DARK_GREEN));

        }
        return 1;
    }

    private static int bufferClear(CommandContext<FabricClientCommandSource> context) {
        long current = System.currentTimeMillis();
        if (current - bufferClearTimer > 2500) {
            context.getSource().sendFeedback(Text.translatable("cheststofox.command.ctf.buffer.clear.double_call_notice").formatted(Formatting.GOLD));
            bufferClearTimer = current;
        } else {
            ContainerManager.clearAllMemory();
            context.getSource().sendFeedback(Text.translatable("cheststofox.command.ctf.buffer.clear.success").formatted(Formatting.GREEN));
        }
        return 1;
    }

    private static int toggleSaving(CommandContext<FabricClientCommandSource> context) {
        if (Config.CONFIG.isSaving()) {
            savingDisable(context);
        } else {
            savingEnable(context);
        }
        return 1;
    }

    private static void savingEnable(CommandContext<FabricClientCommandSource> context) {
        Config.CONFIG.setSaving(true);
        context.getSource().sendFeedback(Text.translatable("cheststofox.command.ctf.saving.enabled").formatted(Formatting.GREEN));
    }

    private static void savingDisable(CommandContext<FabricClientCommandSource> context) {
        Config.CONFIG.setSaving(false);
        context.getSource().sendFeedback(Text.translatable("cheststofox.command.ctf.saving.disabled").formatted(Formatting.RED));
    }

    private static int printState(CommandContext<FabricClientCommandSource> context) {
        Object saving = Text.translatable(Config.CONFIG.isSaving() ? "cheststofox.generic.boolean.enabled" : "cheststofox.generic.boolean.disabled");
        Object autoCloseGuis = Text.translatable(Config.CONFIG.isAutoCloseGuis() ? "cheststofox.generic.boolean.enabled" : "cheststofox.generic.boolean.disabled");

        MutableText currBuff;
        String prefix;
        final MutableText miniStatText = Text.empty();
        if (ContainerManager.isNotEmpty()) {
            prefix = "\n ";
            currBuff = Text.translatable("cheststofox.command.ctf.printState.buffer.stat", ContainerManager.getSavedPositions().length);
            final HashMap<String, StatCollector.StatRow> miniStat = StatCollector.collectAvailable(ContainerManager.containersData);
            int i = 0;
            for (String id : miniStat.keySet()) {
                StatCollector.StatRow row = miniStat.get(id);

                Text rowText = Text.translatable("cheststofox.command.ctf.printState.buffer.hover_mini_stat.row",
                        Text.literal(String.valueOf(row.getAmount())).formatted(Formatting.DARK_AQUA),
                        Text.literal(row.getLocalized()).formatted(Formatting.GREEN),
                        Text.literal(String.valueOf(row.getContainers().size())).formatted(Formatting.LIGHT_PURPLE));
                miniStatText.append(rowText);
                miniStatText.append(Text.of("\n"));

                if (i++ > 7) {
                    break;
                }
            }
        } else {
            prefix = " ";
            currBuff = Text.translatable("cheststofox.command.ctf.printState.buffer.empty");
        }


        currBuff.formatted(Formatting.DARK_AQUA, Formatting.UNDERLINE)
                .styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("cheststofox.command.ctf.printState.buffer.hover_mini_stat", miniStatText))));

        MutableText buffWithPref = Text.literal(prefix).append(currBuff);
        context.getSource().sendFeedback(Text.translatable("cheststofox.command.ctf.printState", Text.literal(ChestsToFox.prettyVersion()).formatted(Formatting.YELLOW), saving, autoCloseGuis, buffWithPref));
        return 1;
    }

    private static int exportAll(CommandContext<FabricClientCommandSource> context) {
        final String content = StatCollector.statToCsv(StatCollector.collectAll(ContainerManager.containersData));
        genericExport(context, content);
        return 1;
    }

    private static int exportAvailable(CommandContext<FabricClientCommandSource> context) {
        final String content = StatCollector.statToCsv(StatCollector.collectAvailable(ContainerManager.containersData));
        genericExport(context, content);
        return 1;
    }

    private static void genericExport(CommandContext<FabricClientCommandSource> context, String exportedString) {
        final String filename = ChestsToFox.getCurrentNameOfExportFile();
        final File file = new File(ChestsToFox.getExportsDir(), filename);
        Files.writeFile(file, exportedString);
        Text filetext = Text.literal(Config.CONFIG.workdir.replace("%MINECRAFT%", ".minecraft") + "/" + file.getName())
                .formatted(Formatting.UNDERLINE, Formatting.ITALIC, Formatting.DARK_GREEN)
                .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, ChestsToFox.getExportsDir().getAbsolutePath())));

        context.getSource().sendFeedback(Text.translatable("cheststofox.command.ctf.export.success", filetext).formatted(Formatting.GREEN));
    }
}
