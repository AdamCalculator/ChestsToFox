package com.adamcalculator.cheststofox.mixin.client;

import com.adamcalculator.cheststofox.RenderInjecting;
import com.adamcalculator.cheststofox.container.ContainerManager;
import net.minecraft.block.entity.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public abstract class DebugRendererMixin {
    @Inject(at = @At("HEAD"), method = "render")
    private void render(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        final MinecraftClient client = MinecraftClient.getInstance();
        final Profiler profiler = client.getProfiler();

        profiler.push("mixin:ChestsToFox-highlights");
        for (final BlockPos pos : ContainerManager.getSavedPositions()) {
            matrices.push();
            matrices.translate((double)pos.getX() - cameraX, (double)pos.getY() - cameraY, (double)pos.getZ() - cameraZ);

            final BlockEntity blockEntity = client.world != null ? client.world.getBlockEntity(pos) : null;
            if (blockEntity instanceof ChestBlockEntity chestBlock) {
                RenderInjecting.renderChestBlockEntity(chestBlock, matrices, vertexConsumers, ci);

            } else if (blockEntity instanceof HopperBlockEntity hopperBlockEntity) {
                RenderInjecting.renderHopperBlockEntity(hopperBlockEntity, matrices, vertexConsumers, ci);

            } else if (blockEntity instanceof BarrelBlockEntity barrelBlockEntity) {
                RenderInjecting.renderCube(barrelBlockEntity.getPos(), 0.5f, matrices, vertexConsumers, ci);

            } else if (blockEntity instanceof ShulkerBoxBlockEntity shulkerboxBlockEntity) {
                RenderInjecting.renderCube(shulkerboxBlockEntity.getPos(), 0.3f, matrices, vertexConsumers, ci);
            } else {
                RenderInjecting.renderCube(pos, 0.2f, matrices, vertexConsumers, ci);
            }


            matrices.pop();
        }
        profiler.pop();
    }
}
