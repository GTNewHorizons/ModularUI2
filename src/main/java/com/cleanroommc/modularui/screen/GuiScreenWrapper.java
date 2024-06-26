package com.cleanroommc.modularui.screen;

import codechicken.nei.NEIClientUtils;
import codechicken.nei.VisiblityData;
import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.api.TaggedInventoryArea;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.guihook.IContainerDrawHandler;
import codechicken.nei.guihook.IContainerInputHandler;
import com.cleanroommc.modularui.GuiErrorHandler;
import com.cleanroommc.modularui.ModularUIConfig;
import com.cleanroommc.modularui.api.widget.IGuiElement;
import com.cleanroommc.modularui.api.widget.IVanillaSlot;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.Stencil;
import com.cleanroommc.modularui.integration.nei.NEIDragAndDropHandler;
import com.cleanroommc.modularui.mixins.early.minecraft.GuiContainerAccessor;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.screen.viewport.LocatedWidget;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.Rectangle;
import java.util.List;
import java.util.Set;

import static com.cleanroommc.modularui.ModularUI.MODID_NEI;
import static com.cleanroommc.modularui.ModularUI.isNEILoaded;

@Optional.Interface(iface = "codechicken.nei.api.INEIGuiHandler", modid = MODID_NEI)
@SideOnly(Side.CLIENT)
public class GuiScreenWrapper extends GuiContainer implements INEIGuiHandler {

    private final ModularScreen screen;
    private boolean init = true;
    private char lastChar;

    private int fps, frameCount = 0;
    private long timer = Minecraft.getSystemTime();

    public GuiScreenWrapper(ModularContainer container, ModularScreen screen) {
        super(container);
        this.screen = screen;
        this.screen.construct(this);
    }

    @Override
    public void initGui() {
        GuiErrorHandler.INSTANCE.clear();
        super.initGui();
        if (this.init) {
            this.screen.onOpen();
            this.init = false;
        }
        this.screen.onResize(this.width, this.height);
    }

    public void updateArea(Area mainViewport) {
        this.guiLeft = mainViewport.x;
        this.guiTop = mainViewport.y;
        this.xSize = mainViewport.width;
        this.ySize = mainViewport.height;
    }

    public GuiContainerAccessor getAccessor() {
        return (GuiContainerAccessor) this;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.frameCount++;
        long time = Minecraft.getSystemTime();
        if (time - this.timer >= 1000) {
            this.fps = this.frameCount;
            this.frameCount = 0;
            this.timer += 1000;
        }

        Stencil.reset();
        Stencil.apply(this.screen.getScreenArea(), null);
        drawDefaultBackground();
        int i = this.guiLeft;
        int j = this.guiTop;

        this.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        this.screen.drawScreen(mouseX, mouseY, partialTicks);

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        // mainly for invtweaks compat
        drawVanillaElements(mouseX, mouseY, partialTicks);
        GL11.glPushMatrix();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        getAccessor().setHoveredSlot(null);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.enableGUIStandardItemLighting();
        if (this.screen.getContext().getNEISettings().isNEIEnabled(this.screen)) {
            // Copied from GuiContainerManager#renderObjects but without translation
            for (IContainerDrawHandler drawHandler : GuiContainerManager.drawHandlers) {
                drawHandler.renderObjects(this, mouseX, mouseY);
            }
            for (IContainerDrawHandler drawHandler : GuiContainerManager.drawHandlers) {
                drawHandler.postRenderObjects(this, mouseX, mouseY);
            }

//            if (!shouldRenderOurTooltip()) {
            // nh todo?
            if (true) {
                GuiContainerManager.getManager().renderToolTips(mouseX, mouseY);
            }
        }
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.disableStandardItemLighting();
        this.drawGuiContainerForegroundLayer(mouseX, mouseY);
        this.screen.drawForeground(partialTicks);
        RenderHelper.enableGUIStandardItemLighting();

        getAccessor().setHoveredSlot(null);
        IGuiElement hovered = this.screen.getContext().getHovered();
        if (hovered instanceof IVanillaSlot vanillaSlot) {
            getAccessor().setHoveredSlot(vanillaSlot.getVanillaSlot());
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPushMatrix();
        GL11.glTranslatef(i, j, 0);
        GL11.glPopMatrix();

        InventoryPlayer inventoryplayer = this.mc.thePlayer.inventory;
        ItemStack itemstack = getAccessor().getDraggedStack() == null ? inventoryplayer.getItemStack() : getAccessor().getDraggedStack();
        GL11.glTranslatef(i, j, 0.0F);
        if (itemstack != null) {
            int k2 = getAccessor().getDraggedStack() == null ? 8 : 16;
            String s = null;

            if (getAccessor().getDraggedStack() != null && getAccessor().getIsRightMouseClick()) {
                itemstack = itemstack.copy();
                itemstack.stackSize = MathHelper.ceiling_double_int((float) itemstack.stackSize / 2.0F);
            } else if (this.isDragSplitting() && this.getDragSlots().size() > 1) {
                itemstack = itemstack.copy();
                itemstack.stackSize = getAccessor().getDragSplittingRemnant();

                if (itemstack.stackSize < 1) {
                    s = EnumChatFormatting.YELLOW + "0";
                }
            }

            this.drawItemStack(itemstack, mouseX - i - 8, mouseY - j - k2, s);
        }

        if (getAccessor().getReturningStack() != null) {
            float f = (float) (Minecraft.getSystemTime() - getAccessor().getReturningStackTime()) / 100.0F;

            if (f >= 1.0F) {
                f = 1.0F;
                getAccessor().setReturningStack(null);
            }

            int l2 = getAccessor().getReturningStackDestSlot().xDisplayPosition - getAccessor().getTouchUpX();
            int i3 = getAccessor().getReturningStackDestSlot().yDisplayPosition - getAccessor().getTouchUpY();
            int l1 = getAccessor().getTouchUpX() + (int) ((float) l2 * f);
            int i2 = getAccessor().getTouchUpY() + (int) ((float) i3 * f);
            this.drawItemStack(getAccessor().getReturningStack(), l1, i2, null);
        }

        GL11.glPopMatrix();

        if (ModularUIConfig.guiDebugMode) {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_BLEND);
            drawDebugScreen();
            GL11.glColor4f(1f, 1f, 1f, 1f);
        }
        GuiErrorHandler.INSTANCE.drawErrors(0, 0);

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.enableStandardItemLighting();

        Stencil.remove();
    }

