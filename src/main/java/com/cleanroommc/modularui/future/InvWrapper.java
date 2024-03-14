package com.cleanroommc.modularui.future;

import net.minecraft.inventory.IInventory;

import static com.google.common.primitives.Ints.saturatedCast;

import java.util.Objects;

import com.cleanroommc.modularui.api.IItemStackLong;
import com.cleanroommc.modularui.utils.item.ItemStackLongDelegate;

public class InvWrapper implements IItemHandlerModifiable {

    private final IInventory inv;

    public InvWrapper(IInventory inv) {
        this.inv = Objects.requireNonNull(inv);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InvWrapper that = (InvWrapper) o;

        return getInv().equals(that.getInv());
    }

    @Override
    public int hashCode() {
        return getInv().hashCode();
    }

    @Override
    public int getSlots() {
        return getInv().getSizeInventory();
    }

    @Override
    public IItemStackLong getStackInSlot(int slot) {
        return new ItemStackLongDelegate(getInv().getStackInSlot(slot));
    }

    @Override
    public IItemStackLong insertItem(int slot, IItemStackLong stack, boolean simulate) {
        if (stack == null) return null;

        IItemStackLong stackInSlot = new ItemStackLongDelegate(getInv().getStackInSlot(slot));

        long m;
        if (stackInSlot.getAsItemStack() != null) {
            if (stackInSlot.getStackSize() >= Math.min(stackInSlot.getMaxStackSize(), getSlotLimit(slot))) return stack;

            if (!ItemHandlerHelper.canItemStacksStack(stack, stackInSlot)) return stack;

            if (!getInv().isItemValidForSlot(slot, stack.getAsItemStack())) return stack;

            m = Math.min(stack.getMaxStackSize(), getSlotLimit(slot)) - stackInSlot.getStackSize();

            if (stack.getStackSize() <= m) {
                if (!simulate) {
                    IItemStackLong copy = stack.copy();
                    copy.setStackSize(copy.getStackSize() + stackInSlot.getStackSize());
                    getInv().setInventorySlotContents(slot, copy.getAsItemStack());
                    getInv().markDirty();
                }

                return null;
            } else {
                // copy the stack to not modify the original one
                stack = stack.copy();
                if (!simulate) {
                    IItemStackLong copy = stack.splitStack(m);
                    copy.setStackSize(copy.getStackSize() + stackInSlot.getStackSize());
                    getInv().setInventorySlotContents(slot, copy.getAsItemStack());
                    getInv().markDirty();
                } else {
                    stack.setStackSize(stack.getStackSize() - m);
                }
                return stack;
            }
        } else {
            if (!getInv().isItemValidForSlot(slot, stack.getAsItemStack())) return stack;

            m = Math.min(stack.getMaxStackSize(), getSlotLimit(slot));
            if (m < stack.getStackSize()) {
                // copy the stack to not modify the original one
                stack = stack.copy();
                if (!simulate) {
                    getInv().setInventorySlotContents(slot, stack.splitStack(m).getAsItemStack());
                    getInv().markDirty();
                } else {
                    stack.setStackSize(stack.getStackSize() - m);
                }
                return stack;
            } else {
                if (!simulate) {
                    getInv().setInventorySlotContents(slot, stack.getAsItemStack());
                    getInv().markDirty();
                }
                return null;
            }
        }
    }

    @Override
    public IItemStackLong extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) return null;

        IItemStackLong stackInSlot = new ItemStackLongDelegate(getInv().getStackInSlot(slot));

        if (stackInSlot.getAsItemStack() == null) return null;

        if (simulate) {
            if (stackInSlot.getStackSize() < amount) {
                return stackInSlot.copy();
            } else {
                IItemStackLong copy = stackInSlot.copy();
                copy.setStackSize(amount);
                return copy;
            }
        } else {
            long m = Math.min(stackInSlot.getStackSize(), amount);

            IItemStackLong decrStackSize = new ItemStackLongDelegate(getInv().decrStackSize(slot, saturatedCast(m)));
            getInv().markDirty();
            return decrStackSize;
        }
    }

    @Override
    public void setStackInSlot(int slot, IItemStackLong stack) {
        getInv().setInventorySlotContents(slot, stack == null ? null : stack.getAsItemStack());
    }

    @Override
    public long getSlotLimit(int slot) {
        return getInv().getInventoryStackLimit();
    }

    @Override
    public boolean isItemValid(int slot, IItemStackLong stack) {
        return getInv().isItemValidForSlot(slot, stack.getAsItemStack());
    }

    @Deprecated
    public IInventory getInv() {
        return inv;
    }
}