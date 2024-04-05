package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.TextRenderer;
import com.cleanroommc.modularui.integration.nei.NEIDragAndDropHandler;
import com.cleanroommc.modularui.integration.nei.NEIIngredientProvider;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.Tooltip;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetSlotTheme;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.utils.NumberFormat;
import com.cleanroommc.modularui.value.sync.FluidSlotSyncHandler;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.value.sync.ValueSyncHandler;
import com.cleanroommc.modularui.widget.Widget;
import gregtech.api.util.GT_Utility;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import static com.cleanroommc.modularui.ModularUI.isGT5ULoaded;

public class FluidSlot<W extends FluidSlot<W>> extends Widget<W> implements Interactable, NEIDragAndDropHandler, NEIIngredientProvider {

    public static final int DEFAULT_SIZE = 18;

    private static final String UNIT_BUCKET = "B";
    private static final String UNIT_LITER = "L";

    private static final IFluidTank EMPTY = new FluidTank(0);

    private final TextRenderer textRenderer = new TextRenderer();
    private FluidSlotSyncHandler syncHandler;
    private int contentOffsetX = 1, contentOffsetY = 1;
    private boolean alwaysShowFull = true;
    @Nullable
    private IDrawable overlayTexture = null;

    public FluidSlot() {
        flex().startDefaultMode()
                .size(DEFAULT_SIZE, DEFAULT_SIZE)
                .endDefaultMode();
        tooltip().setAutoUpdate(true).setHasTitleMargin(true);
        tooltipBuilder(this::addToolTip);
    }

    protected void addToolTip(Tooltip tooltip) {
        IFluidTank fluidTank = getFluidTank();
        FluidStack fluid = this.syncHandler.getValue();
        if (this.syncHandler.isPhantom()) {
            if (fluid != null) {
                tooltip.addLine(IKey.str(fluid.getLocalizedName()));
                if (this.syncHandler.controlsAmount()) {
                    tooltip.addLine(IKey.lang("modularui.fluid.phantom.amount", formatFluidAmount(fluid.amount), getBaseUnit()));
                }
            } else {
                tooltip.addLine(IKey.lang("modularui.fluid.empty"));
            }
            if (this.syncHandler.controlsAmount()) {
                tooltip.addLine(IKey.lang("modularui.fluid.phantom.control"));
            }
        } else {
            if (fluid != null) {
                tooltip.addLine(IKey.str(fluid.getLocalizedName()));
                tooltip.addLine(IKey.lang("modularui.fluid.amount", formatFluidAmount(fluid.amount), formatFluidAmount(fluidTank.getCapacity()), getBaseUnit()));
                addAdditionalFluidInfo(tooltip, fluid);
            } else {
                tooltip.addLine(IKey.lang("modularui.fluid.empty"));
            }
            if (this.syncHandler.canFillSlot() || this.syncHandler.canDrainSlot()) {
                tooltip.addLine(IKey.EMPTY); // Add an empty line to separate from the bottom material tooltips
                if (Interactable.hasShiftDown()) {
                    if (this.syncHandler.canFillSlot() && this.syncHandler.canDrainSlot()) {
                        tooltip.addLine(IKey.lang("modularui.fluid.click_combined"));
                    } else if (this.syncHandler.canDrainSlot()) {
                        tooltip.addLine(IKey.lang("modularui.fluid.click_to_fill"));
                    } else if (this.syncHandler.canFillSlot()) {
                        tooltip.addLine(IKey.lang("modularui.fluid.click_to_empty"));
                    }
                } else {
                    tooltip.addLine(IKey.lang("modularui.tooltip.shift"));
                }
            }
        }
    }

    public void addAdditionalFluidInfo(Tooltip tooltip, FluidStack fluidStack) {
    }

    public String formatFluidAmount(double amount) {
        NumberFormat.FORMAT.setMaximumFractionDigits(3);
        return NumberFormat.FORMAT.format(getBaseUnitAmount(amount));
    }

    protected double getBaseUnitAmount(double amount) {
        return amount;
    }

    protected String getBaseUnit() {
        return UNIT_LITER;
    }

    @Override
    public void onInit() {
        textRenderer.setShadow(true);
        textRenderer.setScale(0.5f);
        this.textRenderer.setColor(Color.WHITE.main);
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        if (syncHandler instanceof FluidSlotSyncHandler fluidSlotSyncHandler) {
            this.syncHandler = fluidSlotSyncHandler;
            return true;
        }
        return false;
    }

