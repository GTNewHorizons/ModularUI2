package com.cleanroommc.modularui.mixins.early.minecraft;

import com.cleanroommc.modularui.overlay.OverlayStack;

import net.minecraft.client.gui.GuiButton;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * This mixin fixes some visual bugs that can happen with overlays.
 */
@Mixin(GuiButton.class)
public abstract class GuiButtonMixin {

    @Shadow protected boolean field_146123_n; // hovered

    @Shadow
    public abstract int getHoverState(boolean mouseOver);

    @Redirect(method = "drawButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiButton;getHoverState(Z)I"))
    public int draw(GuiButton instance, boolean mouseOver) {
        // fixes buttons being hovered when an overlay element is already hovered
        if (this.field_146123_n) this.field_146123_n = !OverlayStack.isHoveringOverlay();
        return getHoverState(this.field_146123_n);
    }
}
