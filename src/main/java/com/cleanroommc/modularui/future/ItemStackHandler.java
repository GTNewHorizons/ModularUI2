package com.cleanroommc.modularui.future;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import javax.annotation.Nullable;

import com.cleanroommc.modularui.api.IItemStackLong;
import com.cleanroommc.modularui.utils.item.ItemStackLong;

import java.util.Arrays;
import java.util.List;

public class ItemStackHandler implements IItemHandlerModifiable, INBTSerializable<NBTTagCompound> {

    protected List<IItemStackLong> stacks;

    public ItemStackHandler() {
        this(1);
    }

    public ItemStackHandler(int size) {
        IItemStackLong[] stacks = new IItemStackLong[size];
        Arrays.fill(stacks, null);
        this.stacks = Arrays.asList(stacks);
    }

    public ItemStackHandler(List<IItemStackLong> stacks) {
        this.stacks = stacks;
    }

    public ItemStackHandler(IItemStackLong[] stacks) {
        this.stacks = Arrays.asList(stacks);
    }

    public void setSize(int size) {
        IItemStackLong[] stacks = new IItemStackLong[size];
        Arrays.fill(stacks, null);
        this.stacks = Arrays.asList(stacks);
    }

    @Override
    public void setStackInSlot(int slot, IItemStackLong stack) {
        this.validateSlotIndex(slot);
        this.stacks.set(slot, stack);
        this.onContentsChanged(slot);
    }

    @Override
    public int getSlots() {
        return this.stacks.size();
    }

    @Override
    public IItemStackLong getStackInSlot(int slot) {
        this.validateSlotIndex(slot);
        return this.stacks.get(slot);
    }

    @Override
    public IItemStackLong insertItem(int slot, IItemStackLong stack, boolean simulate) {
        if (stack == null) {
            return null;
        } else {
            this.validateSlotIndex(slot);
            IItemStackLong existing = this.stacks.get(slot);
            long limit = this.getStackLimit(slot, stack);
            if (existing != null) {
                if (!ItemHandlerHelper.canItemStacksStack(stack, existing)) {
                    return stack;
                }

                limit -= existing.getStackSize();
            }

            if (limit <= 0) {
                return stack;
            } else {
                boolean reachedLimit = stack.getStackSize() > limit;
                if (!simulate) {
                    if (existing == null) {
                        this.stacks.set(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
                    } else {
                        existing.setStackSize(existing.getStackSize() + (reachedLimit ? limit : stack.getStackSize()));
                    }

                    this.onContentsChanged(slot);
                }

                return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getStackSize() - limit) : null;
            }
        }
    }

    @Override
    public IItemStackLong extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) {
            return null;
        } else {
            this.validateSlotIndex(slot);
            IItemStackLong existing = this.stacks.get(slot);
            if (existing == null) {
                return null;
            } else {
                long toExtract = Math.min(amount, existing.getMaxStackSize());
                if (existing.getStackSize() <= toExtract) {
                    if (!simulate) {
                        this.stacks.set(slot, null);
                        this.onContentsChanged(slot);
                    }

                    return existing;
                } else {
                    if (!simulate) {
                        this.stacks.set(
                                slot,
                                ItemHandlerHelper.copyStackWithSize(existing, existing.getStackSize() - toExtract));
                        this.onContentsChanged(slot);
                    }

                    return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
                }
            }
        }
    }

    @Override
    public long getSlotLimit(int slot) {
        return getStackInSlot(slot) != null ? getStackInSlot(slot).getMaxStackSize() : 64;
    }

    protected long getStackLimit(int slot, @Nullable IItemStackLong stack) {
        if (stack == null) {
            return 0;
        }
        return Math.min(this.getSlotLimit(slot), stack.getMaxStackSize());
    }

    @Override
    public boolean isItemValid(int slot, IItemStackLong stack) {
        return true;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagList nbtTagList = new NBTTagList();

        for (int i = 0; i < this.stacks.size(); ++i) {
            if (this.stacks.get(i) != null) {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setInteger("Slot", i);
                this.stacks.get(i).writeToNBT(itemTag);
                nbtTagList.appendTag(itemTag);
            }
        }

        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("Items", nbtTagList);
        nbt.setInteger("Size", this.stacks.size());
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.setSize(nbt.hasKey("Size", 3) ? nbt.getInteger("Size") : this.stacks.size());
        NBTTagList tagList = nbt.getTagList("Items", 10);

        for (int i = 0; i < tagList.tagCount(); ++i) {
            NBTTagCompound itemTags = tagList.getCompoundTagAt(i);
            int slot = itemTags.getInteger("Slot");
            if (slot >= 0 && slot < this.stacks.size()) {
                this.stacks.set(slot, IItemStackLong.loadItemStackFromNBT(itemTags));
            }
        }

        this.onLoad();
    }

    protected void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= this.stacks.size()) {
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + this.stacks.size() + ")");
        }
    }

    protected void onLoad() {}

    protected void onContentsChanged(int slot) {}
}