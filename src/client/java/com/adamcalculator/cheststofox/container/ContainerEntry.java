package com.adamcalculator.cheststofox.container;

import com.google.gson.annotations.SerializedName;
import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.HashMap;

// chests slot:item
public class ContainerEntry {
    @SerializedName("slots")
    private HashMap<Integer, ItemEntry> slots = new HashMap<>();

    public void overwriteFromInventory(Inventory inventory) {
        slots.clear();

        for (int stack = 0; stack < inventory.size(); stack++) {
            final ItemStack itemStack = inventory.getStack(stack);
            final Item item = itemStack.getItem();

            if (itemStack.isEmpty()) {
                continue;
            }

            if (item instanceof BlockItem blockItem) {
                final Block block = blockItem.getBlock();
                if (block instanceof ShulkerBoxBlock) {
                    overwriteFromShulkerBox(stack, itemStack);
                    // add here 'continue' for remove shulkerbox from lists
                }
            }
            slots.put(stack, ItemEntry.ofItemStack(itemStack));
        }
    }

    private void overwriteFromShulkerBox(int slotInSource, ItemStack shulkerItemStack) {
        ContainerComponent containerComponent = shulkerItemStack.getOrDefault(DataComponentTypes.CONTAINER, null);
        if (containerComponent == null) {
            return;
        }
        int slotInShulker = 0;
        for (ItemStack itemStack : containerComponent.iterateNonEmpty()) {
            if (!itemStack.isEmpty()) {
                slots.put(((slotInSource+10)  * 1000) + slotInShulker, ItemEntry.ofItemStack(itemStack));
            }
            slotInShulker++;
        }
    }

    public float blue() {
        return (float) Math.sin((double)System.currentTimeMillis() / 2300) / 2f + 0.5f;//Color.ofHSB((float) Math.sin((double)System.currentTimeMillis() / 6000) / 2f + 1f, (float) Math.sin((double)System.currentTimeMillis() / 2400) / 2f + 1f, (float) Math.sin((double)System.currentTimeMillis() / 840*2) / 2f + 1f).getBlue();
    }

    public float green() {
        return (float) Math.sin((double)System.currentTimeMillis() / 1000) / 2f + 0.5f;//Color.ofHSB((float) Math.sin((double)System.currentTimeMillis() / 2000) / 2f + 1f, (float) Math.cos((double)System.currentTimeMillis() / 800) / 2f + 1f, (float) Math.sin((double)System.currentTimeMillis() / 600) / 2f + 1f).getGreen();
    }

    public float red() {
        return (float) Math.sin((double)System.currentTimeMillis() / 1500) / 2f + 0.5f;//Color.ofHSB((float) Math.sin((double)System.currentTimeMillis() / 2000) / 2f + 1f, (float) Math.sin((double)System.currentTimeMillis() / 3000) / 2f + 1f, (float) Math.sin((double)System.currentTimeMillis() / 12000) / 2f + 1f).getRed();
    }

    @Override
    public String toString() {
        return "ChestEntry{" +
                "slots=" + slots +
                '}';
    }

    public void iterate(ChestEntryIterator iter) {
        for (Integer slot : slots.keySet()) {
            ItemEntry itemEntry = slots.get(slot);
            iter.iterate(slot, itemEntry);
        }
    }

    public interface ChestEntryIterator {
        void iterate(int slot, ItemEntry itemEntry);
    }
}
