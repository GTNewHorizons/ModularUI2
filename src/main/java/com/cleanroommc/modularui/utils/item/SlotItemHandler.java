package com.cleanroommc.modularui.utils.item;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public class SlotItemHandler extends Slot {

    private static final IInventory emptyInventory = new InventoryBasic("[Null]", true, 0);
    private final IItemHandler itemHandler;
    private final int index;

    public SlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(emptyInventory, index, xPosition, yPosition);
        this.itemHandler = itemHandler;
        this.index = index;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        if (stack != null && this.itemHandler.isItemValid(this.index, stack)) {
            IItemHandler handler = this.getItemHandler();
            ItemStack remainder;
            if (handler instanceof IItemHandlerModifiable handlerModifiable) {
                ItemStack currentStack = handlerModifiable.getStackInSlot(this.index);
                handlerModifiable.setStackInSlot(this.index, null);
                remainder = handlerModifiable.insertItem(this.index, stack, true);
                handlerModifiable.setStackInSlot(this.index, currentStack);
            } else {
                remainder = handler.insertItem(this.index, stack, true);
            }

            return remainder != null ? remainder.stackSize < stack.stackSize : stack.stackSize > 0;
        } else {
            return false;
        }
    }

    @Override
    public ItemStack getStack() {
        return itemHandler.getStackInSlot(index);
    }

    @Override
    public void putStack(ItemStack stack) {
        ((IItemHandlerModifiable) this.getItemHandler()).setStackInSlot(this.index, stack);
        this.onSlotChanged();
    }

    @Override
    public void onSlotChange(ItemStack p_75220_1_, ItemStack p_75220_2_) {}

    @Override
    public int getSlotStackLimit() {
        return itemHandler.getSlotLimit(index);
    }

    public int getItemStackLimit(ItemStack stack) {
        ItemStack maxAdd = stack.copy();
        int maxInput = stack.getMaxStackSize();
        maxAdd.stackSize = maxInput;
        IItemHandler handler = this.getItemHandler();
        ItemStack currentStack = handler.getStackInSlot(this.index);
        if (handler instanceof IItemHandlerModifiable handlerModifiable) {
            handlerModifiable.setStackInSlot(this.index, null);
            ItemStack remainder = handlerModifiable.insertItem(this.index, maxAdd, true);
            handlerModifiable.setStackInSlot(this.index, currentStack);
            return remainder != null ? maxInput - remainder.stackSize : maxInput;
        } else {
            ItemStack remainder = handler.insertItem(this.index, maxAdd, true);
            int current = currentStack != null ? currentStack.stackSize : 0;
            int added = remainder != null ? maxInput - remainder.stackSize : maxInput;
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
        return getItemHandler().extractItem(this.index, amount, false);
    }

    public IItemHandler getItemHandler() {
        return this.itemHandler;
    }

    public boolean isSameInventory(Slot other) {
        return other instanceof SlotItemHandler slotHand && slotHand.getItemHandler() == this.itemHandler;
    }

    @Override
    public boolean isSlotInInventory(IInventory inventory, int invIndex) {
        return itemHandler.isSlotFromInventory(this.index, inventory, invIndex);
    }
}
