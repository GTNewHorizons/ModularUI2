package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.utils.item.ItemStackLong;
import com.cleanroommc.modularui.utils.item.ItemStackLongDelegate;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public interface IItemStackLong {

    public static IItemStackLong loadItemStackFromNBT(NBTTagCompound nbt) {
        int type = nbt.getInteger("type");
        return switch(type) {
            case 1 -> ItemStackLong.loadFromNBT(nbt);
            case 2 -> ItemStackLongDelegate.loadFromNBT(nbt);
            default -> null;
        };
    }

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
    NBTTagCompound getTagCompound();

    /**
     * @param newStackSize The new stacksize the itemstack should have
     */
    void setStackSize(long newStackSize);

    /**
     * @return The current ItemStackLong as a ItemStack, this stack shouldn't be edited as it won't reflect upon the real one
     */
    ItemStack getAsItemStack();

    boolean isItemEqual(IItemStackLong other);

    boolean hasTagCompound();

    boolean isStackable();

    boolean getHasSubtypes();

    IItemStackLong copy();

    IItemStackLong splitStack(long toSplit);

    NBTTagCompound writeToNBT(NBTTagCompound nbt);
}