package com.adamcalculator.cheststofox;

import com.adamcalculator.cheststofox.container.ContainerEntry;
import net.minecraft.util.math.BlockPos;

public class StateMachine {

    public static boolean clearOnce;

    public static boolean isRenderCustomContainers() {
        return true;
    }

    public static boolean isRenderHighlightedContainer(BlockPos basePos, ContainerEntry chest) {
        return true;
    }
}
