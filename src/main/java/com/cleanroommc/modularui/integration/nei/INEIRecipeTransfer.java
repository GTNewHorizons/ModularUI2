package com.cleanroommc.modularui.integration.nei;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiOverlayButton;
import codechicken.nei.recipe.IRecipeHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface INEIRecipeTransfer<G extends GuiContainer> {

    String[] getIdents();

    default void overlayRecipe(G gui, IRecipeHandler recipe, int recipeIndex, boolean maxTransfer) {
        transferRecipe(gui, recipe, recipeIndex, maxTransfer ? Integer.MAX_VALUE : 1);
    }

    int transferRecipe(G gui, IRecipeHandler recipe, int recipeIndex, int multiplier);

    default boolean canFillCraftingGrid(G gui, IRecipeHandler recipe, int recipeIndex) {
        return true;
    }

    default boolean craft(G gui, IRecipeHandler recipe, int recipeIndex, int multiplier) {
        return false;
    }

    default boolean canCraft(G gui, IRecipeHandler recipe, int recipeIndex) {
        return false;
    }

    default List<GuiOverlayButton.ItemOverlayState> presenceOverlay(G gui, IRecipeHandler recipe, int recipeIndex) {
        final List<GuiOverlayButton.ItemOverlayState> itemPresenceSlots = new ArrayList<>();
        final List<PositionedStack> ingredients = recipe.getIngredientStacks(recipeIndex);
        final List<ItemStack> invStacks = gui.inventorySlots.inventorySlots.stream()
                .filter(
                        s -> s != null && s.getStack() != null
                                && s.getStack().stackSize > 0
                                && s.isItemValid(s.getStack())
                                && s.canTakeStack(gui.mc.thePlayer))
                .map(s -> s.getStack().copy()).collect(Collectors.toList());

        for (PositionedStack stack : ingredients) {
            Optional<ItemStack> used = invStacks.stream().filter(is -> is.stackSize > 0 && stack.contains(is))
                    .findAny();

            itemPresenceSlots.add(new GuiOverlayButton.ItemOverlayState(stack, used.isPresent()));

            if (used.isPresent()) {
                ItemStack is = used.get();
                is.stackSize -= 1;
            }
        }

        return itemPresenceSlots;
    }

    default ArrayList<PositionedStack> positionStacks(G gui, ArrayList<PositionedStack> stacks) {
        return stacks;
    }
}