    @Override
    public void drawWorldBackground(int tint) {
        if (this.mc.theWorld == null) {
            super.drawWorldBackground(tint);
            return;
        }
        float alpha = this.screen.getMainPanel().getAlpha();
        // vanilla color values as hex
        int color = 0x101010;
        int startAlpha = 0xc0;
        int endAlpha = 0xd0;
        this.drawGradientRect(0, 0, this.width, this.height, Color.withAlpha(color, (int) (startAlpha * alpha)), Color.withAlpha(color, (int) (endAlpha * alpha)));
    }

    private void drawItemStack(ItemStack stack, int x, int y, String altText) {
        GL11.glTranslatef(0.0F, 0.0F, 32.0F);
        this.zLevel = 200.0F;
        itemRender.zLevel = 200.0F;
        FontRenderer font = stack.getItem().getFontRenderer(stack);
        if (font == null) font = this.fontRendererObj;
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        itemRender.renderItemAndEffectIntoGUI(font, mc.getTextureManager(), stack, x, y);
        itemRender.renderItemOverlayIntoGUI(font, mc.getTextureManager(), stack, x, y - (getAccessor().getDraggedStack() == null ? 0 : 8), altText);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GuiDraw.afterRenderItemAndEffectIntoGUI(stack);
        this.zLevel = 0.0F;
        itemRender.zLevel = 0.0F;
    }

    protected void drawVanillaElements(int mouseX, int mouseY, float partialTicks) {
        for (Object guiButton : this.buttonList) {
            ((GuiButton) guiButton).drawButton(this.mc, mouseX, mouseY);
        }
        for (Object guiLabel : this.labelList) {
            ((GuiLabel) guiLabel).func_146159_a(this.mc, mouseX, mouseY);
        }
    }

