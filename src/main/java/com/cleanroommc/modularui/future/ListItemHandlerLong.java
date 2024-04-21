package com.cleanroommc.modularui.future;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.cleanroommc.modularui.api.IItemStackLong;

public class ListItemHandlerLong implements IItemHandlerLong {

    protected final Iterable<? extends IItemHandlerLong> listItemHandler;

    public ListItemHandlerLong(Iterable<? extends IItemHandlerLong> listItemHandler) {
        this.listItemHandler = listItemHandler;
    }

    @Override
    public int getSlots() {
        int ret = 0;
        for (IItemHandlerLong itemHandler : listItemHandler) {
            ret += itemHandler.getSlots();
        }
        return ret;
    }

    @Override
    public IItemStackLong getStackInSlotLong(int slot) {
        Pair<? extends IItemHandlerLong, Integer> result = findItemHandler(slot);
        return result.getLeft().getStackInSlotLong(result.getRight());
    }

    @Nullable
    @Override
    public IItemStackLong insertItemLong(int slot, IItemStackLong stack, boolean simulate) {
        Pair<? extends IItemHandlerLong, Integer> result = findItemHandler(slot);
        return result.getLeft().insertItemLong(result.getRight(), stack, simulate);
    }

    @Nullable
    @Override
    public IItemStackLong extractItemLong(int slot, long amount, boolean simulate) {
        Pair<? extends IItemHandlerLong, Integer> result = findItemHandler(slot);
        return result.getLeft().extractItemLong(result.getRight(), amount, simulate);
    }

    @Override
    public long getSlotLimitLong(int slot) {
        Pair<? extends IItemHandlerLong, Integer> result = findItemHandler(slot);
        return result.getLeft().getSlotLimitLong(result.getRight());
    }

    @Override
    public void setStackInSlotLong(int slot, IItemStackLong stack) {
        Pair<? extends IItemHandlerLong, Integer> result = findItemHandler(slot);
        result.getLeft().setStackInSlotLong(result.getRight(), stack);
    }

    @Override
    public boolean isItemValidLong(int slot, IItemStackLong stack) {
        Pair<? extends IItemHandlerLong, Integer> result = findItemHandler(slot);
        return result.getLeft().isItemValidLong(result.getRight(), stack);
    }

    /**
     * Searches all item handlers and find one matching handler that contains specified slot index
     *
     * @param slot Index to search
     * @return Pair of item handler and actual index for it
     */
    protected Pair<? extends IItemHandlerLong, Integer> findItemHandler(int slot) {
        int searching = 0;
        for (IItemHandlerLong itemHandler : listItemHandler) {
            int numSlots = itemHandler.getSlots();
            if (slot >= searching && slot < searching + numSlots) {
                return new ImmutablePair<>(itemHandler, slot - searching);
            }
            searching += numSlots;
        }
        throw new RuntimeException("Slot " + slot + " not in valid range - [0," + getSlots() + ")");
    }
}
