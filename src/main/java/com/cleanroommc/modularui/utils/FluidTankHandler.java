package com.cleanroommc.modularui.utils;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;

import org.jetbrains.annotations.Nullable;

/**
 * Adapts an {@link IFluidTank} to the {@link IFluidHandler} interface.
 */
public class FluidTankHandler implements IFluidHandler {

    /**
     * Returns the tank itself if it already implements {@link IFluidHandler}, otherwise wraps it in a new {@link FluidTankHandler}.
     *
     * @param tank the tank to adapt
     * @return a fluid handler for the tank
     */
    public static IFluidHandler getTankFluidHandler(IFluidTank tank) {
        if (tank instanceof IFluidHandler fluidHandler) {
            return fluidHandler;
        }
        return new FluidTankHandler(tank);
    }

    private final IFluidTank fluidTank;

    /**
     * Creates a handler that delegates to the given tank.
     *
     * @param tank the tank to wrap
     */
    public FluidTankHandler(IFluidTank tank) {
        this.fluidTank = tank;
    }

    /**
     * Returns a single tank info entry describing the wrapped tank's current fluid and capacity.
     *
     * @param from the side being queried, ignored
     * @return an array with one {@link FluidTankInfo}
     */
    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return new FluidTankInfo[]{
                new FluidTankInfo(this.fluidTank.getFluid(), this.fluidTank.getCapacity())
        };
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        return this.fluidTank.fill(resource, doFill);
    }

    /**
     * Drains the requested fluid from the tank, only if the tank currently holds that same fluid.
     *
     * @param from    the side being drained, ignored
     * @param resource the fluid and amount to drain
     * @param doDrain whether to actually remove the fluid
     * @return the drained fluid, or null if the tank is empty or holds a different fluid
     */
    @Nullable
    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        FluidStack currentFluid = this.fluidTank.getFluid();
        if (currentFluid == null || currentFluid.amount <= 0 || !currentFluid.isFluidEqual(resource)) {
            return null;
        }
        return this.fluidTank.drain(resource.amount, doDrain);
    }

    @Nullable
    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        return this.fluidTank.drain(maxDrain, doDrain);
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return true;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return true;
    }
}
