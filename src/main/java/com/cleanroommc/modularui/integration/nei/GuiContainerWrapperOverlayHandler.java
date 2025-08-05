package com.cleanroommc.modularui.integration.nei;

import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.recipe.GuiOverlayButton;
import codechicken.nei.recipe.IRecipeHandler;
import com.cleanroommc.modularui.screen.GuiContainerWrapper;
import com.cleanroommc.modularui.screen.ModularContainer;
import net.minecraft.client.gui.inventory.GuiContainer;
import java.util.List;

public class GuiContainerWrapperOverlayHandler implements IOverlayHandler {

    @Override
    public void overlayRecipe(GuiContainer gui, IRecipeHandler recipe, int recipeIndex, boolean maxTransfer) {
        if (gui instanceof GuiContainerWrapper muw && gui.inventorySlots instanceof ModularContainer muc && muc instanceof INEIRecipeTransfer<?> tr) {
            tr.overlayRecipe(muw,muc,recipe,recipeIndex,maxTransfer);
        }
    }

    @Override
    public int transferRecipe(GuiContainer gui, IRecipeHandler recipe, int recipeIndex, int multiplier) {
        if (gui instanceof GuiContainerWrapper muw && gui.inventorySlots instanceof ModularContainer muc && muc instanceof INEIRecipeTransfer<?> tr) {
            return tr.transferRecipe(muw, muc, recipe, recipeIndex, multiplier);
        }
        return IOverlayHandler.super.transferRecipe(gui,recipe,recipeIndex,multiplier);
    }

    @Override
    public boolean canFillCraftingGrid(GuiContainer gui, IRecipeHandler recipe, int recipeIndex) {
        if (gui instanceof GuiContainerWrapper muw && gui.inventorySlots instanceof ModularContainer muc && muc instanceof INEIRecipeTransfer<?> tr) {
            return tr.canFillCraftingGrid(muw, muc, recipe, recipeIndex);
        }
        return IOverlayHandler.super.canFillCraftingGrid(gui, recipe, recipeIndex);
    }

    @Override
    public boolean craft(GuiContainer gui, IRecipeHandler recipe, int recipeIndex, int multiplier) {
        if (gui instanceof GuiContainerWrapper muw && gui.inventorySlots instanceof ModularContainer muc && muc instanceof INEIRecipeTransfer<?> tr) {
            return tr.craft(muw, muc, recipe, recipeIndex, multiplier);
        }
        return IOverlayHandler.super.craft(gui, recipe, recipeIndex, multiplier);
    }

    @Override
    public boolean canCraft(GuiContainer gui, IRecipeHandler recipe, int recipeIndex) {
        if (gui instanceof GuiContainerWrapper muw && gui.inventorySlots instanceof ModularContainer muc && muc instanceof INEIRecipeTransfer<?> tr) {
            return tr.canCraft(muw, muc, recipe, recipeIndex);
        }
        return IOverlayHandler.super.canCraft(gui, recipe, recipeIndex);
    }

    @Override
    public List<GuiOverlayButton.ItemOverlayState> presenceOverlay(GuiContainer gui, IRecipeHandler recipe, int recipeIndex) {
        if (gui instanceof GuiContainerWrapper muw &&   gui.inventorySlots instanceof ModularContainer muc && muc instanceof INEIRecipeTransfer<?> tr) {
            return tr.presenceOverlay(muw, muc, recipe, recipeIndex);
        }
        return IOverlayHandler.super.presenceOverlay(gui, recipe, recipeIndex);
    }
}
