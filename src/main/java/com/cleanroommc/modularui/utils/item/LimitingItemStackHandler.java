package com.cleanroommc.modularui.utils.item;

import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * {@link ItemStackHandler} that you can set limit for number of items in slots.
 */
public class LimitingItemStackHandler extends ItemStackHandler {

    private final int limit;

    public LimitingItemStackHandler(int limit) {
        this.limit = limit;
    }

    public LimitingItemStackHandler(int slots, int limit) {
        super(slots);
        this.limit = limit;
    }

    public LimitingItemStackHandler(List<ItemStack> stacks, int limit) {
        super(stacks);
        this.limit = limit;
    }

    public LimitingItemStackHandler(ItemStack[] stacks, int limit) {
        super(stacks);
        this.limit = limit;
    }

    @Override
    public int getSlotLimit(int slot) {
        return limit;
    }
}
