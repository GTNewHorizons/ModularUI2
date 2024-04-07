package com.cleanroommc.modularui.future;

import static com.google.common.primitives.Ints.saturatedCast;

import java.util.ArrayList;
import java.util.List;

import com.cleanroommc.modularui.api.IItemStackLong;
import com.cleanroommc.modularui.utils.item.ItemStackLongDelegate;

import net.minecraft.item.ItemStack;

public interface IItemHandlerLong extends IItemHandlerModifiable {

    void setStackInSlotLong(int slot, IItemStackLong stack);

    @Override
    default void setStackInSlot(int slot, ItemStack stack) {
        setStackInSlotLong(slot, new ItemStackLongDelegate(stack));
    }

    IItemStackLong extractItemLong(int slot, long amount, boolean simulate);

    @Override
    default ItemStack extractItem(int slot, int amount, boolean simulate) {
        IItemStackLong item = extractItemLong(slot, amount, simulate);
        return item == null ? null : item.getAsItemStack();
    }

    long getSlotLimitLong(int slot);

    @Override
    default int getSlotLimit(int slot) {
        return saturatedCast(getSlotLimitLong(slot));
    }

    IItemStackLong getStackInSlotLong(int slot);

    @Override
    default ItemStack getStackInSlot(int slot) {
        IItemStackLong item = getStackInSlotLong(slot);
        return item == null ? null : item.getAsItemStack();
    }

    default List<IItemStackLong> getStacksLong() {
        List<IItemStackLong> ret = new ArrayList<>(); for (int i = 0; i < getSlots(); i++) {
            ret.add(getStackInSlotLong(i));
        }
        return ret;
    }

    IItemStackLong insertItemLong(int slot, IItemStackLong stack, boolean simulate);

    @Override
    default ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        IItemStackLong item = insertItemLong(slot, new ItemStackLongDelegate(stack), simulate);
        return item == null ? null : item.getAsItemStack();
    }

    default boolean isItemValidLong(int slot, IItemStackLong stack) {
        return true;
    }

    @Override
    default boolean isItemValid(int slot, ItemStack stack) {
        return isItemValidLong(slot, new ItemStackLongDelegate(stack));
    }

}