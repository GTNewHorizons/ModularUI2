package com.cleanroommc.modularui.api.widget;

import com.cleanroommc.modularui.ModularUI;

import cpw.mods.fml.common.Optional;

import me.eigenraven.lwjgl3ify.api.InputEvents;

/**
 * Optional interactions using lwjgl3ify input events. Only active when lwjgl3ify is loaded.
 */
public interface ModernInteractable {

    /**
     * Called on a key event when lwjgl3ify is present.
     *
     * @param event key event
     * @return result that determines what happens to other widgets
     */
    @Optional.Method(modid = ModularUI.ModIds.LWJGL3IFY)
    default Interactable.Result onKeyEvent(InputEvents.KeyEvent event) {
        return Interactable.Result.IGNORE;
    }

    /**
     * Called on a text input event when lwjgl3ify is present.
     *
     * @param event text event
     * @return true if the text input was consumed
     */
    @Optional.Method(modid = ModularUI.ModIds.LWJGL3IFY)
    default boolean onTextInput(InputEvents.TextEvent event) {
        return false;
    }
}
