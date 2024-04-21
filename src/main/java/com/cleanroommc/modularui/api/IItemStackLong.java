package com.cleanroommc.modularui.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.cleanroommc.modularui.utils.item.ItemStackLong;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public interface IItemStackLong {

    public static @Nullable IItemStackLong loadItemStackFromNBT(@Nonnull NBTTagCompound nbt) {
        int type = nbt.getInteger("type");
        return switch(type) {
            case 1 -> ItemStackLong.loadFromNBT(nbt);
            default -> null;
        };
    }

    public static boolean areItemStacksEqual(@Nullable IItemStackLong a, @Nullable IItemStackLong b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        if (a.getItem() != b.getItem()) return false;
        if (a.getItemDamage() != b.getItemDamage()) return false;
        if (a.getStackSize() != b.getStackSize()) return false;
        if (a.getTagCompound() == null && b.getTagCompound() == null) return true;
        if (a.getTagCompound() == null || b.getTagCompound() == null) return false;
        return a.getTagCompound().equals(b.getTagCompound());
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
