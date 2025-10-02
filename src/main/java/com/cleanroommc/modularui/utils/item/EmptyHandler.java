package com.cleanroommc.modularui.utils.item;

import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public class EmptyHandler implements IItemHandlerModifiable {
    public static final IItemHandlerModifiable INSTANCE = new EmptyHandler();

    @Override
    public int getSlots() {
        return 0;
    }

    @Override
    @Nullable
    public ItemStack getStackInSlot(int slot) {
        return null;
    }

    @Override
    @Nullable
    public ItemStack insertItem(int slot, @Nullable ItemStack stack, boolean simulate) {
        return stack;
    }

    @Override
    @Nullable
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return null;
    }

    @Override
    public void setStackInSlot(int slot, @Nullable ItemStack stack) {
        // nothing to do here
    }

    @Override
    public int getSlotLimit(int slot) {
        return 0;
    }

    @Override
    public boolean isItemValid(int slot, @Nullable ItemStack stack) {
        return false;
    }
}
