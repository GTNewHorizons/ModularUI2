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

    public static ItemStack getFilledFluidContainer(FluidStack fluidStack, ItemStack itemStack) {
        ItemStack filledContainer = null;

        if (ModularUI.Mods.GT5U.isLoaded()) {
            filledContainer = GTUtility.fillFluidContainer(fluidStack, itemStack, false, false);
        }

        if (filledContainer == null && itemStack.getItem() instanceof IFluidContainerItem container) {
            FluidStack containerFluid = container.getFluid(itemStack);
            int containerFluidAmount = containerFluid != null ? containerFluid.amount : 0;

            if (containerFluid != null && containerFluid.getFluid() != fluidStack.getFluid()) {
                return null;
            }

            if (containerFluidAmount + fluidStack.amount >= container.getCapacity(itemStack)) {
                ItemStack copyStack = itemStack.copy();
                container.fill(copyStack, fluidStack, true);
                return copyStack;
            }

            return null;
        }

        if (filledContainer == null) {
            filledContainer = FluidContainerRegistry.fillFluidContainer(fluidStack, itemStack);
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
