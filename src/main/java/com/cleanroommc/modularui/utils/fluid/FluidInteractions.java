package com.cleanroommc.modularui.utils.fluid;

import com.cleanroommc.modularui.ModularUI;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidTank;

import codechicken.nei.recipe.StackInfo;
import gregtech.api.util.GTUtility;

public class FluidInteractions {

    public static FluidStack getFluidForItem(ItemStack itemStack) {
        FluidStack fluidStack = null;
        if (itemStack.getItem() instanceof IFluidContainerItem container) {
            fluidStack = container.getFluid(itemStack);
        }
        if (fluidStack == null) {
            fluidStack = FluidContainerRegistry.getFluidForFilledItem(itemStack);
        }
        if (fluidStack == null && ModularUI.Mods.NEI.isLoaded()) {
            fluidStack = StackInfo.getFluid(itemStack);
        }
        if (fluidStack == null && ModularUI.Mods.GT5U.isLoaded()) {
            fluidStack = GTUtility.getFluidForFilledItem(itemStack, false);
        }
        return fluidStack;
    }

    public static ItemStack fillFluidContainer(FluidStack fluidStack, ItemStack itemStack) {
        ItemStack filledContainer = fillFluidContainerWithoutIFluidContainerItem(fluidStack, itemStack);
        if (filledContainer == null) {
            filledContainer = fillFluidContainerWithIFluidContainerItem(fluidStack, itemStack);
        }
        if (filledContainer == null) {
            filledContainer = FluidContainerRegistry.fillFluidContainer(fluidStack, itemStack);
            if (filledContainer == null) return itemStack; // give up and return original clicked stack
            FluidStack newFluid = getFluidForItem(filledContainer);
            fluidStack.amount -= newFluid.amount;
        }
        return filledContainer;
    }

    public static ItemStack fillFluidContainerWithoutIFluidContainerItem(FluidStack fluidStack, ItemStack itemStack) {
        if (ModularUI.Mods.GT5U.isLoaded()) {
            return GTUtility.fillFluidContainer(fluidStack, itemStack, true, false);
        }
        return null;
    }

    public static ItemStack fillFluidContainerWithIFluidContainerItem(FluidStack fluidStack, ItemStack itemStack) {
        if (itemStack.getItem() instanceof IFluidContainerItem itemContainer) {
            int tFilledAmount = itemContainer.fill(itemStack, fluidStack, true);
            if (tFilledAmount > 0) {
                fluidStack.amount -= tFilledAmount;
                return itemStack;
            }
        }
        return null;
    }

    public static ItemStack getEmptyContainerForFilledItem(ItemStack itemStack) {
        if (ModularUI.Mods.GT5U.isLoaded()) {
            ItemStack stack = GTUtility.getContainerForFilledItem(itemStack, false);
            if (stack != null) {
                return stack;
            }
        }

        if (itemStack.getItem() instanceof IFluidContainerItem container) {
            ItemStack stack = itemStack.copy();
            int amount = container.getFluid(itemStack).amount;
            FluidStack drained = container.drain(stack, Integer.MAX_VALUE, true);

            if (drained == null || drained.amount < amount) {
                return null;
            }
            return stack;
        }

        return FluidContainerRegistry.drainFluidContainer(itemStack);
    }

    public static int getRealCapacity(IFluidTank fluidTank) {
        if (fluidTank instanceof IOverflowableTank overflowable) {
            return overflowable.getRealCapacity();
        }
        return fluidTank.getCapacity();
    }
}
