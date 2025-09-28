package com.cleanroommc.modularui.integration.nei;

import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.screen.ModularScreen;

import net.minecraft.client.gui.inventory.GuiContainer;

import codechicken.nei.api.INEIGuiAdapter;

import java.awt.*;

public class ModularUINEIGuiHandler extends INEIGuiAdapter {

    @Override
    public boolean hideItemPanelSlot(GuiContainer gui, int x, int y, int w, int h) {
        if (!(gui instanceof IMuiScreen muiScreen)) {
            return false;
        }
        ModularScreen screen = muiScreen.getScreen();
        if (!screen.getContext().getNEISettings().isNEIEnabled(screen)) {
            return false;
        }
        return screen.getContext().getNEISettings().getAllNEIExclusionAreas().stream().anyMatch(
                a -> a.intersects(new Rectangle(x, y, w, h))
        );
    }
}
