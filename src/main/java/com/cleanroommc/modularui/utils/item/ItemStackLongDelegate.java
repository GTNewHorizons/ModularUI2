package com.cleanroommc.modularui.utils.item;

import static com.google.common.primitives.Ints.saturatedCast;

import com.cleanroommc.modularui.api.IItemStackLong;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemStackLongDelegate implements IItemStackLong {

    private ItemStack item;

    public ItemStackLongDelegate(ItemStack item) {
        this.item = item;
    }

    @Override
    public long getStackSize() {
        return item == null ? 0 :item.stackSize;
    }

    @Override
    public long getMaxStackSize() {
        return item == null ? 0 : item.getMaxStackSize();
    }

    @Override
    public Item getItem() {
        return item == null ? null : item.getItem();
    }

    @Override
    public int getItemDamage() {
        return item == null ? 0 : item.getItemDamage();
    }

    @Override
    public NBTTagCompound getTagCompound() {
        return item == null ? null : item.getTagCompound();
    }

    @Override
    public void setStackSize(long newStackSize) {
        if (item == null) return;
        item.stackSize = saturatedCast(newStackSize);
    }

    @Override
    public ItemStack getAsItemStack() {
        return item;
    }

    @Override
    public boolean isItemEqual(IItemStackLong other) {
        if (item == null) return other == null || other.getAsItemStack() == null;
        if (item.getItem() != other.getItem()) return false;
        return true;
    }

    @Override
    public boolean hasTagCompound() {
        return item != null && item.hasTagCompound();
    }

    @Override
    public boolean isStackable() {
        return item.isStackable();
    }

    @Override
    public boolean getHasSubtypes() {
        return item.getHasSubtypes();
    }

    @Override
    public ItemStackLongDelegate copy() {
        return item == null ? null : new ItemStackLongDelegate(item.copy());
    }

    @Override
    public IItemStackLong splitStack(long toSplit) {
        return new ItemStackLongDelegate(item.splitStack(saturatedCast(toSplit)));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("type", 2);
        if (item != null) {
            item.writeToNBT(nbt);
        }
        return nbt;
    }

    public static IItemStackLong loadFromNBT(NBTTagCompound nbt) {
        return new ItemStackLongDelegate(ItemStack.loadItemStackFromNBT(nbt));
    }
}