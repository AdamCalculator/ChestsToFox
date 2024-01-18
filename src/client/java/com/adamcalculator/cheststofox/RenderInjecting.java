package com.adamcalculator.cheststofox;

import com.adamcalculator.cheststofox.container.ContainerEntry;
import com.adamcalculator.cheststofox.container.ContainerManager;
import com.adamcalculator.cheststofox.util.Util;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class RenderInjecting {
    public static void renderChestBlockEntity(ChestBlockEntity chestBlock, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (!StateMachine.isRenderCustomContainers()) return;

        try {
            if (Util.isAppendixChest(chestBlock)) return;
            BlockPos basePos = Util.getBaseChestBlockPos(chestBlock);
            if (ContainerManager.isContainerAtExists(basePos)) {
                ContainerEntry container = ContainerManager.getContainer(basePos);
                if (StateMachine.isRenderHighlightedContainer(basePos, container)) {
                    Box box = new Box(1-0.95f, 1-0.95f-0.05, 1-0.95f, 0.95f, 0.95f-0.05, 0.95f);
                    if (Util.isDoubleChest(chestBlock)) {
                        Direction direction = chestBlock.getCachedState().get(ChestBlock.FACING).rotateYCounterclockwise();
                        final float widenerCoff = 1f;
                        if (direction == Direction.WEST) {
                            box = new Box(box.minX - widenerCoff, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
                        } else if (direction == Direction.NORTH) {
                            box = new Box(box.minX, box.minY, box.minZ - widenerCoff, box.maxX, box.maxY, box.maxZ);
                        } else if (direction == Direction.EAST) {
                            box = new Box(box.minX, box.minY, box.minZ, box.maxX + widenerCoff, box.maxY, box.maxZ);
                        } else if (direction == Direction.SOUTH) {
                            box = new Box(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ + widenerCoff);
                        }
                    }

                    float red = container.red();
                    float green = container.green();
                    float blue = container.blue();
                    DebugRenderer.drawBox(matrices, vertexConsumers, box, red, green, blue, 0.5f);
                }
            }
        } catch (Exception e) {
            ChestsToFox.LOGGER.error("container rendering error", e);
        }
    }

    public static void renderHopperBlockEntity(HopperBlockEntity hopperBlock, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        renderCube(hopperBlock.getPos(), 0.4f, matrices, vertexConsumers, ci);
    }

    public static void renderCube(BlockPos basePos, float alpha, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (!StateMachine.isRenderCustomContainers()) return;
        try {
            if (ContainerManager.isContainerAtExists(basePos)) {
                ContainerEntry container = ContainerManager.getContainer(basePos);
                if (StateMachine.isRenderHighlightedContainer(basePos, container)) {
                    Box box = new Box(0, 0, 0, 1, 1, 1).expand(0.01);

                    float red = container.red();
                    float green = container.green();
                    float blue = container.blue();
                    DebugRenderer.drawBox(matrices, vertexConsumers, box, red, green, blue, alpha);
                }
            }

        } catch (Exception e) {
            ChestsToFox.LOGGER.error("cube rendering error", e);
        }
    }
}
