package com.adamcalculator.cheststofox.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public final class Util {
    private Util() {}

    // get right part of chest
    public static BlockPos getBaseChestBlockPos(ChestBlockEntity chestBlock) {
        return getBaseChestBlockPos(chestBlock, -1);
    }

    // get left part of chest
    public static BlockPos getAppendixChestBlockPos(ChestBlockEntity chestBlock) {
        return getBaseChestBlockPos(chestBlock, 1);
    }

    public static boolean isDoubleChest(ChestBlockEntity chestBlock) {
        BlockState cachedState = chestBlock.getCachedState();
        ChestType chestType = cachedState.get(ChestBlock.CHEST_TYPE);
        return chestType != ChestType.SINGLE;
    }

    public static boolean isAppendixChest(ChestBlockEntity chestBlock) {
        BlockState cachedState = chestBlock.getCachedState();
        ChestType chestType = cachedState.get(ChestBlock.CHEST_TYPE);
        return chestType == ChestType.LEFT;
    }

    public static BlockPos getBaseChestBlockPos(ChestBlockEntity chestBlock, int offset) {
        BlockPos pos = chestBlock.getPos();
        BlockState cachedState = chestBlock.getCachedState();
        ChestType chestType = cachedState.get(ChestBlock.CHEST_TYPE);
        var facing = cachedState.get(ChestBlock.FACING);
        var direction = facing.getOpposite();

        return switch (chestType) {
            case RIGHT, SINGLE -> pos;
            case LEFT -> switch (direction) {
                case NORTH -> pos.east(offset);
                case EAST -> pos.south(offset);
                case SOUTH -> pos.west(offset);
                case WEST -> pos.north(offset);
                default -> throw new RuntimeException("Chest with unsupported direction: " + direction);
            };
        };

    }

    public static <K, V> LinkedHashMap<K, V> sortHashmapByValues(HashMap<K, V> map, Comparator<V> comparator) {
        LinkedHashMap<K, V> sortedMap = new LinkedHashMap<>();
        ArrayList<V> list = new ArrayList<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            list.add(entry.getValue());
        }
        list.sort(comparator);
        for (V num : list) {
            for (Map.Entry<K, V> entry : map.entrySet()) {
                if (entry.getValue().equals(num)) {
                    sortedMap.put(entry.getKey(), num);
                }
            }
        }

        return sortedMap;
    }

    public static <K extends String, V> LinkedHashMap<K, V> sortHashmapByKeys(HashMap<K, V> map, Comparator<K> comparator) {
        ArrayList<K> sortedKeys = new ArrayList<>(map.keySet());

        sortedKeys.sort(comparator);

        LinkedHashMap<K, V> sortedMap = new LinkedHashMap<>();

        for (K sortedKey : sortedKeys) {
            sortedMap.put(sortedKey, map.get(sortedKey));
        }

        return sortedMap;
    }
}
