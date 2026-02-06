package com.cleanroommc.modularui.widgets.slot;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.ISyncOrValue;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerGhostIngredientSlot;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.SlotTheme;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.GlStateManager;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.value.sync.FluidSlotSyncHandler;
import com.cleanroommc.modularui.widgets.AbstractFluidDisplayWidget;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;

import gregtech.api.util.GTUtility;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

import java.text.DecimalFormat;

public class FluidSlot extends AbstractFluidDisplayWidget<FluidSlot> implements Interactable, RecipeViewerGhostIngredientSlot<FluidStack> {

    private static final DecimalFormat TOOLTIP_FORMAT = new DecimalFormat("#.##");
    private static final IFluidTank EMPTY = new FluidTank(0);

    static {
        TOOLTIP_FORMAT.setGroupingUsed(true);
        TOOLTIP_FORMAT.setGroupingSize(3);
    }

    private FluidSlotSyncHandler syncHandler;
    private boolean alwaysShowFull = true;

    public FluidSlot() {
        tooltip().setAutoUpdate(true);
        tooltipBuilder(this::addToolTip);
    }

    protected void addToolTip(RichTooltip tooltip) {
        IFluidTank fluidTank = getFluidTank();
        FluidStack fluid = this.syncHandler.getValue();
        if (fluid != null) {
            tooltip.addFromFluid(fluid);
        }
        if (this.syncHandler.isPhantom()) {
            if (fluid != null) {
                if (this.syncHandler.controlsAmount()) {
                    tooltip.addLine(IKey.lang("modularui2.fluid.phantom.amount", formatFluidTooltipAmount(fluid.amount), getBaseUnit()));
                }
                addAdditionalFluidInfo(tooltip, fluid);
            } else {
                tooltip.addLine(IKey.lang("modularui2.fluid.empty"));
            }
            if (this.syncHandler.controlsAmount()) {
                tooltip.addLine(IKey.lang("modularui2.fluid.phantom.control"));
            } else {
                tooltip.addLine(IKey.lang("modularui2.fluid.phantom.clear"));
            }
        } else {
            if (fluid != null) {
                tooltip.addLine(IKey.lang("modularui2.fluid.amount", formatFluidTooltipAmount(fluid.amount), formatFluidTooltipAmount(fluidTank.getCapacity()), getBaseUnit()));
                addAdditionalFluidInfo(tooltip, fluid);
            } else {
                tooltip.addLine(IKey.lang("modularui2.fluid.empty"));
                tooltip.addLine(IKey.lang("modularui2.fluid.capacity", formatFluidTooltipAmount(fluidTank.getCapacity()), getUnit()));
            }
            if (this.syncHandler.canFillSlot() || this.syncHandler.canDrainSlot()) {
                tooltip.addLine(IKey.EMPTY); // Add an empty line to separate from the bottom material tooltips
                if (Interactable.hasShiftDown()) {
                    if (this.syncHandler.canFillSlot() && this.syncHandler.canDrainSlot()) {
                        tooltip.addLine(IKey.lang("modularui2.fluid.click_combined"));
                    } else if (this.syncHandler.canFillSlot()) {
                        tooltip.addLine(IKey.lang("modularui2.fluid.click_to_fill"));
                    } else if (this.syncHandler.canDrainSlot()) {
                        tooltip.addLine(IKey.lang("modularui2.fluid.click_to_empty"));
                    }
                } else {
                    tooltip.addLine(IKey.lang("modularui2.tooltip.shift"));
                }
            }
        }
    }

    public void addAdditionalFluidInfo(RichTooltip tooltip, FluidStack fluidStack) {
        tooltip.addAdditionalInfoFromFluid(fluidStack);
    }

    public String formatFluidTooltipAmount(double amount) {
        // the tooltip show the full number
        // 1.7.10 hardcoded to use UNIT_LITER for now, so no milli-buckets.
        return TOOLTIP_FORMAT.format(amount);
    }

