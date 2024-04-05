package com.cleanroommc.modularui.future;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import static com.google.common.primitives.Ints.saturatedCast;

import javax.annotation.Nullable;

import com.cleanroommc.modularui.api.IItemStackLong;
import com.cleanroommc.modularui.utils.item.ItemStackLongDelegate;

public class SlotItemHandler extends Slot {

    private static IInventory emptyInventory = new InventoryBasic("[Null]", true, 0);
    private final IItemHandler itemHandler;
    private final int index;

    public SlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(emptyInventory, index, xPosition, yPosition);
        this.itemHandler = itemHandler;
        this.index = index;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return isItemValid(new ItemStackLongDelegate(stack));
    }

    public boolean isItemValid(IItemStackLong stack) {
        if (stack != null && this.itemHandler.isItemValid(this.index, stack)) {
            IItemHandler handler = this.getItemHandler();
            IItemStackLong remainder;
            if (handler instanceof IItemHandlerModifiable) {
                IItemHandlerModifiable handlerModifiable = (IItemHandlerModifiable) handler;
                IItemStackLong currentStack = handlerModifiable.getStackInSlot(this.index);
                handlerModifiable.setStackInSlot(this.index, null);
                remainder = handlerModifiable.insertItem(this.index, stack, true);
                handlerModifiable.setStackInSlot(this.index, currentStack);
            } else {
                remainder = handler.insertItem(this.index, stack, true);
            }

            return remainder != null ? remainder.getStackSize() < stack.getStackSize() : stack.getStackSize() > 0;
        } else {
            return false;
        }
    }

    @Override
    public ItemStack getStack() {
        IItemStackLong longStack = getStackLong();
        if (longStack == null) return null;
        return longStack.getAsItemStack();
    }

    public IItemStackLong getStackLong() {
        return getItemHandler().getStackInSlot(this.index);
    }

    @Override
    public void putStack(ItemStack stack) {
        if (stack == null) {
            putStackLong(null);
            return;
        }
        putStackLong(new ItemStackLongDelegate(stack));
    }

    // Override if your IItemHandler does not implement IItemHandlerModifiable
    public void putStackLong(IItemStackLong stack) {
        ((IItemHandlerModifiable) this.getItemHandler()).setStackInSlot(this.index, stack);
        this.onSlotChanged();
    }

    @Override
    public void onSlotChange(ItemStack p_75220_1_, ItemStack p_75220_2_) {}

    @Override
    public int getSlotStackLimit() {
        return saturatedCast(getSlotStackLimitLong());
    }

    public long getSlotStackLimitLong() {
        return itemHandler.getSlotLimit(index);
    }

    public long getItemStackLimit(ItemStack stack) {
        IItemStackLong maxAdd = new ItemStackLongDelegate(stack.copy());
        int maxInput = stack.getMaxStackSize();
        maxAdd.setStackSize(maxInput);
        IItemHandler handler = this.getItemHandler();
        IItemStackLong currentStack = handler.getStackInSlot(this.index);
        if (handler instanceof IItemHandlerModifiable) {
            IItemHandlerModifiable handlerModifiable = (IItemHandlerModifiable) handler;
            handlerModifiable.setStackInSlot(this.index, null);
            IItemStackLong remainder = handlerModifiable.insertItem(this.index, maxAdd, true);
            handlerModifiable.setStackInSlot(this.index, currentStack);
            return remainder != null ? maxInput - remainder.getStackSize() : maxInput;
        } else {
            IItemStackLong remainder = handler.insertItem(this.index, maxAdd, true);
            long current = currentStack != null ? currentStack.getStackSize() : 0;
            long added = remainder != null ? maxInput - remainder.getStackSize() : maxInput;
            return current + added;
        }
    }

    @Override
    public boolean canTakeStack(EntityPlayer playerIn) {
        // make a best effort guess at checking whether this handler allows extraction
        return this.getItemHandler().getStackInSlot(this.index) == null
                || this.getItemHandler().extractItem(this.index, 1, true) != null;
    }

    @Override
    @Nullable
    public ItemStack decrStackSize(int amount) {
        IItemStackLong item = getItemHandler().extractItem(this.index, amount, false);
        return item == null ? null : item.getAsItemStack();
    }

    public IItemHandler getItemHandler() {
        return this.itemHandler;
    }

    public boolean isSameInventory(Slot other) {
        return other instanceof SlotItemHandler && ((SlotItemHandler) other).getItemHandler() == this.itemHandler;
    }
}