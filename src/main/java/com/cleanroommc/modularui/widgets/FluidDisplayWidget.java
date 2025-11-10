package com.cleanroommc.modularui.widgets;

import net.minecraftforge.fluids.FluidStack;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.value.IValue;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.value.ObjectValue;
import com.cleanroommc.modularui.value.sync.GenericSyncValue;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;

public class FluidDisplayWidget extends Widget<FluidDisplayWidget> {

    private IValue<FluidStack> value;
    private boolean displayAmount = false;

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        if (syncHandler instanceof GenericSyncValue<?>genericSyncValue && genericSyncValue.isOfType(FluidStack.class)) {
            this.value = genericSyncValue.cast();
            return true;
        }
        return false;
    }

    @Override
    protected WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getItemSlotTheme();
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        FluidStack fluid = value.getValue();
        if ((fluid == null)) {
            return;
        }
        GuiDraw.drawFluidTexture(fluid, 0, 0, getArea().width, getArea().height, context.getCurrentDrawingZ());
        if (this.displayAmount) {
            GuiDraw.drawStandardSlotAmountText(fluid.amount, null, getArea());
        }
    }

    public FluidDisplayWidget fluid(IValue<FluidStack> fluidSupplier) {
        this.value = fluidSupplier;
        setValue(fluidSupplier);
        return this;
    }

    public FluidDisplayWidget fluid(FluidStack fluidStack) {
        return fluid(new ObjectValue<>(fluidStack));
    }

    public FluidDisplayWidget displayAmount(boolean displayAmount) {
        this.displayAmount = displayAmount;
        return this;
    }

}
