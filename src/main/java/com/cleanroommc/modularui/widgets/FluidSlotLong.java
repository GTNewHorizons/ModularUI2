package com.cleanroommc.modularui.widgets;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import com.cleanroommc.modularui.api.IFluidTankLong;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.TextRenderer;
import com.cleanroommc.modularui.screen.Tooltip;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.NumberFormat;
import com.cleanroommc.modularui.utils.fluid.FluidTankLong;
import com.cleanroommc.modularui.utils.fluid.FluidTankLongDelegate;
import com.cleanroommc.modularui.value.sync.FluidSlotLongSyncHandler;
import com.cleanroommc.modularui.value.sync.SyncHandler;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

public class FluidSlotLong extends FluidSlot<FluidSlotLong> {

    private FluidSlotLongSyncHandler syncHandler;

    @Override
    protected void addToolTip(Tooltip tooltip) {
        IFluidTankLong fluidTank = getFluidTankLong();
        FluidStack fluid = fluidTank.getFluid();
        if (this.syncHandler.isPhantom()) {
            if (fluid != null) {
                tooltip.addLine(IKey.str(fluid.getLocalizedName()));
                if (this.syncHandler.controlsAmount()) {
                    tooltip.addLine(IKey.lang("modularui2.fluid.phantom.amount",
                            formatFluidAmount(fluidTank.getFluidAmountLong()),
                            getBaseUnit()));
                }
            } else {
                tooltip.addLine(IKey.lang("modularui2.fluid.empty"));
            }
            if (this.syncHandler.controlsAmount()) {
                tooltip.addLine(IKey.lang("modularui2.fluid.phantom.control"));
            }
        } else {
            if (fluid != null) {
                tooltip.addLine(IKey.str(fluid.getLocalizedName()));
                tooltip.addLine(IKey.lang("modularui2.fluid.amount", formatFluidAmount(fluidTank.getFluidAmountLong()),
                        formatFluidAmount(fluidTank.getCapacityLong()), getBaseUnit()));
                addAdditionalFluidInfo(tooltip, fluid);
            } else {
                tooltip.addLine(IKey.lang("modularui2.fluid.empty"));
            }
            if (this.syncHandler.canFillSlot() || this.syncHandler.canDrainSlot()) {
                tooltip.addLine(IKey.EMPTY); // Add an empty line to separate from the bottom material tooltips
                if (Interactable.hasShiftDown()) {
                    if (this.syncHandler.canFillSlot() && this.syncHandler.canDrainSlot()) {
                        tooltip.addLine(IKey.lang("modularui2.fluid.click_combined"));
                    } else if (this.syncHandler.canDrainSlot()) {
                        tooltip.addLine(IKey.lang("modularui2.fluid.click_to_fill"));
                    } else if (this.syncHandler.canFillSlot()) {
                        tooltip.addLine(IKey.lang("modularui2.fluid.click_to_empty"));
                    }
                } else {
                    tooltip.addLine(IKey.lang("modularui.tooltip.shift"));
                }
            }
        }
    }

    @Override
    public void draw(GuiContext context, WidgetTheme widgetTheme) {
        IFluidTankLong fluidTank = getFluidTankLong();
        if (fluidTank.getFluid() != null) {
            int y = getContentOffsetY();
            float height = getArea().height - y * 2;
            if (!showFull()) {
                float newHeight = height * fluidTank.getFluidAmountLong() * 1f / fluidTank.getCapacityLong();
                y += (int) (height - newHeight);
                height = newHeight;
            }
            GuiDraw.drawFluidTexture(fluidTank.getFluid(), getContentOffsetX(), y, getArea().width - getContentOffsetX() * 2, height, 0);
        }
        if (getOverlayTexture() != null) {
            getOverlayTexture().drawAtZero(context, getArea(), widgetTheme);
        }
        if (fluidTank.getFluid() != null && this.syncHandler.controlsAmount()) {
            String s = NumberFormat.formatWithMaxDigits(getBaseUnitAmount(fluidTank.getFluidAmountLong())) + getBaseUnit();
            TextRenderer textRenderer = getTextRenderer();
            textRenderer.setAlignment(Alignment.CenterRight, getArea().width - getContentOffsetX() - 1f);
            textRenderer.setPos((int) (getContentOffsetX() + 0.5f), (int) (getArea().height - 5.5f));
            textRenderer.draw(s);
        }
        if (isHovering()) {
            GL11.glColorMask(true, true, true, false);
            GuiDraw.drawRect(1, 1, getArea().w() - 2, getArea().h() - 2, getWidgetTheme(context.getTheme()).getSlotHoverColor());
            GL11.glColorMask(true, true, true, true);
        }
    }

    @NotNull
    protected IFluidTankLong getFluidTankLong() {
        return syncHandler.getValue();
    }

    @Override
    public IFluidTank getFluidTank() {
        return getFluidTankLong();
    }

    @Override
    public @Nullable FluidStack getFluidStack() {
        return getFluidTankLong().getFluid();
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        if (!(syncHandler instanceof FluidSlotLongSyncHandler fluidSlotSyncHandler)) {
            return false;
        }
        this.syncHandler = fluidSlotSyncHandler;
        return true;
    }

    @Override
    public FluidSlotLong syncHandler(IFluidTank fluidTank) {
        if (!(fluidTank instanceof IFluidTankLong fluidTankLong)) {
            return syncHandler(new FluidTankLongDelegate(fluidTank));
        }
        return syncHandler(fluidTankLong);
    }

    public FluidSlotLong syncHandler(IFluidTankLong fluidTank) {
        return syncHandler(new FluidSlotLongSyncHandler(fluidTank));
    }

    public FluidSlotLong syncHandler(FluidSlotLongSyncHandler syncHandler) {
        this.syncHandler = syncHandler;
        setSyncHandler(syncHandler);
        return getThis();
    }

}
