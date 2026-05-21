package com.cleanroommc.modularui.api.layout;

import com.cleanroommc.modularui.api.GuiAxis;

/**
 * An interface that handles resizing of widgets.
 * Usually this interface doesn't have to be implemented or interacted with, by the users of this library.
 */
public interface IResizeable extends IResizeParent {

    /**
     * Called once before resizing
     */
    void initResizing(boolean onOpen);

    /**
     * Resizes the given element
     *
     * @param isParentLayout if the parent is a layout widget
     * @return true if element is fully resized
     */
    //TODO: this seems like another of the per-frame animation situations?
    boolean resize(boolean isParentLayout);

    /**
     * Called if {@link #resize(boolean)} returned {@code false} after children have been resized.
     *
     * @return if element is fully resized
     */
    boolean postResize();

    /**
     * Called after all elements in the tree are resized and the absolute positions needs to be calculated from their
     * relative positions.
     */
    default void preApplyPos() {}

    /**
     * This converts the relative pos to resizer parent to relative pos to widget parent.
     */
    default void applyPos() {}

    void setChildrenResized(boolean resized);

    void setLayoutDone(boolean done);

    default void setAxisResized(GuiAxis axis, boolean pos, boolean size) {
        if (axis.isHorizontal()) {
            setXAxisResized(pos, size);
        } else {
            setYAxisResized(pos, size);
        }
    }

    void setXAxisResized(boolean pos, boolean size);

    void setYAxisResized(boolean pos, boolean size);

    /**
     * Marks position and size as calculated.
     */
    default void setResized(boolean x, boolean y, boolean w, boolean h) {
        setXAxisResized(x, w);
        setYAxisResized(y, h);
    }

    default void setPosResized(boolean x, boolean y) {
        setResized(x, y, isWidthCalculated(), isHeightCalculated());
    }

    default void setSizeResized(boolean w, boolean h) {
        setResized(isXCalculated(), isYCalculated(), w, h);
    }

    default void setXResized(boolean v) {
        setXAxisResized(v, isWidthCalculated());
    }

    default void setYResized(boolean v) {
        setYAxisResized(v, isHeightCalculated());
    }

    default void setPosResized(GuiAxis axis, boolean v) {
        if (axis.isHorizontal()) {
            setXResized(v);
        } else {
            setYResized(v);
        }
    }

    default void setWidthResized(boolean v) {
        setXAxisResized(isXCalculated(), v);
    }

    default void setHeightResized(boolean v) {
        setYAxisResized(isYCalculated(), v);
    }

    default void setSizeResized(GuiAxis axis, boolean v) {
        if (axis.isHorizontal()) {
            setWidthResized(v);
        } else {
            setHeightResized(v);
        }
    }

    default void setResized(boolean b) {
        setResized(b, b, b, b);
    }

    default void updateResized() {
        setResized(isXCalculated(), isYCalculated(), isWidthCalculated(), isHeightCalculated());
    }

    /**
     * Sets whether margin and padding should be applied on the x-axis.
     *
     * @param b true if margin and padding should be applied
     */
    void setXMarginPaddingApplied(boolean b);

    /**
     * Sets whether margin and padding should be applied on the y-axis.
     *
     * @param b true if margin and padding should be applied
     */
    void setYMarginPaddingApplied(boolean b);

    /**
     * Sets whether margin and padding should be applied on the x- and y-axis.
     * @param b true if margin and padding should be applied
     */
    default void setMarginPaddingApplied(boolean b) {
        setXMarginPaddingApplied(b);
        setYMarginPaddingApplied(b);
    }

    /**
     * Sets whether margin and padding should be applied on the given axis.
     * @param axis the axis to apply to
     * @param b true if margin and padding should be applied
     */
    default void setMarginPaddingApplied(GuiAxis axis, boolean b) {
        if (axis.isHorizontal()) {
            setXMarginPaddingApplied(b);
        } else {
            setYMarginPaddingApplied(b);
        }
    }
}
