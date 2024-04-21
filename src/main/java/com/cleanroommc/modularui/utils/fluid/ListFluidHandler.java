package com.cleanroommc.modularui.utils.fluid;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.cleanroommc.modularui.api.IFluidTankLong;

import net.minecraftforge.fluids.Fluid;

public class ListFluidHandler implements IFluidTanksHandler {

    protected final List<? extends IFluidTanksHandler> fluidHandlers;

    public ListFluidHandler(List<? extends IFluidTanksHandler> fluidHandlers) {
        this.fluidHandlers = fluidHandlers;
    }

    @Override
    public int getTanks() {
        int tanks = 0;

        for (IFluidTanksHandler handler : fluidHandlers) {
            tanks += handler.getTanks();
        }

        return tanks;
    }

    @Override
    public @Nonnull IFluidTankLong getTank(int tankSlot) {
        Pair<? extends IFluidTanksHandler, Integer> result = findFluidHandler(tankSlot);
        return result.getLeft().getTank(result.getRight());
    }

    @Override
    public void setFluidInTank(int tankSlot, @Nullable Fluid fluid, long amount) {
        Pair<? extends IFluidTanksHandler, Integer> result = findFluidHandler(tankSlot);
        result.getLeft().setFluidInTank(result.getRight(), fluid, amount);
    }

    protected Pair<? extends IFluidTanksHandler, Integer> findFluidHandler(int tankSlot) {
        int searching = 0;

        for (IFluidTanksHandler handler : fluidHandlers) {

            if (tankSlot >= searching  && tankSlot < searching + handler.getTanks()) {
                return new ImmutablePair<>(handler, tankSlot - searching);
            }
            searching += handler.getTanks();
        }

        throw new RuntimeException("Tank slot " + tankSlot + " not in valid range - [0," + getTanks() + ")" );
    }
}
