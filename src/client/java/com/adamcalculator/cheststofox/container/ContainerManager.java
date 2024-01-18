package com.adamcalculator.cheststofox.container;

import com.adamcalculator.cheststofox.ChestsToFox;
import com.google.gson.GsonBuilder;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;

public class ContainerManager {
    public static final ContainersData containersData = new ContainersData();

    public static void updateContainerData(BlockPos pos, Inventory inventory) {
        containersData.updateContainerAt(pos, inventory);
    }

    public static String export() {
        String s = new GsonBuilder().setPrettyPrinting().create().toJson(containersData, ContainersData.class);
        ChestsToFox.LOGGER.info(s);
        return s;
    }

    public static boolean isContainerAtExists(BlockPos pos) {
        return containersData.isExist(pos);
    }

    public static ContainerEntry getContainer(BlockPos basePos) {
        return containersData.getContainer(basePos);
    }

    public static ContainerEntry getContainer(String basePos) {
        return containersData.getContainer(basePos);
    }

    public static BlockPos[] getSavedPositions() {
        return containersData.getSavedPositions();
    }

    public static boolean isNotEmpty() {
        return !containersData.isEmpty();
    }

    public static void clearAllMemory() {
        containersData.clearAllMemory();
    }

    public static void clearAt(BlockPos baseBlock) {
        containersData.clearAt(baseBlock);
    }
}
