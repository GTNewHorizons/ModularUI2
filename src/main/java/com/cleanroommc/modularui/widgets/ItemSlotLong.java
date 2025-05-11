package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.widget.IVanillaSlot;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.text.TextRenderer;
import com.cleanroommc.modularui.utils.item.IItemHandlerLong;
import com.cleanroommc.modularui.integration.nei.NEIDragAndDropHandler;
import com.cleanroommc.modularui.integration.nei.NEIIngredientProvider;
import com.cleanroommc.modularui.screen.ClientScreenHandler;
import com.cleanroommc.modularui.mixins.early.minecraft.GuiAccessor;
import com.cleanroommc.modularui.mixins.early.minecraft.GuiContainerAccessor;
import com.cleanroommc.modularui.mixins.early.minecraft.GuiScreenAccessor;
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
import com.cleanroommc.modularui.utils.item.IItemStackLong;
import com.cleanroommc.modularui.utils.item.ItemStackLong;
import com.cleanroommc.modularui.value.sync.ItemSlotLongSH;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.slot.ModularSlotLong;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// Changes made here probably should also be made to ItemSlot
public class ItemSlotLong extends Widget<ItemSlotLong> implements IVanillaSlot, Interactable, NEIDragAndDropHandler, NEIIngredientProvider {

    public static final int SIZE = 18;

    private static final TextRenderer textRenderer = new TextRenderer();
    private ItemSlotLongSH syncHandler;

