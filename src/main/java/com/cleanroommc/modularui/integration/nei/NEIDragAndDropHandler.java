package com.cleanroommc.modularui.integration.nei;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * An interface for compat with NEI's drag-and-drop feature.
 * Implement this on any {@link com.cleanroommc.modularui.api.widget.IWidget}.
 */
public interface NEIDragAndDropHandler {

    /**
     * Implement your drag-and-drop behavior here. The held stack will be deleted if draggedStack.stackSize == 0.
     *
     * @param draggedStack Item dragged from NEI
     * @param button       0 = left click, 1 = right click
     * @return True if success
     */
    boolean handleDragAndDrop(@NotNull ItemStack draggedStack, int button);
}
