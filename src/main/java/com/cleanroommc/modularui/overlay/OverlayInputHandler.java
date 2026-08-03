package com.cleanroommc.modularui.overlay;

import com.cleanroommc.modularui.ModularUI;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.eigenraven.lwjgl3ify.api.InputEvents;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@SideOnly(Side.CLIENT)
@Optional.Interface(modid = ModularUI.ModIds.LWJGL3IFY, iface = "me.eigenraven.lwjgl3ify.api.InputEvents$KeyboardListener")
public class OverlayInputHandler implements InputEvents.KeyboardListener {

    @Optional.Method(modid = ModularUI.ModIds.LWJGL3IFY)
    public static void register() {
        InputEvents.addKeyboardListener(new OverlayInputHandler());
    }

    @Override
    @Optional.Method(modid = ModularUI.ModIds.LWJGL3IFY)
    public void onTextEvent(InputEvents.TextEvent event) {
        OverlayStack.interact(screen -> {
            if (screen.getContext().getFocusedWidget() == null) {
                return false;
            }
            screen.onTextEvent(event);
            return true;
        }, true);
    }
}