    public void drawDebugScreen() {
        GuiContext context = this.screen.getContext();
        int mouseX = context.getAbsMouseX(), mouseY = context.getAbsMouseY();
        int screenH = this.screen.getScreenArea().height;
        int color = Color.rgb(180, 40, 115);
        int lineY = screenH - 13 - (this.screen.getContext().getNEISettings().isNEIEnabled(this.screen) ? 20 : 0);
        drawString(this.fontRendererObj, "Mouse Pos: " + mouseX + ", " + mouseY, 5, lineY, color);
        lineY -= 11;
        drawString(this.fontRendererObj, "FPS: " + this.fps, 5, lineY, color);
        LocatedWidget locatedHovered = this.screen.getPanelManager().getTopWidgetLocated(true);
        if (locatedHovered != null) {
            drawSegmentLine(lineY -= 4, color);
            lineY -= 10;

            IGuiElement hovered = locatedHovered.getElement();
            locatedHovered.applyMatrix(context);
            GL11.glPushMatrix();
            context.applyToOpenGl();

            Area area = hovered.getArea();
            IGuiElement parent = hovered.getParent();

            GuiDraw.drawBorder(0, 0, area.width, area.height, color, 1f);
            if (hovered.hasParent()) {
                GuiDraw.drawBorder(-area.rx, -area.ry, parent.getArea().width, parent.getArea().height, Color.withAlpha(color, 0.3f), 1f);
            }
            GL11.glPopMatrix();
            locatedHovered.unapplyMatrix(context);
            GuiDraw.drawText("Pos: " + area.x + ", " + area.y + "  Rel: " + area.rx + ", " + area.ry, 5, lineY, 1, color, false);
            lineY -= 11;
            GuiDraw.drawText("Size: " + area.width + ", " + area.height, 5, lineY, 1, color, false);
            lineY -= 11;
            GuiDraw.drawText("Class: " + hovered, 5, lineY, 1, color, false);
            if (hovered.hasParent()) {
                drawSegmentLine(lineY -= 4, color);
                lineY -= 10;
                area = parent.getArea();
                GuiDraw.drawText("Parent size: " + area.width + ", " + area.height, 5, lineY, 1, color, false);
                lineY -= 11;
                GuiDraw.drawText("Parent: " + parent, 5, lineY, 1, color, false);
            }
            if (hovered instanceof ItemSlot slotWidget) {
                drawSegmentLine(lineY -= 4, color);
                lineY -= 10;
                ModularSlot slot = slotWidget.getSlot();
                GuiDraw.drawText("Slot Index: " + slot.getSlotIndex(), 5, lineY, 1, color, false);
                lineY -= 11;
                GuiDraw.drawText("Slot Number: " + slot.slotNumber, 5, lineY, 1, color, false);
                lineY -= 11;
                if (slotWidget.isSynced()) {
                    SlotGroup slotGroup = slot.getSlotGroup();
                    boolean allowShiftTransfer = slotGroup != null && slotGroup.allowShiftTransfer();
                    GuiDraw.drawText("Shift-Click Priority: " + (allowShiftTransfer ? slotGroup.getShiftClickPriority() : "DISABLED"), 5, lineY, 1, color, false);
                }
            }
        }
        // dot at mouse pos
        drawRect(mouseX, mouseY, mouseX + 1, mouseY + 1, Color.withAlpha(Color.GREEN.main, 0.8f));
    }

    private void drawSegmentLine(int y, int color) {
        GuiDraw.drawRect(5, y, 140, 1, color);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.screen.onUpdate();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        this.screen.onClose();
        this.init = true;
    }

