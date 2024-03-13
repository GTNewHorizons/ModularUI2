package com.cleanroommc.modularui.utils.fluid;

import static com.google.common.primitives.Ints.saturatedCast;

import com.cleanroommc.modularui.api.IFluidTankLong;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

public class FluidTankLongDelegate implements IFluidTankLong {

    IFluidTank delegate;

    public FluidTankLongDelegate(IFluidTank tank) {
        delegate = tank;
    }

    @Override
    public long drainLong(long maxDrain, boolean doDrain) {
        final FluidStack fluid = delegate.drain(saturatedCast(maxDrain), doDrain);
        return fluid.amount;
    }

    @Override
    public long fillLong(Fluid fluid, long amount, boolean doFill) {
        final FluidStack fluidStack = new FluidStack(fluid, saturatedCast(amount));
        return delegate.fill(fluidStack, doFill);
    }

    @Override
    public long getCapacityLong() {
        return delegate.getCapacity();
    }

    @Override
    public long getFluidAmountLong() {
        return delegate.getFluidAmount();
    }

    @Override
    public Fluid getRealFluid() {
        FluidStack fluid = delegate.getFluid();
        if (fluid == null) return null;
        return fluid.getFluid();
    }
}