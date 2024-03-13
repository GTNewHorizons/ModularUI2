package com.cleanroommc.modularui.api;

import static com.google.common.primitives.Ints.saturatedCast;

import javax.annotation.Nullable;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;

public interface IFluidTankLong extends IFluidTank {

    /**
     * @param maxDrain The max amount to be drained
     * @param doDrain Do we actually drain the tank
     * @return The amount of fluid drained
     */
    long drainLong(long maxDrain, boolean doDrain);

    /**
     * @param fluid The fluid we want to put in
     * @param amount The amount we want to put in
     * @param doFill Do we actually fill the tank
     * @return The amount of fluid filled into the tank
     */
    long fillLong(@Nullable Fluid fluid, long amount, boolean doFill);

    long getCapacityLong();

    long getFluidAmountLong();

    Fluid getRealFluid();

    void setFluid(@Nullable Fluid fluid, long amount);

    @Override
    default FluidStack drain(int maxDrain, boolean doDrain) {
        final FluidStack fluid = new FluidStack(getRealFluid(), 0);
        fluid.amount = saturatedCast(drainLong(maxDrain, doDrain));
        return fluid;
    }

    @Override
    default int fill(FluidStack resource, boolean doFill) {
        return saturatedCast(fillLong(resource.getFluid(), resource.amount, doFill));
    }

    @Override
    default int getCapacity() {
        return saturatedCast(getCapacityLong());
    }

    @Override
    default FluidStack getFluid() {
        final FluidStack fluid = new FluidStack(getRealFluid(), getFluidAmount());
        return fluid;
    }

    @Override
    default int getFluidAmount() {
        return saturatedCast(getFluidAmountLong());
    }

    @Override
    default FluidTankInfo getInfo() {
        final FluidTankInfo info = new FluidTankInfo(getFluid(), getCapacity());
        return info;
    }

}