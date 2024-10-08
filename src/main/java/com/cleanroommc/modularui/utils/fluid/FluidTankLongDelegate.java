package com.cleanroommc.modularui.utils.fluid;

import static com.google.common.primitives.Ints.saturatedCast;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
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

    @Override
    public void setFluid(Fluid fluid, long amount) {
        delegate.drain(Integer.MAX_VALUE, true);
        delegate.fill(new FluidStack(fluid, saturatedCast(amount)), true);
    }

    @Override
    public IFluidTankLong copy() {
        return new FluidTankLongDelegate(new FluidTank(delegate.getFluid(), delegate.getCapacity()));
    }

    @Override
    public IFluidTankLong readFromNBT(NBTTagCompound fluidTag) {
        delegate.drain(Integer.MAX_VALUE, true);
        delegate.fill(FluidStack.loadFluidStackFromNBT(fluidTag), true);
        return this;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound fluidTag) {
        if (delegate.getFluid() == null) {
            return fluidTag;
        }
        delegate.getFluid().writeToNBT(fluidTag);
        return fluidTag;
    }
}
