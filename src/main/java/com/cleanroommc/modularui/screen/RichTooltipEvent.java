package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.api.drawable.IRichTextBuilder;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RichTooltipEvent extends Event {

    protected final IRichTextBuilder<?> tooltip;
    protected final ItemStack itemStack;
    protected int x, y;
    protected FontRenderer fontRenderer;

    private RichTooltipEvent (IRichTextBuilder<?> tooltip, ItemStack itemStack, int x, int y, FontRenderer fontRenderer) {
        this.tooltip = tooltip;
        this.itemStack = itemStack;
        this.x = x;
        this.y = y;
        this.fontRenderer = fontRenderer;
    }

    public IRichTextBuilder<?> getTooltip() {
        return tooltip;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Cancelable
    public static class Pre extends RichTooltipEvent {

        private int screenWidth;
        private int screenHeight;
        private int maxWidth;

        public Pre(ItemStack stack, List<String> lines, int x, int y, int screenWidth, int screenHeight, int maxWidth,
                   @NotNull FontRenderer fr, IRichTextBuilder<?> tooltip) {
            super(tooltip, stack, x, y, fr);
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
            this.maxWidth = maxWidth;
        }

        public int getScreenWidth() {
            return screenWidth;
        }

        public void setScreenWidth(int screenWidth) {
            this.screenWidth = screenWidth;
        }

        public int getScreenHeight() {
            return screenHeight;
        }

        public void setScreenHeight(int screenHeight) {
            this.screenHeight = screenHeight;
        }

        public int getMaxWidth() {
            return maxWidth;
        }

        public void setMaxWidth(int maxWidth) {
            this.maxWidth = maxWidth;
        }

        public void setFontRenderer(FontRenderer fontRenderer) {
            this.fontRenderer = fontRenderer;
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }
    }

    public static class Color extends RichTooltipEvent {

        private final int originalBackground;
        private final int originalBorderStart;
        private final int originalBorderEnd;
        private int background;
        private int borderStart;
        private int borderEnd;

        public Color(ItemStack stack, List<String> lines, int x, int y,
                     @NotNull FontRenderer fr, int background, int borderStart,
                     int borderEnd, IRichTextBuilder<?> tooltip) {
            super(tooltip, stack, x, y, fr);
            this.originalBackground = background;
            this.originalBorderStart = borderStart;
            this.originalBorderEnd = borderEnd;
            this.background = background;
            this.borderStart = borderStart;
            this.borderEnd = borderEnd;
        }

        public int getBackground() {
            return background;
        }

        public void setBackground(int background) {
            this.background = background;
        }

        public int getBorderStart() {
            return borderStart;
        }

        public void setBorderStart(int borderStart) {
            this.borderStart = borderStart;
        }

        public int getBorderEnd() {
            return borderEnd;
        }

        public void setBorderEnd(int borderEnd) {
            this.borderEnd = borderEnd;
        }

        public int getOriginalBackground() {
            return originalBackground;
        }

        public int getOriginalBorderStart() {
            return originalBorderStart;
        }

        public int getOriginalBorderEnd() {
            return originalBorderEnd;
        }
    }

    public static class PostBackground extends RichTooltipEvent {

        private final int width;
        private final int height;

        public PostBackground(ItemStack stack, List<String> lines, int x, int y,
                              @NotNull FontRenderer fr, int width, int height, IRichTextBuilder<?> tooltip) {
            super(tooltip, stack, x, y, fr);
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

    public static class PostText extends RichTooltipEvent {

        private final int width;
        private final int height;

        public PostText(ItemStack stack, List<String> lines, int x, int y,
                        @NotNull FontRenderer fr, int width, int height, IRichTextBuilder<?> tooltip) {
            super(tooltip, stack, x, y, fr);
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }
}
