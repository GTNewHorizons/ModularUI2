package com.cleanroommc.modularui.integration.nei;

import codechicken.nei.ItemPanels;

import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;

import net.minecraft.item.ItemStack;

public class NEIUtil {

    public static ItemStack getNEIDragAndDropTarget(ModularGuiContext context) {
        if (context.getScreen().isOverlay() || !context.getRecipeViewerSettings().isRecipeViewerEnabled(context.getScreen())) {
            return null;
        }
        if (ItemPanels.itemPanel.draggedStack != null) {
            return ItemPanels.itemPanel.draggedStack;
        }
        if (ItemPanels.bookmarkPanel.draggedStack != null) {
            return ItemPanels.bookmarkPanel.draggedStack;
        }
        return null;
    }

    public static void stopNEIGhostDrag() {
        // Replicate behavior of PanelWidget#handleDraggedClick
        if (ItemPanels.itemPanel.draggedStack != null && ItemPanels.itemPanel.draggedStack.stackSize == 0) {
            ItemPanels.itemPanel.draggedStack = null;
        }
        if (ItemPanels.bookmarkPanel.draggedStack != null && ItemPanels.bookmarkPanel.draggedStack.stackSize == 0) {
            ItemPanels.bookmarkPanel.draggedStack = null;
        }
    }
}
