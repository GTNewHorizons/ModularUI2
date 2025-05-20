package com.cleanroommc.modularui.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

/**
 * Version specific code is supposed to go here.
 * Ideally only the body of methods and value of fields should be changed and no signatures.
 */
public class Platform {

    public static final ItemStack EMPTY_STACK = null;

    @SideOnly(Side.CLIENT)
    public static @NotNull EntityPlayerSP getClientPlayer() {
        return Minecraft.getMinecraft().thePlayer;
    }

    public static boolean isStackEmpty(ItemStack stack) {
        return stack == null || stack.getItem() == null || stack.stackSize <= 0;
    }

    public static ItemStack copyStack(ItemStack stack) {
        return isStackEmpty(stack) ? EMPTY_STACK : stack.copy();
    }
}
