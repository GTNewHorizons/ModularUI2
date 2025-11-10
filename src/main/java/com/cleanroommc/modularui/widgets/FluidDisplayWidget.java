package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.drawable.text.TextRenderer;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.NumberFormat;
import com.cleanroommc.modularui.widget.sizer.Area;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
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
    public static TextRenderer textRenderer = new TextRenderer();


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
        if (fluid == null) return;
        GuiDraw.drawFluidTexture(fluid, 0, 0, getArea().width, getArea().height, context.getCurrentDrawingZ());
        if (this.displayAmount) {
            GuiDraw.drawScaledAmountText(fluid.amount, null, 1, 1, this.getArea().width-1,
                    this.getArea().height-1, Alignment.BottomRight, 1);
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
