package com.cleanroommc.modularui.api.widget;

import com.cleanroommc.modularui.ModularUI;

import cpw.mods.fml.common.Optional;

import me.eigenraven.lwjgl3ify.api.InputEvents;

public interface ModernInteractable {

    @Optional.Method(modid = ModularUI.ModIds.LWJGL3IFY)
    default Interactable.Result onKeyEvent(InputEvents.KeyEvent event) {
        return Interactable.Result.IGNORE;
    }

    @Optional.Method(modid = ModularUI.ModIds.LWJGL3IFY)
    default boolean onTextInput(InputEvents.TextEvent event) {
        return false;
    }
}
