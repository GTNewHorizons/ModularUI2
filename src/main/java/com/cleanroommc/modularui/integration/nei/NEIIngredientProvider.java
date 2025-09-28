package com.cleanroommc.modularui.integration.nei;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

/**
 * An interface for NEI to get the itemstack from a widget to show recipes for example.
 * Implement this on {@link com.cleanroommc.modularui.api.widget.IWidget}.
 */
public interface NEIIngredientProvider {

    @Nullable
    ItemStack getStackForNEI();
}
