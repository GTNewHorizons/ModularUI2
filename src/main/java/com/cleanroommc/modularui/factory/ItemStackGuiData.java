package com.cleanroommc.modularui.factory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * See {@link GuiData} for an explanation for what this is for.
 */
public class ItemStackGuiData extends GuiData {

    private final ItemStack itemStack;

    public ItemStackGuiData(EntityPlayer player, ItemStack itemStack) {
        super(player);
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public NBTTagCompound getTagCompound() {
        return itemStack.getTagCompound();
    }
}
