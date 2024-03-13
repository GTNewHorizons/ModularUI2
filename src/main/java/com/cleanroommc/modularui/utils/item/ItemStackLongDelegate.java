package com.cleanroommc.modularui.utils.item;

import static com.google.common.primitives.Ints.saturatedCast;

import com.cleanroommc.modularui.api.IItemStackLong;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemStackLongDelegate implements IItemStackLong {

    private ItemStack item;

    @Override
    public long getStackSizeLong() {
        return item.stackSize;
    }

    @Override
    public long getMaxStackSizeLong() {
        return item.getMaxStackSize();
    }

    @Override
    public Item getItem() {
        return item.getItem();
    }

    @Override
    public int getDamage() {
        return item.getItemDamage();
    }

    @Override
    public NBTTagCompound getNBT() {
        return item.getTagCompound();
    }

    @Override
    public void setStackSizeLong(long newStackSize) {
        item.stackSize = saturatedCast(newStackSize);
    }

    @Override
    public ItemStack getAsItemStack() {
        return item;
    }
}