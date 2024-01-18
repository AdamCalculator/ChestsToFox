package com.adamcalculator.cheststofox;

import com.adamcalculator.cheststofox.container.ContainerManager;
import com.adamcalculator.cheststofox.util.ContainerLinker;
import com.adamcalculator.cheststofox.util.Util;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.entity.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HopperScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.inventory.Inventory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.BooleanSupplier;

public class ChestsToFox implements ClientModInitializer {
	public static final int CHESTS_TO_FOX_VERSION_BUILD = 17;
	public static final String CHESTS_TO_FOX_VERSION_NAME = "0.7 beta";


	public static final Logger LOGGER = LoggerFactory.getLogger("ChestsToFox");
	public static final ContainerLinker CONTAINER_LINKER = new ContainerLinker();
	private static final Timer timer = new Timer();


	@Override
	public void onInitializeClient() {
		LOGGER.info("Mod ChestsToFox initializing in client");
		Config.CONFIG = Config.loadFromFile();
		Config.CONFIG.version = 1;

		// block click event
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (hitResult.getType() == HitResult.Type.BLOCK) {
				return clickedToBlock(world, hitResult);
			}

			return ActionResult.PASS;
		});

		Command.register();
	}

	private ActionResult clickedToBlock(World world, BlockHitResult hitResult) {
		if (!Config.CONFIG.isSaving() && !StateMachine.clearOnce) return ActionResult.PASS;

		final BlockEntity blockEntity = world.getBlockEntity(hitResult.getBlockPos());
		BlockPos baseBlock = null;
		if (blockEntity instanceof ChestBlockEntity chestBlock) {
			baseBlock = Util.getBaseChestBlockPos(chestBlock);

		} else if (blockEntity instanceof BarrelBlockEntity barrel) {
			baseBlock = barrel.getPos();

		} else if (blockEntity instanceof HopperBlockEntity hopper) {
			baseBlock = hopper.getPos();

		} else if (blockEntity instanceof ShulkerBoxBlockEntity shulker) {
			baseBlock = shulker.getPos();
		}

		if (StateMachine.clearOnce) {
			if (baseBlock == null) {
				baseBlock = hitResult.getBlockPos();
			}
			ContainerManager.clearAt(baseBlock);
			MinecraftClient.getInstance().player.sendMessage(Text.of(("§a ✔ Container at " + baseBlock.toShortString() + " removed from buffer.")));
			StateMachine.clearOnce = false;
			return ActionResult.FAIL;
		} else {
			if (baseBlock != null) {
				CONTAINER_LINKER.waitContainerDataForOpen(baseBlock);
			}
		}


		return ActionResult.PASS;
	}

	public static File getExportsDir() {
		return new File(MinecraftClient.getInstance().runDirectory, "/mods/ChestsToFox/");
	}

	public static String getCurrentNameOfExportFile() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
		LocalDateTime now = LocalDateTime.now();
		return "export_" + dtf.format(now) + ".csv";
	}

	public static void setScreenInject(Screen screen, CallbackInfo ci) {
		if (!Config.CONFIG.isSaving()) return;


		// when closing screen
		if (screen == null) {
			Screen previously = MinecraftClient.getInstance().currentScreen;
			if (previously instanceof GenericContainerScreen genericContainerScreen) {
				processInventoryForPos(genericContainerScreen.handler.getInventory(), CONTAINER_LINKER.getAndDone());

			} else if (previously instanceof HopperScreen hopperScreen) {
				processInventoryForPos(hopperScreen.handler.inventory, CONTAINER_LINKER.getAndDone());

			} else if (previously instanceof ShulkerBoxScreen schulkerBoxScreen) {
				processInventoryForPos(schulkerBoxScreen.handler.inventory, CONTAINER_LINKER.getAndDone());
			}
		} else {
			if (Config.CONFIG.autoCloseWhenSaving) {
				if (screen instanceof GenericContainerScreen || screen instanceof HopperScreen || screen instanceof ShulkerBoxScreen) {
					Timer timer = new Timer();
					timer.schedule(new TimerTask() {
						@Override
						public void run() {
							// direct call causes glitches (Timer runs on non-render thread)
							MinecraftClient.getInstance().executeSync(screen::close);
						}
					}, Config.CONFIG.autoCloseWhenSavingDelay);
				}
			}
		}
	}

	public static void processInventoryForPos(Inventory inventory, BlockPos pos) {
		if (pos == null) {
			Debug.log("CONTAINER_LINKER currently not wait guis (server open custom gui or big network ping)");
			return;
		}
		ContainerManager.updateContainerData(pos, inventory);
	}

	public static String prettyVersion() {
		return String.format("%s (v%s)", CHESTS_TO_FOX_VERSION_NAME, CHESTS_TO_FOX_VERSION_BUILD);
	}
}