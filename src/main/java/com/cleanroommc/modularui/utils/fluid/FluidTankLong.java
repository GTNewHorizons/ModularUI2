package com.cleanroommc.modularui.utils.fluid;

import com.cleanroommc.modularui.api.IFluidTankLong;

import net.minecraftforge.fluids.Fluid;

public class FluidTankLong implements IFluidTankLong {

    private Fluid fluid;
    private long amount;
    private long capacity;
    private boolean locked = false;

    public FluidTankLong() {
        this(null, 0, 0);
    }

    public FluidTankLong(Fluid fluid) {
        this(fluid, 0 ,0);
    }

    public FluidTankLong(Fluid fluid, long capacity) {
        this(fluid, capacity, 0);
    }

    public FluidTankLong(long capacity) {
        this(null, capacity, 0);
    }

    public FluidTankLong(Fluid fluid, long capacity, long amount) {
        this.fluid = fluid;
        this.capacity = capacity;
        this.amount = amount;
    }

    @Override
    public long drainLong(long maxDrain, boolean doDrain) {
        long toDrain = Math.min(maxDrain, amount);

        if (doDrain) {
            amount -= toDrain;
            if (amount <= 0 && !locked) {
                this.fluid = null;
            }
        }

        return toDrain;
    }

    @Override
    public long fillLong(Fluid fluid, long amount, boolean doFill) {
        if (fluid == null) return 0;
        if (!doFill && this.fluid == null) return amount;
        if (doFill && this.fluid == null) this.fluid = fluid;
        if (this.fluid != fluid) return 0;
        long toFill = Math.min(amount, getCapacityLong() - getFluidAmountLong());
        if (doFill) {
            amount += toFill;
        }
        return toFill;
    }

    @Override
    public long getCapacityLong() {
        return capacity;
    }

    @Override
    public long getFluidAmountLong() {
        return amount;
    }

    @Override
    public Fluid getRealFluid() {
        return fluid;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isLocked() {
        return locked;
    }

    @Override
    public void setFluid(Fluid fluid, long amount) {
        this.fluid = fluid;
        this.amount = amount;
    }

    @Override
    public IFluidTankLong copy() {
        return new FluidTankLong(getRealFluid(), getCapacityLong(), getFluidAmountLong());
    }
}
