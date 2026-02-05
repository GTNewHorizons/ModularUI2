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

    public static ItemStack getFullFluidContainer(ItemStack itemStack, FluidStack fluidToFill) {
        ItemStack filledContainer = null;

        if (ModularUI.Mods.GT5U.isLoaded()) {
            filledContainer = GTUtility.fillFluidContainer(fluidToFill, itemStack, false, false);
        }

        if (filledContainer == null && itemStack.getItem() instanceof IFluidContainerItem container) {
            FluidStack containerFluid = container.getFluid(itemStack);
            int containerFluidAmount = containerFluid != null ? containerFluid.amount : 0;

            if (containerFluid != null && containerFluid.getFluid() != fluidToFill.getFluid()) {
                return null;
            }

            ItemStack copyStack = itemStack.copy();
            int filled = container.fill(copyStack, fluidToFill, true);

            if (containerFluidAmount + filled == container.getCapacity(copyStack)) {
                return copyStack;
            }

            return null;
        }

        if (filledContainer == null) {
            filledContainer = FluidContainerRegistry.fillFluidContainer(fluidToFill, itemStack);
        }

        return filledContainer;
    }

    public static ItemStack getEmptyFluidContainer(ItemStack itemStack) {
        if (ModularUI.Mods.GT5U.isLoaded()) {
            ItemStack stack = GTUtility.getContainerForFilledItem(itemStack, false);
            if (stack != null) {
                return stack;
            }
        }

        if (itemStack.getItem() instanceof IFluidContainerItem container) {
            ItemStack stack = itemStack.copy();
            FluidStack fluidStack = container.getFluid(itemStack);
            FluidStack drained = container.drain(stack, Integer.MAX_VALUE, true);

            if (drained == null || fluidStack == null || drained.amount < fluidStack.amount) {
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
