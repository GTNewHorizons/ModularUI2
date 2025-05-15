package com.cleanroommc.modularui.widgets.slot;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.text.TextRenderer;
import com.cleanroommc.modularui.integration.nei.NEIDragAndDropHandler;
import com.cleanroommc.modularui.integration.nei.NEIIngredientProvider;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetSlotTheme;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.GlStateManager;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.utils.NumberFormat;
import com.cleanroommc.modularui.value.sync.FluidSlotSyncHandler;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;

import gregtech.api.util.GTUtility;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

import java.text.DecimalFormat;

import static com.cleanroommc.modularui.ModularUI.isGT5ULoaded;

public class FluidSlot extends Widget<FluidSlot> implements Interactable, NEIDragAndDropHandler, NEIIngredientProvider {

    public static final int DEFAULT_SIZE = 18;
    public static final String UNIT_BUCKET = "B";
    public static final String UNIT_LITER = "L";
    private static final DecimalFormat TOOLTIP_FORMAT = new DecimalFormat("#.##");
    private static final IFluidTank EMPTY = new FluidTank(0);

    static {
        TOOLTIP_FORMAT.setGroupingUsed(true);
        TOOLTIP_FORMAT.setGroupingSize(3);
    }

    private final TextRenderer textRenderer = new TextRenderer();
    private FluidSlotSyncHandler syncHandler;
    private int contentOffsetX = 1, contentOffsetY = 1;
    private boolean alwaysShowFull = true;
    @Nullable
    private IDrawable overlayTexture = null;

    public FluidSlot() {
        size(DEFAULT_SIZE);
        tooltip().setAutoUpdate(true);//.setHasTitleMargin(true);
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
        return TOOLTIP_FORMAT.format(amount);// + " " + getBaseUnitBaseSuffix();
    }

    protected double getBaseUnitAmount(double amount) {
        return amount;
    }

    protected String getBaseUnit() {
        return UNIT_LITER;
    }

    protected String getBaseUnitBaseSuffix() {
        return "m";
    }

    @Override
    public void onInit() {
        textRenderer.setShadow(true);
        textRenderer.setScale(0.5f);
        this.textRenderer.setColor(Color.WHITE.main);
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        this.syncHandler = castIfTypeElseNull(syncHandler, FluidSlotSyncHandler.class);
        return this.syncHandler != null;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        IFluidTank fluidTank = getFluidTank();
        FluidStack content = getFluidStack();
        if (content != null) {
            float y = this.contentOffsetY;
            float height = getArea().height - y * 2;
            if (!this.alwaysShowFull) {
                float newHeight = height * content.amount * 1f / fluidTank.getCapacity();
                y += height - newHeight;
                height = newHeight;
            }
            GuiDraw.drawFluidTexture(content, this.contentOffsetX, y, getArea().width - this.contentOffsetX * 2, height, 0);
        }
        if (this.overlayTexture != null) {
            this.overlayTexture.drawAtZero(context, getArea(), widgetTheme);
        }
        if (content != null && this.syncHandler.controlsAmount()) {
            String s = NumberFormat.format(getBaseUnitAmount(content.amount), NumberFormat.AMOUNT_TEXT) + getBaseUnit();
            this.textRenderer.setAlignment(Alignment.CenterRight, getArea().width - this.contentOffsetX - 1f);
            this.textRenderer.setPos((int) (this.contentOffsetX + 0.5f), (int) (getArea().height - 5.5f));
            this.textRenderer.draw(s);
        }
        if (isHovering()) {
            GlStateManager.colorMask(true, true, true, false);
            GuiDraw.drawRect(1, 1, getArea().w() - 2, getArea().h() - 2, getSlotHoverColor());
            GlStateManager.colorMask(true, true, true, true);
        }
    }

    @Override
    public WidgetSlotTheme getWidgetThemeInternal(ITheme theme) {
        return theme.getFluidSlotTheme();
    }

    public int getSlotHoverColor() {
        WidgetTheme theme = getWidgetTheme(getContext().getTheme());
        if (theme instanceof WidgetSlotTheme slotTheme) {
            return slotTheme.getSlotHoverColor();
        }
        return ITheme.getDefault().getFluidSlotTheme().getSlotHoverColor();
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (!this.syncHandler.canFillSlot() && !this.syncHandler.canDrainSlot()) {
            return Result.ACCEPT;
        }
        ItemStack cursorStack = Minecraft.getMinecraft().thePlayer.inventory.getItemStack();
        if (this.syncHandler.isPhantom() || cursorStack != null) {
            MouseData mouseData = MouseData.create(mouseButton);
            this.syncHandler.syncToServer(FluidSlotSyncHandler.SYNC_CLICK, mouseData::writeToPacket);
        }
        return Result.SUCCESS;
    }

    @Override
    public boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int amount) {
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
    public FluidSlot contentOffset(int x, int y) {
        this.contentOffsetX = x;
        this.contentOffsetY = y;
        return this;
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
    public FluidSlot overlayTexture(@Nullable IDrawable overlayTexture) {
        this.overlayTexture = overlayTexture;
        return this;
    }

    public FluidSlot syncHandler(IFluidTank fluidTank) {
        return syncHandler(new FluidSlotSyncHandler(fluidTank));
    }

    public FluidSlot syncHandler(FluidSlotSyncHandler syncHandler) {
        setSyncHandler(syncHandler);
        this.syncHandler = syncHandler;
        return this;
    }

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

    @Override
    public @Nullable ItemStack getStackForNEI() {
        if (isGT5ULoaded) {
            return GTUtility.getFluidDisplayStack(getFluidStack(), false);
        }
        return null;
    }
}
