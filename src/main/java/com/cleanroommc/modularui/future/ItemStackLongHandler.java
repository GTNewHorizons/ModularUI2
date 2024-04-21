package com.cleanroommc.modularui.future;

import java.util.Arrays;
import java.util.List;

import com.cleanroommc.modularui.api.IItemStackLong;
import com.cleanroommc.modularui.utils.item.ItemStackLong;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class ItemStackLongHandler implements IItemHandlerLong, INBTSerializable<NBTTagCompound> {

    private List<IItemStackLong> items;
    private long slotLimit;

    public ItemStackLongHandler(int slots, long slotLimit) {
        ItemStackLong[] items = new ItemStackLong[slots];
        Arrays.fill(items, null);
        this.items = Arrays.asList(items);
        this.slotLimit = slotLimit;
    }

    public ItemStackLongHandler(List<IItemStackLong> items) {
        this.items = items;
    }

    public ItemStackLongHandler(ItemStackLong[] items) {
        this.items = Arrays.asList(items);
    }

    @Override
    public int getSlots() {
        return items.size();
    }

    @Override
    public void setStackInSlotLong(int slot, IItemStackLong stack) {
        items.set(slot, stack == null ? null : new ItemStackLong(stack.getItem(), getSlotLimitLong(slot), stack.getItemDamage(), stack.getStackSize(), stack.getTagCompound()));
    }

    @Override
    public IItemStackLong extractItemLong(int slot, long amount, boolean simulate) {
        if (amount == 0) {
            return null;
        }
        this.validateSlotIndex(slot);
        IItemStackLong existing = this.items.get(slot);
        if (existing == null) {
            return null;
        }
        long toExtract = Math.min(amount, existing.getMaxStackSize());
        if (existing.getStackSize() <= toExtract) {
            if (!simulate) {
                this.items.set(slot, null);
                this.onContentsChanged(slot);
            }

            return existing;
        } else {
            if (!simulate) {
                this.items.set(
                    slot,
                    ItemHandlerHelper.copyStackWithSize(existing, existing.getStackSize() - toExtract));
                this.onContentsChanged(slot);
            }

            return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
        }

    }

    @Override
    public long getSlotLimitLong(int slot) {
        return slotLimit;
    }

    @Override
    public IItemStackLong getStackInSlotLong(int slot) {
        return items.get(slot);
    }

    @Override
    public IItemStackLong insertItemLong(int slot, IItemStackLong stack, boolean simulate) {
        if (stack == null) {
            return null;
        } else {
            this.validateSlotIndex(slot);
            IItemStackLong existing = this.items.get(slot);
            long limit = getSlotLimitLong(slot);
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
                        this.items.set(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
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
    public NBTTagCompound serializeNBT() {
        NBTTagList nbtTagList = new NBTTagList();

        for (int i = 0; i < this.items.size(); ++i) {
            if (this.items.get(i) != null) {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setInteger("Slot", i);
                this.items.get(i).writeToNBT(itemTag);
                nbtTagList.appendTag(itemTag);
            }
        }

        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("Items", nbtTagList);
        nbt.setInteger("Size", this.items.size());
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.setSize(nbt.hasKey("Size", 3) ? nbt.getInteger("Size") : this.items.size());
        NBTTagList tagList = nbt.getTagList("Items", 10);

        for (int i = 0; i < tagList.tagCount(); ++i) {
            NBTTagCompound itemTags = tagList.getCompoundTagAt(i);
            int slot = itemTags.getInteger("Slot");
            if (slot >= 0 && slot < this.items.size()) {
                this.items.set(slot, IItemStackLong.loadItemStackFromNBT(itemTags));
            }
        }

        this.onLoad();
    }

    protected void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= this.items.size()) {
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + this.items.size() + ")");
        }
    }

    public void setSize(int size) {
        IItemStackLong[] stacks = new IItemStackLong[size];
        Arrays.fill(stacks, null);
        this.items = Arrays.asList(stacks);
    }

    protected void onLoad() {}

    protected void onContentsChanged(int slot) {}

}
