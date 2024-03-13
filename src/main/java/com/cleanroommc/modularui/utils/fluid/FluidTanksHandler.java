package com.cleanroommc.modularui.utils.fluid;

import java.util.Collections;
import java.util.List;

import com.cleanroommc.modularui.api.IFluidTankLong;

import net.minecraftforge.fluids.Fluid;

public class FluidTanksHandler implements IFluidTanksHandler {

    protected final List<IFluidTankLong> fluids;

    public FluidTanksHandler(IFluidTankLong tank) {
        fluids = Collections.singletonList(tank);
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
    public void setFluidInTank(int tankSlot, Fluid fluid, long amount) {
        IFluidTankLong tank = getTank(tankSlot);
        tank.setFluid(fluid, amount);
    }
}