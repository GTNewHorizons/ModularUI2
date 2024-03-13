package com.cleanroommc.modularui.utils.item;

import com.cleanroommc.modularui.api.IItemStackLong;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemStackLong implements IItemStackLong {

    private long stackSize;
    private long maxStackSize;
    private Item item;
    private int damage;
    private NBTTagCompound nbt;

    @Override
    public long getStackSizeLong() {
        return stackSize;
    }

    @Override
    public long getMaxStackSizeLong() {
        return maxStackSize;
    }

    @Override
    public Item getItem() {
        return item;
    }

    @Override
    public int getDamage() {
        return damage;
    }

    @Override
    public NBTTagCompound getNBT() {
        return nbt;
    }

    @Override
    public void setStackSizeLong(long newStackSize) {
        stackSize = newStackSize;
    }

    @Override
    public ItemStack getAsItemStack() {
        return new ItemStack(item);
    }
}