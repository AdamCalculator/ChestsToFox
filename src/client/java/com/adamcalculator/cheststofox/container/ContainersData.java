package com.adamcalculator.cheststofox.container;

import com.google.gson.annotations.SerializedName;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ContainersData {
    // <position> <chestEntry object>
    @SerializedName("chests")
    private HashMap<String, ContainerEntry> containersList = new HashMap<>();
    private BlockPos[] cachedSavedPositions = new BlockPos[0];


    public void updateContainerAt(BlockPos pos, Inventory inventory) {
        ContainerEntry chestEntry = getOrCreate(pos);
        chestEntry.overwriteFromInventory(inventory);
    }

    public boolean isExist(BlockPos pos) {
        String key = posToKey(pos);
        return containersList.containsKey(key);
    }

    public ContainerEntry getContainer(BlockPos basePos) {
        String key = posToKey(basePos);
        return containersList.get(key);
    }

    public ContainerEntry getContainer(String key) {
        return containersList.get(key);
    }

    public BlockPos[] getSavedPositions() {
        return cachedSavedPositions;
    }

    private ContainerEntry getOrCreate(BlockPos pos) {
        String key = posToKey(pos);
        if (containersList.containsKey(key)) {
            return containersList.get(key);
        }

        ContainerEntry chestEntry = new ContainerEntry();
        containersList.put(key, chestEntry);
        recalculateCache();

        return chestEntry;
    }

    public void clearAllMemory() {
        containersList.clear();
        recalculateCache();
    }


    public void clearAt(BlockPos baseBlock) {
        containersList.remove(posToKey(baseBlock));
        recalculateCache();
    }

    protected void recalculateCache() {
        List<BlockPos> l = new ArrayList<>();

        for (String s : containersList.keySet()) {
            String[] split = s.split(" ");
            BlockPos blockPos = new BlockPos(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
            l.add(blockPos);
        }

        cachedSavedPositions = l.toArray(new BlockPos[0]);
    }

    public static String posToKey(BlockPos pos) {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }

    public boolean isEmpty() {
        return containersList.isEmpty();
    }

}
