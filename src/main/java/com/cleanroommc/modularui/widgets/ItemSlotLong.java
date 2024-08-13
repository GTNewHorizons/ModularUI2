package com.cleanroommc.modularui.widgets;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.cleanroommc.modularui.api.IItemStackLong;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.TextRenderer;
import com.cleanroommc.modularui.utils.item.IItemHandlerLong;
import com.cleanroommc.modularui.utils.item.IItemHandlerModifiable;
import com.cleanroommc.modularui.mixins.early.minecraft.GuiContainerAccessor;
import com.cleanroommc.modularui.screen.GuiScreenWrapper;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.NumberFormat;
import com.cleanroommc.modularui.utils.item.ItemStackLong;
import com.cleanroommc.modularui.value.sync.ItemSlotLongSH;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlotLong;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

public class ItemSlotLong extends ItemSlot<ItemSlotLong> {

    private ItemSlotLongSH syncHandler;

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        if (syncHandler instanceof ItemSlotLongSH itemSlotSH) {
            this.syncHandler = itemSlotSH;
            return true;
        }
        return false;
    }

    @Override
    public void draw(GuiContext context, WidgetTheme widgetTheme) {
        if (this.syncHandler == null) return;
        RenderHelper.enableGUIStandardItemLighting();
        drawSlot(getSlotLong());
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

    public ModularSlotLong getSlotLong() {
        return this.syncHandler.getSlot();
    }

    @Override
    public Slot getVanillaSlot() {
        return this.syncHandler.getSlot();
    }

    @SideOnly(Side.CLIENT)
    private void drawSlot(ModularSlotLong slotIn) {
        GuiScreenWrapper guiScreen = getScreen().getScreenWrapper();
        GuiContainerAccessor accessor = guiScreen.getAccessor();
        IItemStackLong itemstack = slotIn.getStackLong();
        boolean flag = false;
        boolean flag1 = slotIn == accessor.getClickedSlot() && accessor.getDraggedStack() != null && !accessor.getIsRightMouseClick();
        ItemStack itemstack2 = guiScreen.mc.thePlayer.inventory.getItemStack();
        IItemStackLong itemstack1 = itemstack2 == null ? null : new ItemStackLong(itemstack2);
        long amount = -1;
        TextRenderer textRenderer = getTextRenderer();
        String format = null;

        if (slotIn == accessor.getClickedSlot() && accessor.getDraggedStack() != null && accessor.getIsRightMouseClick() && itemstack != null) {
            itemstack = itemstack.copy();
            itemstack.setStackSize(itemstack.getStackSize() / 2);
        } else if (guiScreen.isDragSplitting() && guiScreen.getDragSlots().contains(slotIn) && itemstack1 != null) {
            if (guiScreen.getDragSlots().size() == 1) {
                return;
            }

            // canAddItemToSlot
            if (Container.func_94527_a(slotIn, itemstack2, true) && guiScreen.inventorySlots.canDragIntoSlot(slotIn)) {
                itemstack = itemstack1.copy();
                flag = true;
                // computeStackSize
                Container.func_94525_a(guiScreen.getDragSlots(), accessor.getDragSplittingLimit(), itemstack.getAsItemStack(), slotIn.getStack() == null ? 0 : slotIn.getStack().stackSize);
                long k = Math.min(itemstack.getMaxStackSize(), slotIn.getSlotStackLimit());

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

            if (itemstack != null) {
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

    /**
     * Made to do nothign as we are not able to handle a slot, which is not a ModularSlotLong
     */
    @Override
    public ItemSlotLong slot(ModularSlot slot) {
        return this;
    }

    /**
     *  Made to do nothing as we are not able to handle a handler, which doesn't use IItemHandlerLong
     */
    @Override
    public ItemSlotLong slot(IItemHandlerModifiable itemHandler, int index) {
        return this;
    }

    public ItemSlotLong slot(IItemHandlerLong itemHandler, int index) {
        return slot(new ModularSlotLong(itemHandler, index));
    }

    public ItemSlotLong slot(ModularSlotLong slot) {
        this.syncHandler = new ItemSlotLongSH(slot);
        setSyncHandler(this.syncHandler);
        return this;
    }
}
