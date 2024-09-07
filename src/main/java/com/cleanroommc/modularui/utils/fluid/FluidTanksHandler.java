package com.cleanroommc.modularui.utils.fluid;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraftforge.fluids.Fluid;

public class FluidTanksHandler implements IFluidTanksHandler {

    protected final List<IFluidTankLong> fluids;

    public FluidTanksHandler(IFluidTankLong tank) {
        fluids = Collections.singletonList(tank);
    }

    public FluidTanksHandler(int size) {
        this(size, 10000);
    }

    public FluidTanksHandler(int size, long capacity) {
        FluidTankLong[] fluids = new FluidTankLong[size];
        for (int i = 0; i < fluids.length; i++) {
            fluids[i] = new FluidTankLong(capacity);
        }
        this.fluids = Arrays.asList(fluids);
    }

    @Override
    public int getTanks() {
        return fluids.size();
    }

    @Override
    public IFluidTankLong getTank(int tankSlot) {
        return fluids.get(tankSlot);
    }

    @Override
    public void setFluidInTank(int tankSlot, @Nullable Fluid fluid, long amount) {
        IFluidTankLong tank = getTank(tankSlot);
        tank.setFluid(fluid, amount);
    }
}
