package com.cleanroommc.modularui.utils;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntFunction;

/**
 * A fluid tank handler that aggregates multiple {@link IFluidTank}s into one logical handler.
 * Fill and drain operations are distributed across the tanks in order.
 */
public class MultiFluidTankHandler implements IMultiFluidTankHandler {

    private final IFluidTank[] tanks;

    /**
     * Creates a handler from the given tanks. The array is copied.
     *
     * @param tanks tanks to aggregate, none of them may be null
     * @throws NullPointerException if the array or any tank is null
     */
    public MultiFluidTankHandler(IFluidTank... tanks) {
        Objects.requireNonNull(tanks);
        for (IFluidTank tank : tanks) {
            Objects.requireNonNull(tank);
        }
        this.tanks = Arrays.copyOf(tanks, tanks.length);
    }

    /**
     * Creates a handler with {@code count} standard {@link FluidTank}s of the same capacity.
     *
     * @param count    number of tanks
     * @param capacity capacity of each tank in mB
     */
    public MultiFluidTankHandler(int count, int capacity) {
        this(count, i -> new FluidTank(capacity));
    }

    /**
     * Creates a handler with {@code count} tanks, each created by {@code tankBuilder} using its index.
     *
     * @param count       number of tanks
     * @param tankBuilder function that creates the tank for a given index, must not return null
     * @throws NullPointerException if {@code tankBuilder} returns null for any index
     */
    public MultiFluidTankHandler(int count, IntFunction<IFluidTank> tankBuilder) {
        this.tanks = new IFluidTank[count];
        for (int i = 0; i < count; i++) {
            this.tanks[i] = Objects.requireNonNull(tankBuilder.apply(i));
        }
    }

    @Override
    public int getTankCount() {
        return this.tanks.length;
    }

    @Override
    public IFluidTank getFluidTank(int index) {
        return this.tanks[index];
    }

    /**
     * Drains matching fluid from the tanks starting at {@code startIndex}, until the requested amount is reached
     * or no more matching fluid is available.
     *
     * @param startIndex index of the first tank to drain from
     * @param resource   the fluid and amount to drain
     * @param doDrain    whether to actually remove the fluid
     * @return a stack with the amount drained, or null if nothing was drained
     */
    private @Nullable FluidStack drain(int startIndex, FluidStack resource, boolean doDrain) {
        if (startIndex >= this.tanks.length) return null;
        int drainAmount = resource.amount;
        for (int i = startIndex; i < this.tanks.length; i++) {
            IFluidTank tank = this.tanks[i];
            if (isFluidEmpty(tank.getFluid()) || !resource.isFluidEqual(tank.getFluid())) continue;
            FluidStack d = this.tanks[i].drain(drainAmount, doDrain);
            if (!isFluidEmpty(d)) {
                drainAmount -= d.amount;
                if (drainAmount <= 0) return resource.copy();
            }
        }
        if (drainAmount == resource.amount) return null;
        FluidStack drained = resource.copy();
        drained.amount -= drainAmount;
        return drained;
    }

    /**
     * Checks whether the stack contains no fluid.
     *
     * @param f the stack to check, may be null
     * @return true if the stack is null, has no fluid type, or has an amount of zero or less
     */
    public static boolean isFluidEmpty(FluidStack f) {
        return f == null || f.getFluid() == null || f.amount <= 0;
    }

    /**
     * Fills the tanks. Tanks that already hold the same fluid are filled first, then empty tanks.
     *
     * @param from   the side the fluid comes from, not used by this handler
     * @param resource the fluid and amount to insert
     * @param doFill whether to actually insert the fluid
     * @return the amount of fluid accepted by the tanks
     */
    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        if (isFluidEmpty(resource)) return 0;
        int fillAmount = resource.amount;
        FluidStack toFill = resource.copy();
        // first find tanks with matching fluid
        for (IFluidTank tank : this.tanks) {
            if (!isFluidEmpty(tank.getFluid()) && resource.isFluidEqual(tank.getFluid())) {
                fillAmount -= tank.fill(toFill, doFill);
                toFill.amount = fillAmount;
                if (fillAmount <= 0) return resource.amount;
            }
        }
        // if still fluid there, insert into empty tanks
        for (IFluidTank tank : this.tanks) {
            if (isFluidEmpty(tank.getFluid())) {
                fillAmount -= tank.fill(toFill, doFill);
                toFill.amount = fillAmount;
                if (fillAmount <= 0) return resource.amount;
            }
        }
        return resource.amount - fillAmount;
    }

    /**
     * Drains the given fluid from the tanks in order, up to the requested amount.
     *
     * @param from    the side the fluid is drained from, not used by this handler
     * @param resource the fluid and amount to drain
     * @param doDrain whether to actually remove the fluid
     * @return the drained fluid, or null if nothing was drained or the resource is empty
     */
    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        if (isFluidEmpty(resource)) return null;
        return drain(0, resource, doDrain);
    }

    /**
     * Drains up to {@code maxDrain} from the first tank that holds fluid. If that tank does not supply enough,
     * the remainder is drained from later tanks holding the same fluid.
     *
     * @param from     the side the fluid is drained from, not used by this handler
     * @param maxDrain maximum amount to drain
     * @param doDrain  whether to actually remove the fluid
     * @return the drained fluid, or null if no tank holds fluid
     */
    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        for (int i = 0; i < this.tanks.length; i++) {
            FluidStack drained = this.tanks[i].drain(maxDrain, doDrain);
            if (!isFluidEmpty(drained)) {
                // if already drained enough from this slot return
                if (drained.amount >= maxDrain) return drained;
                // otherwise drain from all other slots with the same fluid
                FluidStack toDrain = drained.copy();
                toDrain.amount = maxDrain - toDrain.amount;
                FluidStack drained2 = drain(i + 1, toDrain, doDrain);
                if (isFluidEmpty(drained2)) return drained;
                drained.amount += drained2.amount;
                return drained;
            }
        }
        return null;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return true;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return true;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        FluidTankInfo[] info = new FluidTankInfo[this.tanks.length];
        for (int i = 0; i < info.length; i++) {
            info[i] = this.tanks[i].getInfo();
        }
        return info;
    }
}