    public ModularScreen getScreen() {
        return this.screen;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (this.screen.onMousePressed(mouseButton)) {
            if (this.screen.getContext().getNEISettings().isNEIEnabled(this.screen)) {
                for (IContainerInputHandler inputhander : GuiContainerManager.inputHandlers) {
                    inputhander.onMouseClicked(this, mouseX, mouseY, mouseButton);
                }
            }
            return;
        }
        // NEI injects GuiContainerManager#mouseClicked there.
        // Ideally we should call `onMouseClicked` before handling our click behaviors,
        // but then they will be called twice if our onMousePressed returns false.
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void clickSlot() {
        super.mouseClicked(this.screen.getContext().getAbsMouseX(), this.screen.getContext().getAbsMouseY(), this.screen.getContext().getMouseButton());
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        if (this.screen.onMouseRelease(state)) return;
        super.mouseMovedOrUp(mouseX, mouseY, state);
    }

    public void releaseSlot() {
        super.mouseMovedOrUp(this.screen.getContext().getAbsMouseX(), this.screen.getContext().getAbsMouseY(), this.screen.getContext().getMouseButton());
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (this.screen.onMouseDrag(clickedMouseButton, timeSinceLastClick)) return;
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    public void dragSlot(long timeSinceLastClick) {
        super.mouseClickMove(this.screen.getContext().getAbsMouseX(), this.screen.getContext().getAbsMouseY(), this.screen.getContext().getMouseButton(), timeSinceLastClick);
    }

    @Override
    public void handleMouseInput() {
        int scrolled = Mouse.getEventDWheel();
        if (scrolled != 0 && this.screen.onMouseScroll(scrolled > 0 ? ModularScreen.UpOrDown.UP : ModularScreen.UpOrDown.DOWN, Math.abs(scrolled))) {
            return;
        }
        super.handleMouseInput();
    }

    /**
     * This replicates vanilla behavior while also injecting custom behavior for consistency
     */
    @Override
    public void handleKeyboardInput() {
        char c0 = Keyboard.getEventCharacter();
        int key = Keyboard.getEventKey();
        boolean state = Keyboard.getEventKeyState();

        if (isNEILoaded && GuiContainerManager.getManager().firstKeyTyped(c0, key)) {
            return;
        }

        if (state) {
            this.lastChar = c0;
            if (this.screen.onKeyPressed(c0, key)) return;
            keyTyped(c0, key);
        } else {
            // when the key is released, the event char is empty
            if (this.screen.onKeyRelease(this.lastChar, key)) return;
            if (key == 0 && c0 >= ' ') {
                keyTyped(c0, key);
            }
        }

        this.mc.func_152348_aa();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        // debug mode C + CTRL + SHIFT + ALT
        if (keyCode == Keyboard.KEY_C && isCtrlKeyDown() && isShiftKeyDown() && Interactable.hasAltDown()) {
            ModularUIConfig.guiDebugMode = !ModularUIConfig.guiDebugMode;
            return;
        }
        if (isNEILoaded && GuiContainerManager.getManager().lastKeyTyped(keyCode, typedChar)) {
            return;
        }
        if (keyCode == Keyboard.KEY_ESCAPE || keyCode == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
            if (this.screen.getContext().hasDraggable()) {
                this.screen.getContext().dropDraggable();
            } else {
                this.screen.getPanelManager().closeTopPanel(true);
            }
        }

        this.checkHotbarKeys(keyCode);
        Slot hoveredSlot = getAccessor().getHoveredSlot();
        if (hoveredSlot != null && hoveredSlot.getHasStack()) {
            if (keyCode == this.mc.gameSettings.keyBindPickBlock.getKeyCode()) {
                this.handleMouseClick(hoveredSlot, hoveredSlot.slotNumber, 0, 3);
            } else if (keyCode == this.mc.gameSettings.keyBindDrop.getKeyCode()) {
                this.handleMouseClick(hoveredSlot, hoveredSlot.slotNumber, isCtrlKeyDown() ? 1 : 0, 4);
            }
        }
    }

    public boolean isDragSplitting() {
        return getAccessor().isDragSplittingInternal();
    }

    public Set<Slot> getDragSlots() {
        return getAccessor().getDragSplittingSlots();
    }

    public static RenderItem getItemRenderer() {
        return itemRender;
    }

    public float getZ() {
        return this.zLevel;
    }

    public void setZ(float z) {
        this.zLevel = z;
    }

    public FontRenderer getFontRenderer() {
        return this.fontRendererObj;
    }

    // === NEI overrides ===

    @Override
    public VisiblityData modifyVisiblity(GuiContainer gui, VisiblityData currentVisibility) {
        return null;
    }

    @Override
    public Iterable<Integer> getItemSpawnSlots(GuiContainer gui, ItemStack item) {
        return null;
    }

    @Override
    public List<TaggedInventoryArea> getInventoryAreas(GuiContainer gui) {
        return null;
    }

    @Override
    public boolean handleDragNDrop(GuiContainer gui, int mousex, int mousey, ItemStack draggedStack, int button) {
        if (!(gui instanceof GuiScreenWrapper) || NEIClientUtils.getHeldItem() != null) return false;
        IGuiElement hovered = ((GuiScreenWrapper) gui).getScreen().getContext().getHovered();
        if (hovered instanceof NEIDragAndDropHandler) {
            return ((NEIDragAndDropHandler) hovered).handleDragAndDrop(draggedStack, button);
        }
        return false;
    }

    @Override
    public boolean hideItemPanelSlot(GuiContainer gui, int x, int y, int w, int h) {
        if (!(gui instanceof GuiScreenWrapper)) return false;
        if (!this.screen.getContext().getNEISettings().isNEIEnabled(this.screen)) return false;
        return this.screen.getContext().getNEISettings().getAllNEIExclusionAreas().stream().anyMatch(
            a -> a.intersects(new Rectangle(x, y, w, h))
        );
    }
}
