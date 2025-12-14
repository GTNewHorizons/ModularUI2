package com.cleanroommc.modularui.integration.nei;

import com.cleanroommc.modularui.screen.GuiContainerWrapper;
import com.cleanroommc.modularui.screen.ModularContainer;

import codechicken.nei.PositionedStack;
import codechicken.nei.api.IStackPositioner;

import java.util.ArrayList;

public class ModularUIGuiContainerStackPositioner implements IStackPositioner {

    public GuiContainerWrapper wrapper;
    public ModularContainer container;
    public INEIRecipeTransfer<?> recipeTransfer;

    @Override
    public ArrayList<PositionedStack> positionStacks(ArrayList<PositionedStack> ai) {
        return recipeTransfer.positionStacks(wrapper, container, ai);
    }
}
