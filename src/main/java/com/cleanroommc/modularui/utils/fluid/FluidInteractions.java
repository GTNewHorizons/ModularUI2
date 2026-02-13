package com.cleanroommc.modularui.utils.fluid;

import com.cleanroommc.modularui.ModularUI;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidTank;

import codechicken.nei.recipe.StackInfo;

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
        return fluidStack;
    }

    public static ItemStack getFullFluidContainer(ItemStack itemStack, FluidStack fluidToFill) {
        if (itemStack.getItem() instanceof IFluidContainerItem container) {
            FluidStack containerFluid = container.getFluid(itemStack);
            int containerFluidAmount = containerFluid != null ? containerFluid.amount : 0;

            ItemStack copyStack = itemStack.copy();
            int filled = container.fill(copyStack, fluidToFill, true);

            if (containerFluidAmount + filled == container.getCapacity(copyStack)) {
                return copyStack;
            }

            return null;
        }

        return FluidContainerRegistry.fillFluidContainer(fluidToFill, itemStack);
    }

    public static ItemStack getEmptyFluidContainer(ItemStack itemStack) {
        if (itemStack.getItem() instanceof IFluidContainerItem container) {
            FluidStack fluidStack = container.getFluid(itemStack);
            if (fluidStack == null) {
                return null;
            }

            ItemStack stack = itemStack.copy();
            FluidStack drained = container.drain(stack, Integer.MAX_VALUE, true);

            if (drained == null || drained.amount < fluidStack.amount) {
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
