package com.cleanroommc.modularui.integration.recipeviewer;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

/**
 * An interface for recipe viewer to get the ingredient from a widget to show recipes for example.
 * Implement this on {@link com.cleanroommc.modularui.api.widget.IWidget}.
 */
public interface RecipeViewerIngredientProvider {

    @Nullable
    ItemStack getStackForRecipeViewer();
}
