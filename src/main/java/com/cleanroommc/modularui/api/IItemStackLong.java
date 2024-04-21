package com.cleanroommc.modularui.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.cleanroommc.modularui.utils.item.ItemStackLong;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public interface IItemStackLong {

    /**
     * @return The stacksize the item has currently
     */
    long getStackSize();

    /**
     * @return The max stacksize of the item
     */
    long getMaxStackSize();

    /**
     * @return The item it was created from
     */
    Item getItem();

    /**
     * @return The damage the item has. Its meta id
     */
    int getItemDamage();

    /**
     * @return NBT data the item holds
     */
    @Nullable NBTTagCompound getTagCompound();

    /**
     * @param newStackSize The new stacksize the itemstack should have
     */
    void setStackSize(long newStackSize);

    /**
     * @return The current ItemStackLong as a ItemStack, this stack shouldn't be edited as it won't reflect upon the real one
     */
    @Nonnull ItemStack getAsItemStack();

    boolean isItemEqual(@Nullable IItemStackLong other);

    boolean hasTagCompound();

    boolean isStackable();

    boolean getHasSubtypes();

    @Nonnull IItemStackLong copy();

    @Nonnull IItemStackLong splitStack(long toSplit);

    @Nonnull NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt);
}
