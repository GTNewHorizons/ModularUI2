package com.cleanroommc.modularui.integration.nei;

import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.widget.sizer.Area;

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
        if (!screen.getContext().getRecipeViewerSettings().isRecipeViewerEnabled(screen)) {
            return false;
        }
        Area.SHARED.set(x, y, w, h);
        for (Rectangle exclusionArea : screen.getContext().getRecipeViewerSettings().getAllRecipeViewerExclusionAreas()) {
            if (exclusionArea.intersects(Area.SHARED)) {
                return true;
            }
        }
        return false;
    }
}
