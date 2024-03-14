package com.cleanroommc.modularui.utils;

import com.cleanroommc.modularui.api.IItemStackLong;
import com.cleanroommc.modularui.future.IItemHandlerModifiable;
import com.cleanroommc.modularui.future.ItemHandlerHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// nh todo: this doesn't work due to `ItemStackItemHandler#container` being different object from actual item stored in inv
public class ItemStackItemHandler implements IItemHandlerModifiable {

    private static final String KEY_ITEMS = "Items";

    private final ItemStack container;
    private final int slots;

    public ItemStackItemHandler(ItemStack container, int slots) {
        this.container = container;
        this.slots = slots;
    }

    @Override
    public int getSlots() {
        return this.slots;
    }

    @Nullable
    @Override
    public IItemStackLong getStackInSlot(int slot) {
        validateSlotIndex(slot);
        NBTTagCompound item = getItemsNbt().getCompoundTagAt(slot);
        return item == null ? null : IItemStackLong.loadItemStackFromNBT(item);
    }

    @Override
    public void setStackInSlot(int slot, @Nullable IItemStackLong stack) {
        validateSlotIndex(slot);
        NBTTagList list = getItemsNbt();
        list.func_150304_a(slot, stack == null ? new NBTTagCompound() : stack.writeToNBT(new NBTTagCompound()));
    }

    @Nullable
    @Override
    public IItemStackLong insertItem(int slot, @Nullable IItemStackLong stack, boolean simulate) {
        if (stack == null) return null;
        IItemStackLong existing = getStackInSlot(slot);

        long limit = getStackLimit(slot, stack);

        if (existing != null) {
            if (!ItemHandlerHelper.canItemStacksStack(stack, existing))
                return stack;

            limit -= existing.getStackSize();
        }

        if (limit <= 0) return stack;

        boolean reachedLimit = stack.getStackSize() > limit;

        if (!simulate) {
            if (existing == null) {
                setStackInSlot(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
            } else {
                existing.setStackSize(existing.getStackSize() + (reachedLimit ? limit :stack.getStackSize()));
            }
            onContentsChanged(slot);
        }

        return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getStackSize() - limit) : null;
    }

    @Nullable
    @Override
    public IItemStackLong extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) return null;

        IItemStackLong existing = getStackInSlot(slot);
        if (existing == null) return null;

        long toExtract = Math.min(amount, existing.getMaxStackSize());

        if (existing.getStackSize() <= toExtract) {
            if (!simulate) {
                setStackInSlot(slot, null);
                onContentsChanged(slot);
            }
            return existing;
        } else {
            if (!simulate) {
                setStackInSlot(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.getStackSize() - toExtract));
                onContentsChanged(slot);
            }

            return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
        }
    }

    @Override
    public long getSlotLimit(int slot) {
        return 64;
    }

    protected long getStackLimit(int slot, @Nonnull IItemStackLong stack) {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }

    protected void onContentsChanged(int slot) {
    }

    public NBTTagList getItemsNbt() {
        NBTTagCompound nbt = this.container.getTagCompound();
        if (nbt == null) {
            nbt = new NBTTagCompound();
            this.container.setTagCompound(nbt);
        }
        if (!nbt.hasKey(KEY_ITEMS)) {
            NBTTagList list = new NBTTagList();
            for (int i = 0; i < getSlots(); i++) {
                list.appendTag(new NBTTagCompound());
            }
            nbt.setTag(KEY_ITEMS, list);
        }
        return nbt.getTagList(KEY_ITEMS, Constants.NBT.TAG_COMPOUND);
    }

    protected void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= this.slots) {
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + this.slots + ")");
        }
    }
}