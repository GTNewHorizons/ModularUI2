package com.cleanroommc.modularui.utils;

import net.minecraft.item.ItemStack;

public class Platform {

    public static final ItemStack EMPTY_STACK = null;

    public static boolean isStackEmpty(ItemStack stack) {
        return stack == null || stack.getItem() == null || stack.stackSize <= 0;
    }

    public static ItemStack copyStack(ItemStack stack) {
        return isStackEmpty(stack) ? EMPTY_STACK : stack.copy();
    }
}
