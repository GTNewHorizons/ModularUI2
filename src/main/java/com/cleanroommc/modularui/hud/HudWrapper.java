package com.cleanroommc.modularui.hud;

import com.cleanroommc.modularui.screen.ModularScreen;

import net.minecraft.client.gui.GuiScreen;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.jetbrains.annotations.ApiStatus;

/**
 * A phantom {@link GuiScreen}, never set as {@code Minecraft.currentScreen} - it only exists to
 * satisfy {@link ModularScreen#constructOverlay(GuiScreen)}, reusing the normal render pipeline
 * for HUD elements. Input methods are inherited as no-ops, which is correct since HUD elements
 * are display-only.
 */
@ApiStatus.Internal
@SideOnly(Side.CLIENT)
class HudWrapper extends GuiScreen {

    private final ModularScreen screen;

    HudWrapper(ModularScreen screen) {
        this.screen = screen;
        this.screen.constructOverlay(this);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public String toString() {
        return "HudWrapper(" + this.screen + ")";
    }
}
