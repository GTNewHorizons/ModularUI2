package com.cleanroommc.modularui.hud;

import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.jetbrains.annotations.ApiStatus;

/**
 * A read-only context for HUD elements: disables the hover/focus/drag subsystem, since HUD
 * elements are display-only.
 */
@ApiStatus.Internal
@SideOnly(Side.CLIENT)
public class HudContext extends ModularGuiContext {

    public HudContext(ModularScreen screen) {
        super(screen);
    }

    @Override
    public void onFrameUpdate() {
        // no-op: display-only, no hover/focus/drag
    }
}
