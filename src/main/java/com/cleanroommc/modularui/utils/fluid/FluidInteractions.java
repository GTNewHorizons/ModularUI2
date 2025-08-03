package com.cleanroommc.modularui.utils.fluid;

import codechicken.nei.recipe.StackInfo;

import com.cleanroommc.modularui.ModularUI;

import gregtech.api.util.GTUtility;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidTank;

public class FluidInteractions {
    /**
     * Gets fluid actually stored in item. Used for transferring fluid.
     */
    public static FluidStack getFluidForRealItem(ItemStack itemStack) {
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
        if (ModularUI.Mods.GT5U.isLoaded() && fluidStack == null) {
            fluidStack = GTUtility.getFluidForFilledItem(itemStack, true);
        }
        return fluidStack;
    }

    /**
     * Gets fluid for use in phantom slot.
     */
    public static FluidStack getFluidForPhantomItem(ItemStack itemStack) {
        FluidStack fluidStack = null;
        if (itemStack.getItem() instanceof IFluidContainerItem container) {
            fluidStack = container.getFluid(itemStack.copy());
        }
        if (fluidStack == null) {
            fluidStack = FluidContainerRegistry.getFluidForFilledItem(itemStack.copy());
        }
        if (fluidStack == null && ModularUI.Mods.NEI.isLoaded()) {
            fluidStack = StackInfo.getFluid(itemStack.copy());
        }
        if (ModularUI.Mods.GT5U.isLoaded() && fluidStack == null) {
            fluidStack = GTUtility.getFluidForFilledItem(itemStack, true);
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
            if(filledContainer==null)
            {
                //Null return was not possible, even thought it was expected
                return filledContainer;
            }
            FluidStack newFluid = getFluidForRealItem(filledContainer);
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

    public static ItemStack getContainerForFilledItem(ItemStack itemStack) {
        ItemStack stack = getContainerForFilledItemWithoutIFluidContainerItem(itemStack);
        if (stack == null && itemStack.getItem() instanceof IFluidContainerItem container) {
            stack = itemStack.copy();
            container.drain(stack, Integer.MAX_VALUE, true);
        }
        if (stack == null) {
            stack = FluidContainerRegistry.drainFluidContainer(itemStack.copy());
        }
        return stack;
    }

    public static ItemStack getContainerForFilledItemWithoutIFluidContainerItem(ItemStack itemStack) {
        if (ModularUI.Mods.GT5U.isLoaded()) {
            return GTUtility.getContainerForFilledItem(itemStack, false);
        }
        return null;
    }

    public static int getRealCapacity(IFluidTank fluidTank) {
        if (fluidTank instanceof IOverflowableTank overflowable) {
            return overflowable.getRealCapacity();
        }
        return fluidTank.getCapacity();
    }
}
