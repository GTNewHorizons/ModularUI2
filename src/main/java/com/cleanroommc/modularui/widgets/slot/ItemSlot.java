package com.cleanroommc.modularui.widgets.slot;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.value.ISyncOrValue;
import com.cleanroommc.modularui.api.widget.IVanillaSlot;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.core.mixins.early.minecraft.GuiAccessor;
import com.cleanroommc.modularui.core.mixins.early.minecraft.GuiContainerAccessor;
import com.cleanroommc.modularui.core.mixins.early.minecraft.GuiScreenAccessor;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerIngredientProvider;
import com.cleanroommc.modularui.screen.ClientScreenHandler;
import com.cleanroommc.modularui.screen.NEAAnimationHandler;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.SlotTheme;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.GlStateManager;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.utils.item.IItemHandlerModifiable;
import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.neverenoughanimations.NEAConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemSlot extends Widget<ItemSlot> implements IVanillaSlot, Interactable, RecipeViewerIngredientProvider {

    public static final int SIZE = 18;

    public static ItemSlot create(boolean phantom) {
        return phantom ? new PhantomItemSlot() : new ItemSlot();
    }

    private ItemSlotSH syncHandler;
    private RichTooltip tooltip;

    public ItemSlot() {
        itemTooltip().setAutoUpdate(true);//.setHasTitleMargin(true);
        itemTooltip().tooltipBuilder(tooltip -> {
            if (!isSynced()) return;
            ItemStack stack = getSlot().getStack();
            buildTooltip(stack, tooltip);
        });
    }

    @Override
    public void onInit() {
        if (getScreen().isOverlay()) {
            throw new IllegalStateException("Overlays can't have slots!");
        }
        size(SIZE);
    }

    @Override
    public boolean isValidSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        // disallow null
        return syncOrValue instanceof ItemSlotSH;
    }

    @Override
    protected void setSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        super.setSyncOrValue(syncOrValue);
        this.syncHandler = syncOrValue.castOrThrow(ItemSlotSH.class);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        boolean shouldBeEnabled = areAncestorsEnabled();
        if (shouldBeEnabled != getSlot().func_111238_b()) {
            this.syncHandler.setEnabled(shouldBeEnabled, true);
        }
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        if (this.syncHandler == null) return;
        RenderHelper.enableGUIStandardItemLighting();
        drawSlot(getSlot());
        RenderHelper.disableStandardItemLighting();
        drawOverlay();
    }

    protected void drawOverlay() {
        if (isHovering() && (!ModularUI.Mods.NEA.isLoaded() || NEAConfig.itemHoverOverlay)) {
            GuiDraw.drawRect(1, 1, 16, 16, getSlotHoverColor());
        }
    }

    @Override
    public void drawForeground(ModularGuiContext context) {
        RichTooltip tooltip = getTooltip();
        if (tooltip != null && isHoveringFor(tooltip.getShowUpTimer())) {
            tooltip.draw(getContext(), getSlot().getStack());
        }
    }

    public void buildTooltip(ItemStack stack, RichTooltip tooltip) {
        if (stack == null) return;
        tooltip.addFromItem(stack);
    }

    @Override
    public WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        PlayerSlotType playerSlotType = this.syncHandler != null ? this.syncHandler.getPlayerSlotType() : null;
        if (playerSlotType == null) return theme.getWidgetTheme(IThemeApi.ITEM_SLOT);
        return switch (playerSlotType) {
            case HOTBAR -> theme.getWidgetTheme(IThemeApi.ITEM_SLOT_PLAYER_HOTBAR);
            case MAIN_INVENTORY -> theme.getWidgetTheme(IThemeApi.ITEM_SLOT_PLAYER_MAIN_INV);
            case ARMOR -> theme.getWidgetTheme(IThemeApi.ITEM_SLOT_PLAYER_ARMOR);
        };
    }

    public int getSlotHoverColor() {
        WidgetThemeEntry<SlotTheme> theme = getWidgetTheme(getContext().getTheme(), SlotTheme.class);
        return theme.getTheme().getSlotHoverColor();
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        ClientScreenHandler.clickSlot(getScreen(), getSlot());
        return Result.SUCCESS;
    }

    @Override
    public boolean onMouseRelease(int mouseButton) {
        ClientScreenHandler.releaseSlot();
        return true;
    }

    @Override
    public void onMouseDrag(int mouseButton, long timeSinceClick) {
        ClientScreenHandler.dragSlot(timeSinceClick);
    }

    public ModularSlot getSlot() {
        return this.syncHandler.getSlot();
    }

    @Override
    public Slot getVanillaSlot() {
        return this.syncHandler.getSlot();
    }

    @Override
    public boolean handleAsVanillaSlot() {
        return true;
    }

    @Override
    public @NotNull ItemSlotSH getSyncHandler() {
        if (this.syncHandler == null) {
            throw new IllegalStateException("Widget is not initialised!");
        }
        return this.syncHandler;
    }

    public RichTooltip getItemTooltip() {
        return super.getTooltip();
    }

    public RichTooltip itemTooltip() {
        return super.tooltip();
    }

    @Override
    public @Nullable RichTooltip getTooltip() {
        if (isSynced() && getSlot().getStack() != null) {
            return getItemTooltip();
        }
        return tooltip;
    }

    @Override
    public ItemSlot tooltip(RichTooltip tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    @Override
    public @NotNull RichTooltip tooltip() {
        if (this.tooltip == null) {
            this.tooltip = new RichTooltip().parent(this);
        }
        return this.tooltip;
    }

    public ItemSlot slot(ModularSlot slot) {
        return syncHandler(new ItemSlotSH(slot));
    }

    public ItemSlot slot(IItemHandlerModifiable itemHandler, int index) {
        return slot(new ModularSlot(itemHandler, index));
    }

    public ItemSlot syncHandler(ItemSlotSH syncHandler) {
        setSyncOrValue(ISyncOrValue.orEmpty(syncHandler));
        return this;
    }

    @SideOnly(Side.CLIENT)
    private void drawSlot(ModularSlot slotIn) {
        GuiScreen guiScreen = getScreen().getScreenWrapper().getGuiScreen();
        if (!(guiScreen instanceof GuiContainer guiContainer))
            throw new IllegalStateException("The gui must be an instance of GuiContainer if it contains slots!");
        GuiContainerAccessor acc = (GuiContainerAccessor) guiScreen;
        RenderItem renderItem = GuiScreenAccessor.getItemRender();
        ItemStack itemstack = slotIn.getStack();
        boolean isDragPreview = false;
        boolean flag1 = slotIn == acc.getClickedSlot() && acc.getDraggedStack() != null && !acc.getIsRightMouseClick();
        ItemStack itemstack1 = guiScreen.mc.thePlayer.inventory.getItemStack();
        int amount = -1;
        String format = null;

        if (!getSyncHandler().isPhantom()) {
            if (slotIn == acc.getClickedSlot() && acc.getDraggedStack() != null && acc.getIsRightMouseClick() && itemstack != null) {
                itemstack = itemstack.copy();
                itemstack.stackSize /= 2;
            } else if (acc.getDragSplitting() && acc.getDragSplittingSlots().contains(slotIn) && itemstack1 != null) {
                if (acc.getDragSplittingSlots().size() == 1) {
                    return;
                }

                // canAddItemToSlot
                if (Container.func_94527_a(slotIn, itemstack1, true) && getScreen().getContainer().canDragIntoSlot(slotIn)) {
                    itemstack = itemstack1.copy();
                    isDragPreview = true;
                    // computeStackSize
                    Container.func_94525_a(acc.getDragSplittingSlots(), acc.getDragSplittingLimit(), itemstack, slotIn.getStack() == null ? 0 : slotIn.getStack().stackSize);
                    int k = Math.min(itemstack.getMaxStackSize(), slotIn.getSlotStackLimit());

                    if (itemstack.stackSize > k) {
                        amount = k;
                        format = EnumChatFormatting.YELLOW.toString();
                        itemstack.stackSize = k;
                    }
                } else {
                    acc.getDragSplittingSlots().remove(slotIn);
                    acc.invokeUpdateDragSplitting();
                }
            }
        }

        // makes sure items of different layers don't interfere with each other visually
        float z = getContext().getCurrentDrawingZ() + 100;
        ((GuiAccessor) guiScreen).setZLevel(z);
        renderItem.zLevel = z;

        if (!doDrawItem) {
            if (isDragPreview) {
                GuiDraw.drawRect(1, 1, 16, 16, 0x80FFFFFF);
            }

            itemstack = NEAAnimationHandler.injectVirtualStack(itemstack, guiContainer, slotIn);

            if (itemstack != null) {
                Platform.setupDrawItem();
                float itemScale = NEAAnimationHandler.injectHoverScale(guiContainer, slotIn);
                // render the item itself
                renderItem.renderItemAndEffectIntoGUI(Minecraft.getMinecraft().fontRenderer, Minecraft.getMinecraft().getTextureManager(), itemstack, 1, 1);
                GuiDraw.afterRenderItemAndEffectIntoGUI(itemstack);
                GlStateManager.disableRescaleNormal();
                Platform.endDrawItem();

                if (amount < 0) {
                    amount = itemstack.stackSize;
                }
                GuiDraw.drawStandardSlotAmountText(amount, format, getArea());

                int cachedCount = itemstack.stackSize;
                itemstack.stackSize = 1; // required to not render the amount overlay
                // render other overlays like durability bar
                renderItem.renderItemOverlayIntoGUI(((GuiScreenAccessor) guiScreen).getFontRenderer(), Minecraft.getMinecraft().getTextureManager(), itemstack, 1, 1, null);
                NEAAnimationHandler.endHoverScale();
                itemstack.stackSize = cachedCount;
                GlStateManager.disableDepth();
                GlStateManager.disableLighting();
            }
        }

        ((GuiAccessor) guiScreen).setZLevel(0f);
        renderItem.zLevel = 0f;
    }

    @Override
    public @Nullable ItemStack getStackForRecipeViewer() {
        return this.syncHandler.getSlot().getStack();
    }
}
