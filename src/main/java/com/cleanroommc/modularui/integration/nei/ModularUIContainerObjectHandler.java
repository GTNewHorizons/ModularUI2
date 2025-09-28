package com.cleanroommc.modularui.integration.nei;

import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.api.widget.IGuiElement;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import codechicken.nei.guihook.IContainerObjectHandler;

public class ModularUIContainerObjectHandler implements IContainerObjectHandler {

    @Override
    public void guiTick(GuiContainer gui) {}

    @Override
    public void refresh(GuiContainer gui) {}

    @Override
    public void load(GuiContainer gui) {}

    @Override
    public ItemStack getStackUnderMouse(GuiContainer gui, int mousex, int mousey) {
        if (gui instanceof IMuiScreen muiScreen) {
            IGuiElement hovered = muiScreen.getScreen().getContext().getHovered();
            if (hovered instanceof NEIIngredientProvider) {
                return ((NEIIngredientProvider) hovered).getStackForNEI();
            }
        }
        return null;
    }

    @Override
    public boolean objectUnderMouse(GuiContainer gui, int mousex, int mousey) {
        return false;
    }

    @Override
    public boolean shouldShowTooltip(GuiContainer gui) {
        return true;
    }
}
