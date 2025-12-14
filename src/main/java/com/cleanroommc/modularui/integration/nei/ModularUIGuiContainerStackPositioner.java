package com.cleanroommc.modularui.integration.nei;

import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.screen.GuiContainerWrapper;
import com.cleanroommc.modularui.screen.ModularContainer;

import codechicken.nei.PositionedStack;
import codechicken.nei.api.IStackPositioner;

import net.minecraft.client.gui.inventory.GuiContainer;

import java.util.ArrayList;

public class ModularUIGuiContainerStackPositioner<T extends GuiContainer & IMuiScreen> implements IStackPositioner {
    private final T wrapper;
    private final ModularContainer container;
    private final INEIRecipeTransfer<?> recipeTransfer;

    public ModularUIGuiContainerStackPositioner(T wrapper, ModularContainer container, INEIRecipeTransfer<?> tr) {
        this.wrapper = wrapper;
        this.container = container;
        this.recipeTransfer = tr;
    }
    @Override
    public ArrayList<PositionedStack> positionStacks(ArrayList<PositionedStack> stacks) {
        if (wrapper instanceof GuiContainerWrapper gcw) {
            return recipeTransfer.positionStacks(gcw, container, stacks);
        }
        return stacks;
    }
}
