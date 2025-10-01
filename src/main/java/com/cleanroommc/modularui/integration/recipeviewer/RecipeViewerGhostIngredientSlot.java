package com.cleanroommc.modularui.integration.recipeviewer;

import com.cleanroommc.modularui.api.widget.IWidget;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

/**
 * An interface for compat with recipe viewer ghost slots.
 * Implement this on any {@link IWidget}.
 *
 * @param <I> type of the ingredient (kept for parity with 1.12)
 */
public interface RecipeViewerGhostIngredientSlot<I> {

    /**
     * Implement your drag-and-drop behavior here. The held stack will be deleted if draggedStack.stackSize == 0.
     *
     * @param draggedStack Item dragged from NEI
     * @param button       0 = left click, 1 = right click
     * @return True if success
     */
    boolean handleDragAndDrop(@NotNull ItemStack draggedStack, int button);
}