    public ItemSlotLong() {
        tooltip().setAutoUpdate(true);//.setHasTitleMargin(true);
        tooltipBuilder(tooltip -> {
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
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        this.syncHandler = castIfTypeElseNull(syncHandler, ItemSlotLongSH.class);
        return this.syncHandler != null;
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
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        if (this.syncHandler == null) return;
        RenderHelper.enableGUIStandardItemLighting();
        drawSlot(getSlot());
        RenderHelper.disableStandardItemLighting();
        if (isHovering()) {
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.colorMask(true, true, true, false);
            GuiDraw.drawRect(1, 1, 16, 16, getSlotHoverColor());
            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.disableBlend();
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
    public WidgetSlotTheme getWidgetThemeInternal(ITheme theme) {
        return theme.getItemSlotTheme();
    }

    public int getSlotHoverColor() {
        WidgetTheme theme = getWidgetTheme(getContext().getTheme());
        if (theme instanceof WidgetSlotTheme slotTheme) {
            return slotTheme.getSlotHoverColor();
        }
        return ITheme.getDefault().getItemSlotTheme().getSlotHoverColor();
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (this.syncHandler.isPhantom()) {
            MouseData mouseData = MouseData.create(mouseButton);
            this.syncHandler.syncToServer(2, mouseData::writeToPacket);
        } else {
            ClientScreenHandler.clickSlot(getScreen(), getSlot());
            //getScreen().getScreenWrapper().clickSlot();
        }
        return Result.SUCCESS;
    }

    @Override
    public boolean onMouseRelease(int mouseButton) {
        if (!this.syncHandler.isPhantom()) {
            ClientScreenHandler.releaseSlot();
            //getScreen().getScreenWrapper().releaseSlot();
        }
        return true;
    }

    @Override
    public boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int amount) {
        if (this.syncHandler.isPhantom()) {
            MouseData mouseData = MouseData.create(scrollDirection.modifier);
            this.syncHandler.syncToServer(3, mouseData::writeToPacket);
            return true;
        }
        return false;
    }

    @Override
    public void onMouseDrag(int mouseButton, long timeSinceClick) {
        //getScreen().getScreenWrapper().dragSlot(timeSinceClick);
        ClientScreenHandler.dragSlot(timeSinceClick);
    }

    public ModularSlotLong getSlot() {
        return this.syncHandler.getSlot();
    }

    @Override
    public Slot getVanillaSlot() {
        return this.syncHandler.getSlot();
    }

    @Override
    public @NotNull ItemSlotLongSH getSyncHandler() {
        if (this.syncHandler == null) {
            throw new IllegalStateException("Widget is not initialised!");
        }
        return this.syncHandler;
    }

    public ItemSlotLong slot(ModularSlotLong slot) {
        this.syncHandler = new ItemSlotLongSH(slot);
        setSyncHandler(this.syncHandler);
        return this;
    }

    public ItemSlotLong slot(IItemHandlerLong itemHandler, int index) {
        return slot(new ModularSlotLong(itemHandler, index));
    }

    @SideOnly(Side.CLIENT)
    private void drawSlot(ModularSlotLong slotIn) {
        GuiScreen guiScreen = getScreen().getScreenWrapper().getGuiScreen();
        if (!(guiScreen instanceof GuiContainer))
            throw new IllegalStateException("The gui must be an instance of GuiContainer if it contains slots!");
        GuiContainerAccessor acc = (GuiContainerAccessor) guiScreen;
        RenderItem renderItem = GuiScreenAccessor.getItemRender();
        IItemStackLong itemstack = slotIn.getStackLong();
        boolean flag = false;
        boolean flag1 = slotIn == acc.getClickedSlot() && acc.getDraggedStack() != null && !acc.getIsRightMouseClick();
        ItemStack itemstack2 = guiScreen.mc.thePlayer.inventory.getItemStack();
        IItemStackLong itemstack1 = itemstack2 == null ? null : new ItemStackLong(itemstack2);
        long amount = -1;
        String format = null;

        if (slotIn == acc.getClickedSlot() && acc.getDraggedStack() != null && acc.getIsRightMouseClick() && itemstack != null) {
            itemstack = itemstack.copy();
            itemstack.setStackSize(itemstack.getStackSize() / 2);
        } else if (acc.getDragSplitting() && acc.getDragSplittingSlots().contains(slotIn) && itemstack1 != null) {
            if (acc.getDragSplittingSlots().size() == 1) {
                return;
            }

            // canAddItemToSlot
            if (Container.func_94527_a(slotIn, itemstack2, true) && getScreen().getContainer().canDragIntoSlot(slotIn)) {
                itemstack = itemstack1.copy();
                flag = true;
                // computeStackSize
                Container.func_94525_a(acc.getDragSplittingSlots(), acc.getDragSplittingLimit(), itemstack.getAsItemStack(), slotIn.getStack() == null ? 0 : slotIn.getStack().stackSize);
                long k = Math.min(itemstack.getMaxStackSize(), slotIn.getSlotStackLimit());

                if (itemstack.getStackSize() > k) {
                    amount = k;
                    format = EnumChatFormatting.YELLOW.toString();
                    itemstack.setStackSize(k);
                }
            } else {
                acc.getDragSplittingSlots().remove(slotIn);
                acc.invokeUpdateDragSplitting();
            }
        }

        ((GuiAccessor) guiScreen).setZLevel(100f);
        renderItem.zLevel = 100.0F;

        if (!flag1) {
            if (flag) {
                GuiDraw.drawRect(1, 1, 16, 16, -2130706433);
            }

            if (itemstack != null) {
                GlStateManager.enableRescaleNormal();
                RenderHelper.enableGUIStandardItemLighting();
                GlStateManager.enableDepth();
                // render the item itself
                renderItem.renderItemAndEffectIntoGUI(Minecraft.getMinecraft().fontRenderer, Minecraft.getMinecraft().getTextureManager(), itemstack.getAsItemStack(), 1, 1);
                GuiDraw.afterRenderItemAndEffectIntoGUI(itemstack.getAsItemStack());
                GlStateManager.disableRescaleNormal();
                if (amount < 0) {
                    amount = itemstack.getStackSize();
                }
                // render the amount overlay
                if (amount > 1 || format != null) {
                    String amountText = NumberFormat.format(amount, NumberFormat.AMOUNT_TEXT);
                    if (format != null) {
                        amountText = format + amountText;
                    }
                    float scale = 1f;
                    if (amountText.length() == 3) {
                        scale = 0.8f;
                    } else if (amountText.length() == 4) {
                        scale = 0.6f;
                    } else if (amountText.length() > 4) {
                        scale = 0.5f;
                    }
                    textRenderer.setShadow(true);
                    textRenderer.setScale(scale);
                    textRenderer.setColor(Color.WHITE.main);
                    textRenderer.setAlignment(Alignment.BottomRight, getArea().width - 1, getArea().height - 1);
                    textRenderer.setPos(1, 1);
                    GlStateManager.disableLighting();
                    GlStateManager.disableDepth();
                    GlStateManager.disableBlend();
                    textRenderer.draw(amountText);
                    GlStateManager.enableLighting();
                    GlStateManager.enableDepth();
                    GlStateManager.enableBlend();
                }

                long cachedCount = itemstack.getStackSize();
                itemstack.setStackSize(1); // required to not render the amount overlay
                // render other overlays like durability bar
                renderItem.renderItemOverlayIntoGUI(((GuiScreenAccessor) guiScreen).getFontRenderer(), Minecraft.getMinecraft().getTextureManager(), itemstack.getAsItemStack(), 1, 1, null);
                itemstack.setStackSize(cachedCount);
                GlStateManager.disableDepth();
            }
        }

        ((GuiAccessor) guiScreen).setZLevel(0.0F);
        renderItem.zLevel = 0f;
    }

    @Override
    public boolean handleDragAndDrop(@NotNull ItemStack draggedStack, int button) {
        if (!this.syncHandler.isPhantom()) return false;
        this.syncHandler.updateFromClient(draggedStack, button);
        draggedStack.stackSize = 0;
        return true;
    }

    @Override
    public @Nullable ItemStack getStackForNEI() {
        return this.syncHandler.getSlot().getStack();
    }
}
