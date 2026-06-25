package com.cleanroommc.modularui.api.layout;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.widget.INotifyEnabled;
import com.cleanroommc.modularui.api.widget.IWidget;

/**
 * This is responsible for laying out widgets.
 */
public interface ILayoutWidget extends INotifyEnabled {

    /**
     * Called after the children tried to calculate their size. This method responsible for laying out its children
     * within itself. This includes calling {@link IResizeable#setSizeResized(boolean, boolean)} or one of its variants after a size with
     * {@link com.cleanroommc.modularui.widget.sizer.Area#setSize(GuiAxis, int)} or one of its variants on each child.
     * The same applies to position modifications.<br>
     * <b>If this widget also applies margin and padding</b> (this is usually the case), <b>then {@link IResizeable#setMarginPaddingApplied(boolean)}
     * or one of its variants needs to be called too.</b>
     * <p>
     * Note that even if {@link #shouldIgnoreChildSize(IWidget)} returns {@code false} at least one of the {@code setResized} methods in
     * {@link IResizeable} must be called. There is a no arg variant {@link IResizeable#updateResized()}, which can also be used.
     * Not doing so may result in failure to resize the widget tree fully.
     *
     * @return {@code true} if the layout was successful and no further iteration is needed
     */
    boolean layoutWidgets();

    /**
     * Called after post calculation of this widget. The last call guarantees, that this widget is fully calculated.
     *
     * @return {@code true} if the layout was successful and no further iteration is needed
     */
    default boolean postLayoutWidgets() {
        return true;
    }

    default boolean canCoverByDefaultSize(GuiAxis axis) {
        return false;
    }

    /**
     * Called when determining the wrapping size of this widget.
     * If this method returns {@code true}, size and margin of the queried child will be ignored for calculation.<br>
     * Typically return {@code true} when the children are disabled and you want to collapse them for layout-ing.<br>
     * This method should also be used for layout-ing children with {@link #layoutWidgets} if it might return {@code true}.
     */
    default boolean shouldIgnoreChildSize(IWidget child) {
        return false;
    }

    @Override
    default void onChildChangeEnabled(IWidget child, boolean enabled) {
        layoutWidgets();
        postLayoutWidgets();
    }
}
