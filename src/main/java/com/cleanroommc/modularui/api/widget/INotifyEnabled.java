package com.cleanroommc.modularui.api.widget;

/**
 * Implemented by parent widgets that want to be notified when a child widget is enabled or disabled.
 */
public interface INotifyEnabled {

    /**
     * Called when a child widget changes its enabled state.
     *
     * @param child   child widget whose state changed
     * @param enabled new enabled state of the child
     */
    void onChildChangeEnabled(IWidget child, boolean enabled);

}
