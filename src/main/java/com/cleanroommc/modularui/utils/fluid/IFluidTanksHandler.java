package com.cleanroommc.modularui.utils.fluid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.cleanroommc.modularui.api.IFluidTankLong;

import net.minecraftforge.fluids.Fluid;

public interface IFluidTanksHandler {

    int getTanks();

    @Nonnull
    IFluidTankLong getTank(int tankSlot);

    @Nullable
    default Fluid getFluidInTank(int tankSlot) {
        return getTank(tankSlot).getRealFluid();
    }

    default long fill(int tankSlot, @Nonnull Fluid fluid, long amount, boolean doFill) {
        return getTank(tankSlot).fillLong(fluid, amount, doFill);
    }

    default long drain(int tankSlot, long amount, boolean doDrain) {
        return getTank(tankSlot).drainLong(amount, doDrain);
    }

    default long getTankCapacity(int tankSlot) {
        return getTank(tankSlot).getCapacityLong();
    }

    default long getTankAmount(int tankSlot) {
        return getTank(tankSlot).getFluidAmountLong();
    }

    default boolean isFluidValid(int tankSlot, Fluid fluid) {
        return true;
    }

    void setFluidInTank(int tankSlot, @Nonnull Fluid fluid, long amount);

    default void setFluidInTank(int tankSlot, @Nonnull Fluid fluid) {
        setFluidInTank(tankSlot, fluid, 0);
    }

    default void setFluidInTank(int tankSlot, @Nonnull IFluidTankLong tank) {
        setFluidInTank(tankSlot, tank.getRealFluid(), tank.getFluidAmountLong());
    }
}
