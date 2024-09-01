package com.cleanroommc.modularui.widgets;

import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.guihook.IContainerTooltipHandler;

import com.cleanroommc.modularui.screen.ClientScreenHandler;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.widget.IVanillaSlot;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.TextRenderer;
import com.cleanroommc.modularui.utils.item.IItemHandlerModifiable;
import com.cleanroommc.modularui.integration.nei.NEIDragAndDropHandler;
import com.cleanroommc.modularui.integration.nei.NEIIngredientProvider;
import com.cleanroommc.modularui.mixins.early.minecraft.GuiAccessor;
import com.cleanroommc.modularui.mixins.early.minecraft.GuiContainerAccessor;
import com.cleanroommc.modularui.mixins.early.minecraft.GuiScreenAccessor;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.Tooltip;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetSlotTheme;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.utils.NumberFormat;
import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
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
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.List;

import static com.cleanroommc.modularui.ModularUI.isNEILoaded;

// Changes made here probably should also be made to ItemSlotLong
public class ItemSlot extends Widget<ItemSlot> implements IVanillaSlot, Interactable, NEIDragAndDropHandler, NEIIngredientProvider {

    public static final int SIZE = 18;

    private static final TextRenderer textRenderer = new TextRenderer();
    private ItemSlotSH syncHandler;

    public ItemSlot() {
        tooltip().setAutoUpdate(true).setHasTitleMargin(true);
        tooltipBuilder(this::addToolTip);
    }

    protected void addToolTip(Tooltip tooltip) {
        if (!isSynced()) return;
        ItemStack stack = getSlot().getStack();
        if (stack == null) return;
        tooltip.addStringLines(getItemTooltip(stack));
    }

    @Override
    public void onInit() {
        if (getScreen().isOverlay()) {
            throw new IllegalStateException("Overlays can't have slots!");
        }
        size(SIZE, SIZE);
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        this.syncHandler = castIfTypeElseNull(syncHandler, ItemSlotSH.class);
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
            ClientScreenHandler.clickSlot();
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

    @SideOnly(Side.CLIENT)
    protected List<String> getItemTooltip(ItemStack stack) {
        if (!isNEILoaded || !(getScreen().getScreenWrapper() instanceof GuiContainer guiContainer)) {
            return stack.getTooltip(
                    Minecraft.getMinecraft().thePlayer,
                    Minecraft.getMinecraft().gameSettings.advancedItemTooltips);
        }

        List<String> tooltips = GuiContainerManager.itemDisplayNameMultiline(stack, guiContainer, true);

        GuiContainerManager.applyItemCountDetails(tooltips, stack);

        if (GuiContainerManager.getManager() != null && GuiContainerManager.shouldShowTooltip(guiContainer)) {
            for (IContainerTooltipHandler handler : GuiContainerManager.getManager().instanceTooltipHandlers)
                tooltips = handler.handleItemTooltip(guiContainer, stack, getContext().getMouseX(), getContext().getMouseY(), tooltips);
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
        GuiScreen guiScreen = getScreen().getScreenWrapper().getGuiScreen();
        if (!(guiScreen instanceof GuiContainer))
            throw new IllegalStateException("The gui must be an instance of GuiContainer if it contains slots!");
        GuiContainerAccessor acc = (GuiContainerAccessor) guiScreen;
        RenderItem renderItem = ((GuiScreenAccessor) guiScreen).getItemRender();
        ItemStack itemstack = slotIn.getStack();
        boolean flag = false;
        boolean flag1 = slotIn == acc.getClickedSlot() && acc.getDraggedStack() != null && !acc.getIsRightMouseClick();
        ItemStack itemstack1 = guiScreen.mc.thePlayer.inventory.getItemStack();
        int amount = -1;
        String format = null;

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
                flag = true;
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

        ((GuiAccessor) guiScreen).setZLevel(100f);
        renderItem.zLevel = 100.0F;

        if (!flag1) {
            if (flag) {
                GuiDraw.drawRect(1, 1, 16, 16, -2130706433);
            }

            if (itemstack != null) {
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                GL11.glEnable(GL11.GL_LIGHTING);
                RenderHelper.enableGUIStandardItemLighting();
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                // render the item itself
                renderItem.renderItemAndEffectIntoGUI(Minecraft.getMinecraft().fontRenderer, Minecraft.getMinecraft().getTextureManager(), itemstack, 1, 1);
                GuiDraw.afterRenderItemAndEffectIntoGUI(itemstack);
                GL11.glDisable(GL12.GL_RESCALE_NORMAL);
                if (amount < 0) {
                    amount = itemstack.stackSize;
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

                int cachedCount = itemstack.stackSize;
                itemstack.stackSize = 1; // required to not render the amount overlay
                // render other overlays like durability bar
                renderItem.renderItemOverlayIntoGUI(((GuiScreenAccessor) guiScreen).getFontRenderer(), Minecraft.getMinecraft().getTextureManager(), itemstack, 1, 1, null);
                itemstack.stackSize = cachedCount;
                GL11.glDisable(GL11.GL_DEPTH_TEST);
            }
        }

        ((GuiAccessor) guiScreen).setZLevel(0f);
        renderItem.zLevel = 0f;
    }

    @Override
    public boolean handleDragAndDrop(@NotNull ItemStack draggedStack, int button) {
        if (!this.syncHandler.isPhantom()) return false;
        this.syncHandler.updateFromClient(draggedStack);
        draggedStack.stackSize = 0;
        return true;
    }

    @Override
    public @Nullable ItemStack getStackForNEI() {
        return this.syncHandler.getSlot().getStack();
    }

    protected TextRenderer getTextRenderer() {
        return textRenderer;
    }
}
