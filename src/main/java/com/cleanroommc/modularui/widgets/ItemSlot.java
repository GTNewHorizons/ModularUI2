package com.cleanroommc.modularui.widgets;

import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.guihook.IContainerTooltipHandler;

import com.cleanroommc.modularui.api.IItemStackLong;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.widget.IVanillaSlot;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.TextRenderer;
import com.cleanroommc.modularui.future.IItemHandlerModifiable;
import com.cleanroommc.modularui.integration.nei.NEIDragAndDropHandler;
import com.cleanroommc.modularui.integration.nei.NEIIngredientProvider;
import com.cleanroommc.modularui.mixins.early.minecraft.GuiContainerAccessor;
import com.cleanroommc.modularui.screen.GuiScreenWrapper;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetSlotTheme;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.utils.NumberFormat;
import com.cleanroommc.modularui.utils.item.ItemStackLongDelegate;
import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.List;

import static com.cleanroommc.modularui.ModularUI.isNEILoaded;
import static com.google.common.primitives.Ints.saturatedCast;

public class ItemSlot extends Widget<ItemSlot> implements IVanillaSlot, Interactable, NEIDragAndDropHandler, NEIIngredientProvider {

    public static final int SIZE = 18;

    private static final TextRenderer textRenderer = new TextRenderer();
    private ItemSlotSH syncHandler;

    public ItemSlot() {
        tooltip().setAutoUpdate(true).setHasTitleMargin(true);
        tooltipBuilder(tooltip -> {
            tooltip.excludeArea(getArea());
            if (!isSynced()) return;
            ItemStack stack = getSlot().getStack();
            if (stack == null) return;
            tooltip.addStringLines(getItemTooltip(stack));
        });
    }

