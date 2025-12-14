package com.cleanroommc.modularui.integration.nei;

import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.screen.ModularContainer;

import net.minecraft.client.gui.inventory.GuiContainer;

import codechicken.nei.PositionedStack;
import codechicken.nei.api.IStackPositioner;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

public class ModularUIGuiContainerStackPositioner<G extends GuiContainer & IMuiScreen> implements IStackPositioner {

    @Nullable
    @SuppressWarnings("unchecked")
    public static <G extends GuiContainer & IMuiScreen> ModularUIGuiContainerStackPositioner<G> of(GuiContainer gui, String ident) {
        if (gui instanceof IMuiScreen && gui.inventorySlots instanceof ModularContainer mc &&
                mc instanceof INEIRecipeTransfer<?> tr && Arrays.asList(tr.getIdents()).contains(ident)) {
            return new ModularUIGuiContainerStackPositioner<>((G) gui, (INEIRecipeTransfer<G>) tr);
        }
        return null;
    }

    public final G wrapper;
    public final INEIRecipeTransfer<G> recipeTransfer;

    public ModularUIGuiContainerStackPositioner(G wrapper, INEIRecipeTransfer<G> recipeTransfer) {
        this.wrapper = wrapper;
        this.recipeTransfer = recipeTransfer;
    }

    @Override
    public ArrayList<PositionedStack> positionStacks(ArrayList<PositionedStack> ai) {
        return recipeTransfer.positionStacks(wrapper, ai);
    }
}