    @Override
    public void draw(GuiContext context, WidgetTheme widgetTheme) {
        IFluidTank fluidTank = getFluidTank();
        FluidStack content = getFluidStack();
        if (content != null) {
            int y = getContentOffsetY();
            float height = getArea().height - y * 2;
            if (!this.alwaysShowFull) {
                float newHeight = height * content.amount * 1f / fluidTank.getCapacity();
                y += (int) (height - newHeight);
                height = newHeight;
            }
            GuiDraw.drawFluidTexture(content, getContentOffsetX(), y, getArea().width - getContentOffsetX() * 2, height, 0);
        }
        if (this.overlayTexture != null) {
            this.overlayTexture.drawAtZero(context, getArea(), widgetTheme);
        }
        if (content != null && this.syncHandler.controlsAmount()) {
            String s = NumberFormat.formatWithMaxDigits(getBaseUnitAmount(content.amount)) + getBaseUnit();
            this.textRenderer.setAlignment(Alignment.CenterRight, getArea().width - getContentOffsetX() - 1f);
            this.textRenderer.setPos((int) (getContentOffsetX() + 0.5f), (int) (getArea().height - 5.5f));
            this.textRenderer.draw(s);
        }
        if (isHovering()) {
            GL11.glColorMask(true, true, true, false);
            GuiDraw.drawRect(1, 1, getArea().w() - 2, getArea().h() - 2, getWidgetTheme(context.getTheme()).getSlotHoverColor());
            GL11.glColorMask(true, true, true, true);
        }
    }

    protected int getContentOffsetX() {
        return contentOffsetX;
    }

    protected int getContentOffsetY() {
        return contentOffsetY;
    }

    protected boolean showFull() {
        return alwaysShowFull;
    }

    protected TextRenderer getTextRenderer() {
        return textRenderer;
    }

    protected IDrawable getOverlayTexture() {
        return overlayTexture;
    }

    @Override
    public WidgetSlotTheme getWidgetTheme(ITheme theme) {
        return theme.getFluidSlotTheme();
    }

    @NotNull
    @Override
    public Result onMouseTapped(int mouseButton) {
        if (!this.syncHandler.canFillSlot() && !this.syncHandler.canDrainSlot()) {
            return Result.IGNORE;
        }
        ItemStack cursorStack = Minecraft.getMinecraft().thePlayer.inventory.getItemStack();
        if (this.syncHandler.isPhantom() || cursorStack != null) {
            MouseData mouseData = MouseData.create(mouseButton);
            this.syncHandler.syncToServer(1, mouseData::writeToPacket);
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
            this.syncHandler.syncToServer(2, mouseData::writeToPacket);
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
    public W contentOffset(int x, int y) {
        this.contentOffsetX = x;
        this.contentOffsetY = y;
        return getThis();
    }

    /**
     * @param alwaysShowFull if the fluid should be rendered as full or as the partial amount.
     */
    public W alwaysShowFull(boolean alwaysShowFull) {
        this.alwaysShowFull = alwaysShowFull;
        return getThis();
    }

    /**
     * @param overlayTexture texture that is rendered on top of the fluid
     */
    public W overlayTexture(@Nullable IDrawable overlayTexture) {
        this.overlayTexture = overlayTexture;
        return getThis();
    }

    public W syncHandler(IFluidTank fluidTank) {
        return syncHandler(new FluidSlotSyncHandler(fluidTank));
    }

    public W syncHandler(FluidSlotSyncHandler syncHandler) {
        setSyncHandler(syncHandler);
        this.syncHandler = syncHandler;
        return getThis();
    }

    @Override
    public boolean handleDragAndDrop(@NotNull ItemStack draggedStack, int button) {
        if (!this.syncHandler.isPhantom()) return false;
        draggedStack.stackSize = 0;
        return true;
    }

    protected void setPhantomValue(@NotNull ItemStack draggedStack) {
        this.syncHandler.setValue(FluidContainerRegistry.getFluidForFilledItem(draggedStack));
    }

    @Override
    public @Nullable ItemStack getStackForNEI() {
        if (isGT5ULoaded) {
            return GT_Utility.getFluidDisplayStack(getFluidStack(), false);
        }
        return null;
    }
}