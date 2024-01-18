package com.adamcalculator.cheststofox.container;

import com.google.gson.annotations.SerializedName;
import net.minecraft.item.ItemStack;

public class ItemEntry {
    @SerializedName("id")
    private String id;
    @SerializedName("count")
    private int count;
    private String localized;

    public static ItemEntry ofItemStack(ItemStack stack) {
        ItemEntry itemEntry = new ItemEntry(stack.getItem().toString(), stack.getCount());
        itemEntry.localized = stack.getName().asTruncatedString(99);
        return itemEntry;
    }

    public ItemEntry(String id, int count) {
        this.id = id;
        this.count = count;
    }

    public String getId() {
        return id;
    }

    public int getCount() {
        return count;
    }

    public String getLocalized() {
        return localized;
    }
}
