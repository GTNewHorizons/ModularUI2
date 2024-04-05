package com.cleanroommc.modularui.utils.fluid;

import static com.cleanroommc.modularui.ModularUI.isGT5ULoaded;

import codechicken.nei.recipe.StackInfo;
import gregtech.api.util.GT_Utility;
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
        if (isGT5ULoaded) {
            fluidStack = GT_Utility.getFluidForFilledItem(itemStack, true);
        }
        if (fluidStack == null && itemStack.getItem() instanceof IFluidContainerItem container) {
            fluidStack = container.getFluid(itemStack);
        }
        if (fluidStack == null) {
            fluidStack = FluidContainerRegistry.getFluidForFilledItem(itemStack);
        }
        if (fluidStack == null) {
            fluidStack = StackInfo.getFluid(itemStack);
        }
        return fluidStack;
    }

    /**
     * Gets fluid for use in phantom slot.
     */
    public static FluidStack getFluidForPhantomItem(ItemStack itemStack) {
        if (isGT5ULoaded) {
            return GT_Utility.getFluidFromContainerOrFluidDisplay(itemStack);
        } else {
            return StackInfo.getFluid(itemStack);
        }
    }

    public static ItemStack fillFluidContainer(FluidStack fluidStack, ItemStack itemStack) {
        ItemStack filledContainer = fillFluidContainerWithoutIFluidContainerItem(fluidStack, itemStack);
        if (filledContainer == null) {
            filledContainer = fillFluidContainerWithIFluidContainerItem(fluidStack, itemStack);
        }
        if (filledContainer == null) {
            filledContainer = FluidContainerRegistry.fillFluidContainer(fluidStack, itemStack);
            FluidStack newFluid = getFluidForRealItem(filledContainer);
            fluidStack.amount -= newFluid.amount;
        }
        return filledContainer;
    }

    public static ItemStack fillFluidContainerWithoutIFluidContainerItem(FluidStack fluidStack, ItemStack itemStack) {
        if (isGT5ULoaded) {
            return GT_Utility.fillFluidContainer(fluidStack, itemStack, true, false);
        }
        return null;
    }

    public static ItemStack fillFluidContainerWithIFluidContainerItem(FluidStack fluidStack, ItemStack itemStack) {
        if (itemStack.getItem() instanceof IFluidContainerItem) {
            IFluidContainerItem tContainerItem = (IFluidContainerItem) itemStack.getItem();
            int tFilledAmount = tContainerItem.fill(itemStack, fluidStack, true);
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
        if (isGT5ULoaded) {
            return GT_Utility.getContainerForFilledItem(itemStack, false);
        }
        return null;
    }

    public static int getRealCapacity(IFluidTank fluidTank) {
        if (fluidTank instanceof IOverflowableTank) {
            return ((IOverflowableTank) fluidTank).getRealCapacity();
        }
        return fluidTank.getCapacity();
    }
}