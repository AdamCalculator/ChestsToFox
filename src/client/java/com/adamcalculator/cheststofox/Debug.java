package com.adamcalculator.cheststofox;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class Debug {
    public static void log(String s) {
        ChestsToFox.LOGGER.info(s);
        try {
            MinecraftClient.getInstance().player.sendMessage(Text.of("§6 ⚠ ChestsToFox: " + s));
        } catch (Exception ignored) {}
    }
}
