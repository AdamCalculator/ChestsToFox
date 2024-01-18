package com.adamcalculator.cheststofox.mixin.client;

import com.adamcalculator.cheststofox.ChestsToFox;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
	@Inject(at = @At("HEAD"), method = "setScreen")
	private void setScreen(Screen screen, CallbackInfo ci) {
		ChestsToFox.setScreenInject(screen, ci);
	}
}