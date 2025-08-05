package com.cleanroommc.modularui.integration.nei;

import codechicken.nei.PositionedStack;
import codechicken.nei.api.IStackPositioner;
import com.cleanroommc.modularui.screen.GuiContainerWrapper;
import com.cleanroommc.modularui.screen.ModularContainer;
import java.util.ArrayList;

public class GuiContainerWrapperStackPositioner implements IStackPositioner {

    //Hacky way around not having much params passed here
    public GuiContainerWrapper wrapper;
    public ModularContainer container;
    public INEIRecipeTransfer<?> recipeTransfer;

    @Override
    public ArrayList<PositionedStack> positionStacks(ArrayList<PositionedStack> ai) {
        return recipeTransfer.positionStacks(wrapper,container,ai);
    }
}
