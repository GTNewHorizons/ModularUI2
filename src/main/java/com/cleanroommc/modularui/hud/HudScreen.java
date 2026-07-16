package com.cleanroommc.modularui.hud;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Internal subclass of {@link ModularScreen} used by {@link HudElement}.
 * <p>
 * Constructs the screen with a read-only {@link HudContext} instead of the default
 * {@link ModularGuiContext}, then builds the main panel via the supplied creator function
 * using that context.
 */
@ApiStatus.Internal
@SideOnly(Side.CLIENT)
class HudScreen extends ModularScreen {

    HudScreen(@NotNull String owner, @NotNull Function<ModularGuiContext, ModularPanel> panelCreator) {
        super(owner, panelCreator, HudContext::new);
    }
}
