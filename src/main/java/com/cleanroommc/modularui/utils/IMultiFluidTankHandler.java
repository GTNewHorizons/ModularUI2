package com.cleanroommc.modularui.utils;

import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;

public interface IMultiFluidTankHandler extends IFluidHandler {

    int getTankCount();

    IFluidTank getFluidTank(int index);
}
