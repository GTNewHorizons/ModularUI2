package com.cleanroommc.modularui.utils;

import com.cleanroommc.modularui.drawable.BufferBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.util.function.Consumer;

/**
 * Version specific code is supposed to go here.
 * Ideally only the body of methods and value of fields should be changed and no signatures.
 */
public class Platform {

    public static final ItemStack EMPTY_STACK = null;

    @SideOnly(Side.CLIENT)
    public static @NotNull EntityPlayerSP getClientPlayer() {
        return Minecraft.getMinecraft().thePlayer;
    }

    @SideOnly(Side.CLIENT)
    public static String getKeyDisplay(KeyBinding keyBinding) {
        return GameSettings.getKeyDisplayString(keyBinding.getKeyCode());
    }

    public static boolean isStackEmpty(ItemStack stack) {
        return stack == null || stack.getItem() == null || stack.stackSize <= 0;
    }

    public static ItemStack copyStack(ItemStack stack) {
        return isStackEmpty(stack) ? EMPTY_STACK : stack.copy();
    }

    public static void startDrawing(DrawMode drawMode, VertexFormat format, Consumer<BufferBuilder> bufferBuilder) {
        Tessellator tessellator = Tessellator.instance;
        BufferBuilder buffer = BufferBuilder.bufferbuilder;
        tessellator.startDrawing(drawMode.mode);
        bufferBuilder.accept(buffer);
        tessellator.draw();
    }

    public static void setupDrawColor() {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
    }

    public static void setupDrawTex(ResourceLocation texture) {
        setupDrawTex();
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);
    }

    public static void setupDrawTex(int textureId) {
        setupDrawTex();
        GlStateManager.bindTexture(textureId);
    }

    public static void setupDrawTex() {
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
    }

    public static void setupDrawGradient() {
        setupDrawGradient(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    }

    public static void setupDrawGradient(GlStateManager.SourceFactor srcFactor, GlStateManager.DestFactor destFactor, GlStateManager.SourceFactor srcFactorAlpha, GlStateManager.DestFactor destFactorAlpha) {
        GlStateManager.tryBlendFuncSeparate(srcFactor, destFactor, srcFactorAlpha, destFactorAlpha);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
    }

    public static void endDrawGradient() {
        GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    public static void setupDrawItem() {
        setupDrawTex();
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableDepth();
    }

    public static void endDrawItem() {
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableDepth();
    }

    public static void setupDrawFont() {
        setupDrawTex();
    }

    /**
     * <a href="https://i.sstatic.net/sfQdh.jpg">Reference</a>
     */
    public enum DrawMode {
        QUADS(GL11.GL_QUADS),
        POINTS(GL11.GL_POINTS),
        LINES(GL11.GL_LINES),
        LINE_STRIP(GL11.GL_LINE_STRIP),
        LINE_LOOP(GL11.GL_LINE_LOOP),
        TRIANGLES(GL11.GL_TRIANGLES),
        TRIANGLE_STRIP(GL11.GL_TRIANGLE_STRIP),
        TRIANGLE_FAN(GL11.GL_TRIANGLE_FAN);

        public final int mode;

        DrawMode(int mode) {
            this.mode = mode;
        }
    }

    // kept for parity with 1.12
    public enum VertexFormat {

        POS,
        POS_TEX,
        POS_COLOR,
        POS_TEX_COLOR,
        POS_NORMAL,
        POS_TEX_NORMAL,
        POS_TEX_COLOR_NORMAL,
        POS_TEX_LMAP_COLOR;
    }
}
