package com.adamcalculator.cheststofox.util;

import com.adamcalculator.cheststofox.ChestsToFox;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public final class ContainerLinker {
    @Nullable private BlockPos waitingPos;
    private long startWaitAt;
    private boolean skipOnce;

    public void waitContainerDataForOpen(BlockPos pos) {
        this.waitingPos = pos;
        this.startWaitAt = System.currentTimeMillis();
    }

    @Nullable
    public BlockPos getAndDone() {
        long diff = System.currentTimeMillis() - startWaitAt;
        ChestsToFox.LOGGER.info("ChestLinker done by " + diff + "ms");

        BlockPos temp = this.waitingPos;
        this.waitingPos = null;
        return temp;
    }

    public BlockPos get() {
        return waitingPos;
    }

    public void skipOnce() {
        this.skipOnce = true;
    }

    public boolean isSkipOnceAndMakeDone() {
        boolean b = this.skipOnce;
        this.skipOnce = false;
        return b;
    }

    public boolean isSkipOnce() {
        return this.skipOnce;
    }
}