    @Override
    public boolean isValidSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        return syncOrValue.isTypeOrEmpty(FluidSlotSyncHandler.class);
    }

    @Override
    protected void setSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        super.setSyncOrValue(syncOrValue);
        this.syncHandler = syncOrValue.castNullable(FluidSlotSyncHandler.class);
    }

    @Override
    protected boolean displayAmountText() {
        return this.syncHandler == null || this.syncHandler.controlsAmount();
    }

    @Override
    public void drawOverlay(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.drawOverlay(context, widgetTheme);
        if (isHovering()) {
            GuiDraw.drawRect(1, 1, getArea().w() - 2, getArea().h() - 2, getSlotHoverColor());
        }
    }

    @Override
    public WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getFluidSlotTheme();
    }

    public int getSlotHoverColor() {
        WidgetThemeEntry<SlotTheme> theme = getWidgetTheme(getContext().getTheme(), SlotTheme.class);
        return theme.getTheme().getSlotHoverColor();
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (!this.syncHandler.canFillSlot() && !this.syncHandler.canDrainSlot()) {
            return Result.ACCEPT;
        }
        ItemStack cursorStack = Platform.getClientPlayer().inventory.getItemStack();
        if (this.syncHandler.isPhantom() || cursorStack != null) {
            MouseData mouseData = MouseData.create(mouseButton);
            this.syncHandler.syncToServer(FluidSlotSyncHandler.SYNC_CLICK, mouseData::writeToPacket);
        }
        return Result.SUCCESS;
    }

    @Override
    public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
        if (this.syncHandler.isPhantom()) {
            if ((scrollDirection.isUp() && !this.syncHandler.canFillSlot()) || (scrollDirection.isDown() && !this.syncHandler.canDrainSlot())) {
                return false;
            }
            MouseData mouseData = MouseData.create(scrollDirection.modifier);
            this.syncHandler.syncToServer(FluidSlotSyncHandler.SYNC_SCROLL, mouseData::writeToPacket);
            return true;
        }
        return false;
    }

    @Override
    public @NotNull Result onKeyPressed(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_LSHIFT || keyCode == Keyboard.KEY_RSHIFT) {
            markTooltipDirty();
        }
        return Interactable.super.onKeyPressed(typedChar, keyCode);
    }

    @Override
    public boolean onKeyRelease(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_LSHIFT || keyCode == Keyboard.KEY_RSHIFT) {
            markTooltipDirty();
        }
        return Interactable.super.onKeyRelease(typedChar, keyCode);
    }

    @Override
    protected int getCapacity() {
        return this.alwaysShowFull ? 0 : getFluidTank().getCapacity();
    }

    @Nullable
    public FluidStack getFluidStack() {
        return this.syncHandler == null ? null : this.syncHandler.getValue();
    }

    public IFluidTank getFluidTank() {
        return this.syncHandler == null ? EMPTY : this.syncHandler.getFluidTank();
    }

    /**
     * Set the offset in x and y (on both sides) at which the fluid should be rendered.
     * Default is 1 for both.
     *
     * @param x x offset
     * @param y y offset
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    public FluidSlot contentOffset(int x, int y) {
        return contentPaddingLeft(x).contentPaddingTop(y);
    }

    /**
     * @param alwaysShowFull if the fluid should be rendered as full or as the partial amount.
     */
    public FluidSlot alwaysShowFull(boolean alwaysShowFull) {
        this.alwaysShowFull = alwaysShowFull;
        return this;
    }

    /**
     * @param overlayTexture texture that is rendered on top of the fluid
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    public FluidSlot overlayTexture(@Nullable IDrawable overlayTexture) {
        return overlay(overlayTexture);
    }

    public FluidSlot syncHandler(IFluidTank fluidTank) {
        return syncHandler(new FluidSlotSyncHandler(fluidTank));
    }

    public FluidSlot syncHandler(FluidSlotSyncHandler syncHandler) {
        setSyncOrValue(ISyncOrValue.orEmpty(syncHandler));
        return this;
    }

    /* === Recipe viewer ghost slot === */

    @Override
    public boolean handleDragAndDrop(@NotNull ItemStack draggedStack, int button) {
        if (!this.syncHandler.isPhantom()) return false;
        MouseData mouseData = MouseData.create(button);
        this.syncHandler.syncToServer(4, buffer -> {
            mouseData.writeToPacket(buffer);
            NetworkUtils.writeItemStack(buffer, draggedStack);
        });
        draggedStack.stackSize = 0;
        return true;
    }

    protected void setPhantomValue(@NotNull ItemStack draggedStack) {
        this.syncHandler.setValue(FluidContainerRegistry.getFluidForFilledItem(draggedStack));
    }
}
