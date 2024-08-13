package com.cleanroommc.modularui.utils.item;

import com.cleanroommc.modularui.api.IItemStackLong;

public class EmptyHandlerLong implements IItemHandlerLong {

    public static final IItemHandlerLong INSTANCE = new EmptyHandlerLong();

    @Override
    public void setStackInSlotLong(int slot, IItemStackLong stack) {
        // nothing to do here
    }

    @Override
    public IItemStackLong extractItemLong(int slot, long amount, boolean simulate) {
        return null;
    }

    @Override
    public long getSlotLimitLong(int slot) {
        return 0;
    }

    @Override
    public IItemStackLong getStackInSlotLong(int slot) {
        return null;
    }

    @Override
    public IItemStackLong insertItemLong(int slot, IItemStackLong stack, boolean simulate) {
        return null;
    }

    @Override
    public int getSlots() {
        return 0;
    }
}