    @Override
    public void onInit() {
        size(SIZE, SIZE);
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        if (syncHandler instanceof ItemSlotSH itemSlotSH) {
            this.syncHandler = itemSlotSH;
            return true;
        }
        return false;
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
    public void draw(GuiContext context, WidgetTheme widgetTheme) {
        if (this.syncHandler == null) return;
        RenderHelper.enableGUIStandardItemLighting();
        drawSlot(getSlot());
        RenderHelper.disableStandardItemLighting();
        if (isHovering()) {
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glColorMask(true, true, true, false);
            GuiDraw.drawRect(1, 1, 16, 16, getWidgetTheme(context.getTheme()).getSlotHoverColor());
            GL11.glColorMask(true, true, true, true);
            GL11.glDisable(GL11.GL_BLEND);
        }
    }

    @Override
    public WidgetSlotTheme getWidgetTheme(ITheme theme) {
        return theme.getItemSlotTheme();
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (this.syncHandler.isPhantom()) {
            MouseData mouseData = MouseData.create(mouseButton);
            this.syncHandler.syncToServer(2, mouseData::writeToPacket);
        } else {
            getScreen().getScreenWrapper().clickSlot();
        }
        return Result.SUCCESS;
    }

    @Override
    public boolean onMouseRelease(int mouseButton) {
        if (!this.syncHandler.isPhantom()) {
            getScreen().getScreenWrapper().releaseSlot();
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
        getScreen().getScreenWrapper().dragSlot(timeSinceClick);
    }

    public ModularSlot getSlot() {
        return this.syncHandler.getSlot();
    }

    @Override
    public Slot getVanillaSlot() {
        return this.syncHandler.getSlot();
    }

    @Override
    public @NotNull ItemSlotSH getSyncHandler() {
        if (this.syncHandler == null) {
            throw new IllegalStateException("Widget is not initialised!");
        }
        return this.syncHandler;
    }

    protected List<String> getItemTooltip(ItemStack stack) {
        if (!isNEILoaded) {
            return stack.getTooltip(
                    Minecraft.getMinecraft().thePlayer,
                    Minecraft.getMinecraft().gameSettings.advancedItemTooltips);
        }

        List<String> tooltips = GuiContainerManager.itemDisplayNameMultiline(stack, getScreen().getScreenWrapper(), true);

        GuiContainerManager.applyItemCountDetails(tooltips, stack);

        if (GuiContainerManager.getManager() != null && GuiContainerManager.shouldShowTooltip(getScreen().getScreenWrapper())) {
            for (IContainerTooltipHandler handler : GuiContainerManager.getManager().instanceTooltipHandlers)
                tooltips = handler.handleItemTooltip(getScreen().getScreenWrapper(), stack, getContext().getMouseX(), getContext().getMouseY(), tooltips);
        }

        return tooltips;
    }

    public ItemSlot slot(ModularSlot slot) {
        this.syncHandler = new ItemSlotSH(slot);
        setSyncHandler(this.syncHandler);
        return this;
    }

    public ItemSlot slot(IItemHandlerModifiable itemHandler, int index) {
        return slot(new ModularSlot(itemHandler, index));
    }

    @SideOnly(Side.CLIENT)
    private void drawSlot(ModularSlot slotIn) {
        GuiScreenWrapper guiScreen = getScreen().getScreenWrapper();
        GuiContainerAccessor accessor = guiScreen.getAccessor();
        IItemStackLong itemstack = slotIn.getStackLong();
        boolean flag = false;
        boolean flag1 = slotIn == accessor.getClickedSlot() && accessor.getDraggedStack() != null && !accessor.getIsRightMouseClick();
        ItemStack itemstack1 = guiScreen.mc.thePlayer.inventory.getItemStack();
        long amount = -1;
        String format = null;

        if (slotIn == accessor.getClickedSlot() && accessor.getDraggedStack() != null && accessor.getIsRightMouseClick() && itemstack != null) {
            itemstack = itemstack.copy();
            itemstack.setStackSize(itemstack.getStackSize()/2);
        } else if (guiScreen.isDragSplitting() && guiScreen.getDragSlots().contains(slotIn) && itemstack1 != null) {
            if (guiScreen.getDragSlots().size() == 1) {
                return;
            }

            // canAddItemToSlot
            if (Container.func_94527_a(slotIn, itemstack1, true) && guiScreen.inventorySlots.canDragIntoSlot(slotIn)) {
                itemstack = new ItemStackLongDelegate(itemstack1.copy());
                flag = true;
                // computeStackSize
                Container.func_94525_a(guiScreen.getDragSlots(), accessor.getDragSplittingLimit(), itemstack.getAsItemStack(), slotIn.getStackLong() == null ? 0 : saturatedCast(slotIn.getStackLong().getStackSize()));
                long k = Math.min(itemstack.getMaxStackSize(), slotIn.getSlotStackLimitLong());

                if (itemstack.getStackSize() > k) {
                    amount = k;
                    format = EnumChatFormatting.YELLOW.toString();
                    itemstack.setStackSize(k);
                }
            } else {
                guiScreen.getDragSlots().remove(slotIn);
                accessor.invokeUpdateDragSplitting();
            }
        }

        guiScreen.setZ(100f);
        GuiScreenWrapper.getItemRenderer().zLevel = 100.0F;

        if (!flag1) {
            if (flag) {
                GuiDraw.drawRect(1, 1, 16, 16, -2130706433);
            }

            if (itemstack != null && itemstack.getAsItemStack() != null) {
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                GL11.glEnable(GL11.GL_LIGHTING);
                RenderHelper.enableGUIStandardItemLighting();
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                // render the item itself
                GuiScreenWrapper.getItemRenderer().renderItemAndEffectIntoGUI(Minecraft.getMinecraft().fontRenderer, Minecraft.getMinecraft().getTextureManager(), itemstack.getAsItemStack(), 1, 1);
                GuiDraw.afterRenderItemAndEffectIntoGUI(itemstack.getAsItemStack());
                GL11.glDisable(GL12.GL_RESCALE_NORMAL);
                if (amount < 0) {
                    amount = itemstack.getStackSize();
                }
                // render the amount overlay
                if (amount > 1 || format != null) {
                    String amountText = NumberFormat.formatWithMaxDigits(amount);
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
                    GL11.glDisable(GL11.GL_LIGHTING);
                    GL11.glDisable(GL11.GL_DEPTH_TEST);
                    GL11.glDisable(GL11.GL_BLEND);
                    textRenderer.draw(amountText);
                    GL11.glEnable(GL11.GL_LIGHTING);
                    GL11.glEnable(GL11.GL_DEPTH_TEST);
                    GL11.glEnable(GL11.GL_BLEND);
                }

                long cachedCount = itemstack.getStackSize();
                itemstack.setStackSize(1); // required to not render the amount overlay
                // render other overlays like durability bar
                GuiScreenWrapper.getItemRenderer().renderItemOverlayIntoGUI(guiScreen.getFontRenderer(), Minecraft.getMinecraft().getTextureManager(), itemstack.getAsItemStack(), 1, 1, null);
                itemstack.setStackSize(cachedCount);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
            }
        }

        GuiScreenWrapper.getItemRenderer().zLevel = 0.0F;
        guiScreen.setZ(0f);
    }

    @Override
    public boolean handleDragAndDrop(@NotNull ItemStack draggedStack, int button) {
        if (!this.syncHandler.isPhantom()) return false;
        this.syncHandler.updateFromClient(new ItemStackLongDelegate(draggedStack));
        draggedStack.stackSize = 0;
        return true;
    }

    @Override
    public @Nullable ItemStack getStackForNEI() {
        return this.syncHandler.getSlot().getStack();
    }
}