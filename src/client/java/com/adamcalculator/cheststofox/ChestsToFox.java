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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
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

public class ChestsToFox implements ClientModInitializer {
	public static final int CHESTS_TO_FOX_VERSION_BUILD = 30;
	public static final String CHESTS_TO_FOX_VERSION_NAME = "1.3";


	public static final Logger LOGGER = LoggerFactory.getLogger("ChestsToFox");
	public static final ContainerLinker CONTAINER_LINKER = new ContainerLinker();


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

		} else if (blockEntity instanceof EnderChestBlockEntity) {
			CONTAINER_LINKER.skipOnce();
		}

		if (StateMachine.clearOnce) {
			if (baseBlock == null) {
				baseBlock = hitResult.getBlockPos();
			}
			if (ContainerManager.isContainerAtExists(baseBlock)) {
				ContainerManager.clearAt(baseBlock);
				MinecraftClient.getInstance().player.sendMessage(Text.translatable("cheststofox.command.ctf.buffer.clearOnce.success", baseBlock.toShortString()).formatted(Formatting.GREEN));
			} else {
				MinecraftClient.getInstance().player.sendMessage(Text.translatable("cheststofox.command.ctf.buffer.clearOnce.not_found", baseBlock.toShortString()).formatted(Formatting.DARK_GREEN));
			}
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
		return new File(Config.CONFIG.workdir.replace("%MINECRAFT%", MinecraftClient.getInstance().runDirectory.getAbsolutePath()));
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
			if (CONTAINER_LINKER.isSkipOnceAndMakeDone()) {
				return;
			}

			if (previously instanceof GenericContainerScreen genericContainerScreen) {
				processInventoryForPos(genericContainerScreen.handler.getInventory(), CONTAINER_LINKER.getAndDone());

			} else if (previously instanceof HopperScreen hopperScreen) {
				processInventoryForPos(hopperScreen.handler.inventory, CONTAINER_LINKER.getAndDone());

			} else if (previously instanceof ShulkerBoxScreen schulkerBoxScreen) {
				processInventoryForPos(schulkerBoxScreen.handler.inventory, CONTAINER_LINKER.getAndDone());
			}
		} else {
			if (Config.CONFIG.autoCloseWhenSaving && !CONTAINER_LINKER.isSkipOnce()) {
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
			Debug.log("CONTAINER_LINKER currently not wait guis (server open custom gui or big network ping???)");
			return;
		}
		ContainerManager.updateContainerData(pos, inventory);
	}

	public static String prettyVersion() {
		return String.format("%s (v%s)", CHESTS_TO_FOX_VERSION_NAME, CHESTS_TO_FOX_VERSION_BUILD);
	}

	public static String getItemId(ItemStack stack) {
		return getItemId(stack.getItem());
	}

	public static String getItemId(Item item) {
		return getItemId(Registries.ITEM.getId(item));
	}

	/**
	 * This method converts Identifiers of items to string ids
	 * If you want change/modify ids in sheets, you can make this here
	 */
	public static String getItemId(Identifier identifier) {
		return identifier.getPath(); // path = dirt (without 'namespace:' prefix); make toString() to minecraft:dirt
	}
}