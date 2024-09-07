package com.cleanroommc.modularui.integration.nei;

import codechicken.nei.NEIClientUtils;
import codechicken.nei.api.INEIGuiAdapter;

import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.api.widget.IGuiElement;

import com.cleanroommc.modularui.screen.ModularScreen;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import java.awt.*;

public class ModularUINEIGuiHandler extends INEIGuiAdapter {

    @Override
    public boolean handleDragNDrop(GuiContainer gui, int mousex, int mousey, ItemStack draggedStack, int button) {
        if (!(gui instanceof IMuiScreen muiScreen) || NEIClientUtils.getHeldItem() != null) {
            return false;
        }
        IGuiElement hovered = muiScreen.getScreen().getContext().getHovered();
        if (hovered instanceof NEIDragAndDropHandler) {
            return ((NEIDragAndDropHandler) hovered).handleDragAndDrop(draggedStack, button);
        }
        return false;
    }

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
