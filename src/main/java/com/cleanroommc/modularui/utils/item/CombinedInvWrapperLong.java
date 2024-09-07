package com.cleanroommc.modularui.utils.item;

public class CombinedInvWrapperLong implements IItemHandlerLong {

    protected final IItemHandlerLong[] itemHandler; // the handlers
    protected final int[] baseIndex; // index-offsets of the different handlers
    protected final int slotCount; // number of total slots

    public CombinedInvWrapperLong(IItemHandlerLong... itemHandler) {
        this.itemHandler = itemHandler;
        this.baseIndex = new int[itemHandler.length];
        int index = 0;
        for (int i = 0; i < itemHandler.length; i++) {
            index += itemHandler[i].getSlots();
            baseIndex[i] = index;
        }
        this.slotCount = index;
    }

    // returns the handler index for the slot
    protected int getIndexForSlot(int slot) {
        if (slot < 0) {
            return -1;
        }

        for (int i = 0; i < baseIndex.length; i++) {
            if (slot - baseIndex[i] < 0) {
                return i;
            }
        }
        return -1;
    }

    protected IItemHandlerLong getHandlerFromIndex(int index) {
        if (index < 0 || index >= itemHandler.length) {
            return EmptyHandlerLong.INSTANCE;
        }
        return itemHandler[index];
    }

    protected int getSlotFromIndex(int slot, int index) {
        if (index <= 0 || index >= baseIndex.length) {
            return slot;
        }
        return slot - baseIndex[index - 1];
    }

    @Override
    public void setStackInSlotLong(int slot, IItemStackLong stack) {
        int index = getIndexForSlot(slot);
        IItemHandlerLong handler = getHandlerFromIndex(index);
        slot = getSlotFromIndex(slot, index);
        handler.setStackInSlotLong(slot, stack);
    }

    @Override
    public int getSlots() {
        return slotCount;
    }

    @Override
    public IItemStackLong getStackInSlotLong(int slot) {
        int index = getIndexForSlot(slot);
        IItemHandlerLong handler = getHandlerFromIndex(index);
        slot = getSlotFromIndex(slot, index);
        return handler.getStackInSlotLong(slot);
    }

    @Override
    public IItemStackLong insertItemLong(int slot, IItemStackLong stack, boolean simulate) {
        int index = getIndexForSlot(slot);
        IItemHandlerLong handler = getHandlerFromIndex(index);
        slot = getSlotFromIndex(slot, index);
        return handler.insertItemLong(slot, stack, simulate);
    }

    @Override
    public IItemStackLong extractItemLong(int slot, long amount, boolean simulate) {
        int index = getIndexForSlot(slot);
        IItemHandlerLong handler = getHandlerFromIndex(index);
        slot = getSlotFromIndex(slot, index);
        return handler.extractItemLong(slot, amount, simulate);
    }

    @Override
    public long getSlotLimitLong(int slot) {
        int index = getIndexForSlot(slot);
        IItemHandlerLong handler = getHandlerFromIndex(index);
        int localSlot = getSlotFromIndex(slot, index);
        return handler.getSlotLimitLong(localSlot);
    }

    @Override
    public boolean isItemValidLong(int slot, IItemStackLong stack) {
        int index = getIndexForSlot(slot);
        IItemHandlerLong handler = getHandlerFromIndex(index);
        int localSlot = getSlotFromIndex(slot, index);
        return handler.isItemValidLong(localSlot, stack);
    }
}
