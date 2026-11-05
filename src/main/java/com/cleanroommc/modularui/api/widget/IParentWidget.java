package com.cleanroommc.modularui.api.widget;

import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

/**
 * Helper interface with child builder methods for parent widgets.
 *
 * @param <I> child widget type
 * @param <W> parent widget type
 */
public interface IParentWidget<I extends IWidget, W extends IParentWidget<I, W>> {

    /**
     * @return this cast to the true widget type
     */
    W getThis();

    /**
     * Adds a child widget at the given index.
     *
     * @param child child widget to add
     * @param index index to insert at, -1 to append
     * @return true if the child was added
     */
    boolean addChild(I child, int index);

    /**
     * Adds a child widget at the given index.
     *
     * @param index index to insert at
     * @param child child widget to add
     * @return this
     * @throws IllegalStateException if the child could not be added
     */
    default W child(int index, I child) {
        if (!addChild(child, index)) {
            throw new IllegalStateException("Failed to add child");
        }
        return getThis();
    }

    /**
     * Appends a child widget.
     *
     * @param child child widget to add
     * @return this
     * @throws IllegalStateException if the child could not be added
     */
    default W child(I child) {
        if (!addChild(child, -1)) {
            throw new IllegalStateException("Failed to add child");
        }
        return getThis();
    }

    /**
     * @deprecated use {@link #childIf(boolean, Supplier)}
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    default W childIf(boolean condition, I child) {
        if (condition) return child(child);
        return getThis();
    }

    default W childIf(boolean condition, Supplier<I> child) {
        if (condition) return child(child.get());
        return getThis();
    }
}
