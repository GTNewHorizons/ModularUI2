package com.cleanroommc.modularui.integration.nei;

import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.screen.ModularContainer;

import net.minecraft.client.gui.inventory.GuiContainer;

import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.recipe.GuiOverlayButton;
import codechicken.nei.recipe.IRecipeHandler;

import java.util.List;

public class ModularUIGuiContainerStackOverlay implements IOverlayHandler {

    private interface Action<T, G extends GuiContainer & IMuiScreen> {

        T doAction(G gui, INEIRecipeTransfer<G> tr);
    }

    @SuppressWarnings("unchecked")
    private static <T, G extends GuiContainer & IMuiScreen> T doAction(GuiContainer gui, Action<T, G> action) {
        if (gui instanceof IMuiScreen && gui.inventorySlots instanceof ModularContainer mc && mc instanceof INEIRecipeTransfer<?> tr) {
            return action.doAction((G) gui, (INEIRecipeTransfer<G>) tr);
        }
        return null;
    }

    @Override
    public void overlayRecipe(GuiContainer gui, IRecipeHandler recipe, int recipeIndex, boolean maxTransfer) {
        doAction(gui, (mui, tr) -> {
            tr.overlayRecipe(mui, recipe, recipeIndex, maxTransfer);
            return null;
        });
    }

    @Override
    public int transferRecipe(GuiContainer gui, IRecipeHandler recipe, int recipeIndex, int multiplier) {
        var res = doAction(gui, (mui, tr) -> tr.transferRecipe(mui, recipe, recipeIndex, multiplier));
        return res != null ? res : IOverlayHandler.super.transferRecipe(gui, recipe, recipeIndex, multiplier);
    }

    @Override
    public boolean canFillCraftingGrid(GuiContainer gui, IRecipeHandler recipe, int recipeIndex) {
        var res = doAction(gui, (mui, tr) -> tr.canFillCraftingGrid(mui, recipe, recipeIndex));
        return res != null ? res : IOverlayHandler.super.canFillCraftingGrid(gui, recipe, recipeIndex);
    }

    @Override
    public boolean craft(GuiContainer gui, IRecipeHandler recipe, int recipeIndex, int multiplier) {
        var res = doAction(gui, (mui, tr) -> tr.craft(mui, recipe, recipeIndex, multiplier));
        return res != null ? res : IOverlayHandler.super.craft(gui, recipe, recipeIndex, multiplier);
    }

    @Override
    public boolean canCraft(GuiContainer gui, IRecipeHandler recipe, int recipeIndex) {
        var res = doAction(gui, (mui, tr) -> tr.canCraft(mui, recipe, recipeIndex));
        return res != null ? res : IOverlayHandler.super.canCraft(gui, recipe, recipeIndex);
    }

    @Override
    public List<GuiOverlayButton.ItemOverlayState> presenceOverlay(GuiContainer gui, IRecipeHandler recipe, int recipeIndex) {
        var res = doAction(gui, (mui, tr) -> tr.presenceOverlay(mui, recipe, recipeIndex));
        return res != null ? res : IOverlayHandler.super.presenceOverlay(gui, recipe, recipeIndex);
    }
}
