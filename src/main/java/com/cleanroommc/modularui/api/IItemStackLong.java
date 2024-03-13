package com.cleanroommc.modularui.api;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public interface IItemStackLong {

    /**
     * @return The stacksize the item has currently
     */
    long getStackSizeLong();

    /**
     * @return The max stacksize of the item
     */
    long getMaxStackSizeLong();

    /**
     * @return The item it was created from
     */
    Item getItem();

    /**
     * @return The damage the item has. Its meta id
     */
    int getDamage();

    /**
     * @return NBT data the item holds
     */
    NBTTagCompound getNBT();

    /**
     * @param newStackSize The new stacksize the itemstack should have
     */
    void setStackSizeLong(long newStackSize);

    /**
     * @return The current ItemStackLong as a ItemStack, this stack shouldn't be edited as it won't reflect upon the real one
     */
    ItemStack getAsItemStack();
}