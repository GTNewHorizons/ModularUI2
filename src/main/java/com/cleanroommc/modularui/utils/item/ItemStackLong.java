package com.cleanroommc.modularui.utils.item;

import static com.google.common.primitives.Ints.saturatedCast;

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

    public ItemStackLong(Item item) {
        this(item, 64, 0, 0, null);
    }

    public ItemStackLong(Item item, long maxStackSize) {
        this(item, maxStackSize, 0, 0, null);
    }

    public ItemStackLong(Item item, long maxStackSize, int damage) {
        this(item, maxStackSize, damage, 0, null);
    }

    public ItemStackLong(Item item, long maxStackSize, int damage, long stackSize) {
        this(item, maxStackSize, damage, stackSize, null);
    }

    public ItemStackLong(Item item, long maxStackSize, int damage, long stackSize, NBTTagCompound nbt) {
        this.item = item;
        this.maxStackSize = maxStackSize;
        this.stackSize = stackSize;
        this.damage = damage;
        this.nbt = nbt;
    }

    @Override
    public long getStackSize() {
        return stackSize;
    }

    @Override
    public long getMaxStackSize() {
        return maxStackSize;
    }

    @Override
    public Item getItem() {
        return item;
    }

    @Override
    public int getItemDamage() {
        return damage;
    }

    @Override
    public NBTTagCompound getTagCompound() {
        return nbt;
    }

    @Override
    public void setStackSize(long newStackSize) {
        stackSize = newStackSize;
    }

    @Override
    public ItemStack getAsItemStack() {
        return new ItemStack(item, saturatedCast(stackSize), damage);
    }

    @Override
    public boolean isItemEqual(IItemStackLong other) {
        if (getItem() != other.getItem()) return false;
        return true;
    }

    @Override
    public boolean hasTagCompound() {
        return getTagCompound() != null;
    }

    @Override
    public boolean isStackable() {
        return item.getItemStackLimit(getAsItemStack()) > 1;
    }

    @Override
    public boolean getHasSubtypes() {
        return item.getHasSubtypes();
    }

    @Override
    public IItemStackLong copy() {
        return new ItemStackLong(item, maxStackSize, damage, stackSize, nbt);
    }

    @Override
    public IItemStackLong splitStack(long toSplit) {
        ItemStackLong split = new ItemStackLong(item, maxStackSize, damage, toSplit, nbt);
        this.setStackSize(getStackSize() - toSplit);
        return split;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("type", 1);
        ItemStack itemStack = getAsItemStack();
        itemStack.setTagCompound(getTagCompound());
        itemStack.writeToNBT(nbt);
        nbt.setLong("stackSizeLong", getStackSize());
        nbt.setLong("maxStackSizeLong", getMaxStackSize());
        return nbt;
    }

    public static ItemStackLong loadFromNBT(NBTTagCompound nbt) {
        ItemStack itemStack = ItemStack.loadItemStackFromNBT(nbt);
        ItemStackLong item = new ItemStackLong(itemStack.getItem(), nbt.getLong("maxStackSizeLong"), itemStack.getItemDamage(), nbt.getLong("maxStackSizeLong"), itemStack.getTagCompound());
        return item;
    }
}