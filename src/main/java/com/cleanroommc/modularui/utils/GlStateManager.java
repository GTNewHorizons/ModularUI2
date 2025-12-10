package com.cleanroommc.modularui.utils;

import net.minecraft.client.renderer.OpenGlHelper;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/**
 * Thin wrapper for making backport easier. Do not use outside MUI source code, it's pointless.
 */
public class GlStateManager {

    public static void disableAlpha() {
        GL11.glDisable(GL11.GL_ALPHA_TEST);
    }

    public static void enableAlpha() {
        GL11.glEnable(GL11.GL_ALPHA_TEST);
    }

    public static void enableLighting() {
        GL11.glEnable(GL11.GL_LIGHTING);
    }

    public static void disableLighting() {
        GL11.glDisable(GL11.GL_LIGHTING);
    }

    public static void disableDepth() {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
    }

    public static void enableDepth() {
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    public static void depthMask(boolean flagIn) {
        GL11.glDepthMask(flagIn);
    }

    public static void disableBlend() {
        GL11.glDisable(GL11.GL_BLEND);
    }

    public static void enableBlend() {
        GL11.glEnable(GL11.GL_BLEND);
    }

    public static void blendFunc(int srcFactor, int dstFactor) {
        GL11.glBlendFunc(srcFactor, dstFactor);
    }

    public static void tryBlendFuncSeparate(GlStateManager.SourceFactor srcFactor, GlStateManager.DestFactor dstFactor, GlStateManager.SourceFactor srcFactorAlpha, GlStateManager.DestFactor dstFactorAlpha) {
        tryBlendFuncSeparate(srcFactor.factor, dstFactor.factor, srcFactorAlpha.factor, dstFactorAlpha.factor);
    }

    public static void tryBlendFuncSeparate(int srcFactor, int dstFactor, int srcFactorAlpha, int dstFactorAlpha) {
        OpenGlHelper.glBlendFunc(srcFactor, dstFactor, srcFactorAlpha, dstFactorAlpha);
    }

    public static void enableCull() {
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    public static void disableColorLogic() {
        GL11.glDisable(GL11.GL_COLOR_LOGIC_OP);
    }

    public static void enableTexture2D() {
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    public static void disableTexture2D() {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
    }

    public static void bindTexture(int texture) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
    }

    public static void shadeModel(int mode) {
        GL11.glShadeModel(mode);
    }

    public static void enableRescaleNormal() {
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
    }

    public static void disableRescaleNormal() {
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
    }

    public static void viewport(int x, int y, int width, int height) {
        GL11.glViewport(x, y, width, height);
    }

    public static void colorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        GL11.glColorMask(red, green, blue, alpha);
    }

    public static void clearDepth(double depth) {
        GL11.glClearDepth(depth);
    }

    public static void clear(int mask) {
        GL11.glClear(mask);
    }

    public static void matrixMode(int mode) {
        GL11.glMatrixMode(mode);
    }

    public static void loadIdentity() {
        GL11.glLoadIdentity();
    }

    public static void pushMatrix() {
        GL11.glPushMatrix();
    }

    public static void popMatrix() {
        GL11.glPopMatrix();
    }

    public static void rotate(float angle, float x, float y, float z) {
        GL11.glRotatef(angle, x, y, z);
    }

    public static void scale(float x, float y, float z) {
        GL11.glScalef(x, y, z);
    }

    public static void scale(double x, double y, double z) {
        GL11.glScaled(x, y, z);
    }

    public static void translate(float x, float y, float z) {
        GL11.glTranslatef(x, y, z);
    }

    public static void translate(double x, double y, double z) {
        GL11.glTranslated(x, y, z);
    }

    public static void color(float colorRed, float colorGreen, float colorBlue, float colorAlpha) {
        GL11.glColor4f(colorRed, colorGreen, colorBlue, colorAlpha);
    }

    public enum DestFactor {
        ONE_MINUS_SRC_ALPHA(771),
        ZERO(0);

        final int factor;

        DestFactor(int factorIn) {
            this.factor = factorIn;
        }
    }

    public enum SourceFactor {
        ONE(1),
        SRC_ALPHA(770);

        final int factor;

        SourceFactor(int factorIn) {
            this.factor = factorIn;
        }
    }
}
