package com.cleanroommc.modularui.api.widget;

/**
 * The edge or corner of a widget that is being dragged to resize it.
 */
public enum ResizeDragArea {

    TOP_LEFT(true, true, false, false),
    TOP_RIGHT(true, false, false, true),
    BOTTOM_LEFT(false, true, true, false),
    BOTTOM_RIGHT(false, false, true, true),
    TOP(true, false, false, false),
    LEFT(false, true, false, false),
    BOTTOM(false, false, true, false),
    RIGHT(false, false, false, true);

    public final boolean top, left, bottom, right;

    /**
     * @param top    if the top edge is affected
     * @param left   if the left edge is affected
     * @param bottom if the bottom edge is affected
     * @param right  if the right edge is affected
     * @throws IllegalArgumentException if both top and bottom or both left and right are set
     */
    ResizeDragArea(boolean top, boolean left, boolean bottom, boolean right) {
        if (top && bottom || left && right) throw new IllegalArgumentException();
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }
}
