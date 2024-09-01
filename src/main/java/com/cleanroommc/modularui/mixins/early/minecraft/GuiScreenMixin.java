package com.cleanroommc.modularui.mixins.early.minecraft;

import com.cleanroommc.modularui.api.event.KeyboardInputEvent;
import com.cleanroommc.modularui.api.event.MouseInputEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import net.minecraftforge.common.MinecraftForge;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiScreen.class)
public abstract class GuiScreenMixin {

    @Shadow
    public Minecraft mc;

    @Shadow
    public abstract void handleMouseInput();

    @Shadow
    public abstract void handleKeyboardInput();

    @Redirect(method = "handleInput",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;handleMouseInput()V"))
    public void modularui$injectMouseInputEvent(GuiScreen instance) {
        if (MinecraftForge.EVENT_BUS.post(new MouseInputEvent.Pre(modularui$getThis()))) {
            return;
        }
        this.handleMouseInput();
        if (modularui$getThis().equals(this.mc.currentScreen)) {
            MinecraftForge.EVENT_BUS.post(new MouseInputEvent.Post(modularui$getThis()));
        }
    }

    @Redirect(method = "handleInput",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;handleKeyboardInput()V"))
    public void modularui$injectKeyboardInputEvent(GuiScreen instance) {
        if (MinecraftForge.EVENT_BUS.post(new KeyboardInputEvent.Pre(modularui$getThis()))) {
            return;
        }
        this.handleKeyboardInput();
        if (modularui$getThis().equals(this.mc.currentScreen)) {
            MinecraftForge.EVENT_BUS.post(new KeyboardInputEvent.Post(modularui$getThis()));
        }
    }

    @Unique
    private GuiScreen modularui$getThis() {
        return (GuiScreen) (Object) this;
    }
}
