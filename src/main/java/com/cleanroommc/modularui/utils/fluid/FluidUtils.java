package com.cleanroommc.modularui.utils.fluid;

import com.cleanroommc.modularui.ModularUI;

import gregtech.api.util.GTUtility;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import org.jetbrains.annotations.Nullable;

public class FluidUtils {

    @Nullable
    public static FluidStack copyFluid(@Nullable FluidStack fluidStack) {
        return fluidStack == null ? null : fluidStack.copy();
    }

    public static long fluidAmount(@Nullable FluidStack fluidStack) {
        if (fluidStack == null) return 0;
        if (ModularUI.Mods.GT5U.isLoaded()) return GTUtility.getFluidAmount(fluidStack);
        else return fluidStack.amount;
    }

    public static long fluidAmount(IFluidTank fluidTank) {
        if (fluidTank instanceof IFluidTankLong fluidTankLong) {
            return fluidTankLong.getFluidAmountLong();
        } else {
            return fluidTank.getFluidAmount();
        }
    }

    public static long fluidCapacity(IFluidTank fluidTank) {
        if (fluidTank instanceof IFluidTankLong fluidTankLong) {
            return fluidTankLong.getCapacityLong();
        } else {
            return fluidTank.getCapacity();
        }
    }
}
