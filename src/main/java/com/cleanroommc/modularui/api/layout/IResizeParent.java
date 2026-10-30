package com.cleanroommc.modularui.api.layout;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.widget.sizer.Area;

public interface IResizeParent {

    /**
     * @return the mutable layout area of the element
     */
    Area getArea();

    /**
     * @return {@code true} if the relative x position has been calculated
     */
    boolean isXCalculated();

    /**
     * @return {@code true} if the relative y position has been calculated
     */
    boolean isYCalculated();

    /**
     * @return {@code true} if the width has been calculated
     */
    boolean isWidthCalculated();

    /**
     * @return {@code true} if the height has been calculated
     */
    boolean isHeightCalculated();

    /**
     * @return {@code true} if all child elements have been calculated
     */
    boolean areChildrenCalculated();

    boolean isLayoutDone();

    /**
     * Returns whether this element may need another layout pass.
     *
     * @param isParentLayout {@code true} if this is the parent layer
     * @return {@code true} if this element can be laid out again
     */
    boolean canRelayout(boolean isParentLayout);

    default boolean isSizeCalculated(GuiAxis axis) {
        return axis.isHorizontal() ? isWidthCalculated() : isHeightCalculated();
    }

    default boolean isPosCalculated(GuiAxis axis) {
        return axis.isHorizontal() ? isXCalculated() : isYCalculated();
    }

    /**
     * @return {@code true} if the relative position and size are fully calculated and doesn't need further layout recalculation iterations.
     */
    default boolean isSelfFullyCalculated(boolean isParentLayout) {
        return isSelfFullyCalculated() && !canRelayout(isParentLayout);
    }

    default boolean isSelfFullyCalculated() {
        return isXCalculated() && isYCalculated() && isWidthCalculated() && isHeightCalculated();
    }

    default boolean isFullyCalculated() {
        return isSelfFullyCalculated() && areChildrenCalculated() && isLayoutDone();
    }

    default boolean isFullyCalculated(boolean isParentLayout) {
        return isFullyCalculated() && !canRelayout(isParentLayout);
    }

    /**
     * @return {@code true} if margin and padding are applied on the x-axis
     */
    boolean isXMarginPaddingApplied();

    /**
     * @return {@code true} if margin and padding are applied on the y-axis
     */
    boolean isYMarginPaddingApplied();
}
