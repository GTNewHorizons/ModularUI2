package com.cleanroommc.modularui.integration.nei;

import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.api.IStackPositioner;
import codechicken.nei.recipe.GuiOverlayButton;
import codechicken.nei.recipe.IRecipeHandler;
import com.cleanroommc.modularui.screen.GuiContainerWrapper;
import com.cleanroommc.modularui.screen.ModularContainer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface INEIRecipeTransfer<Self extends ModularContainer> {
    public String[] getIdents();

    public default void overlayRecipe(GuiContainerWrapper gui, ModularContainer self, IRecipeHandler recipe, int recipeIndex, boolean maxTransfer) {
        transferRecipe(gui,self, recipe, recipeIndex, maxTransfer ? Integer.MAX_VALUE : 1);
    }

    public int transferRecipe(GuiContainerWrapper gui, ModularContainer self, IRecipeHandler recipe, int recipeIndex, int multiplier);

    public default boolean canFillCraftingGrid(GuiContainerWrapper gui, ModularContainer self, IRecipeHandler recipe, int recipeIndex) {
        return true;
    }

    public default boolean craft(GuiContainerWrapper gui, ModularContainer self, IRecipeHandler recipe, int recipeIndex, int multiplier) {
        return false;
    }

    public default boolean canCraft(GuiContainerWrapper gui, ModularContainer self, IRecipeHandler recipe, int recipeIndex) {
        return false;
    }

    public default List<GuiOverlayButton.ItemOverlayState> presenceOverlay(GuiContainerWrapper gui, ModularContainer self, IRecipeHandler recipe, int recipeIndex) {
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

    public default ArrayList<PositionedStack> positionStacks(GuiContainerWrapper gui, ModularContainer self, ArrayList<PositionedStack> stacks)     {
        return stacks;
    }
}
