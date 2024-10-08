package com.cleanroommc.modularui.mixins.early.minecraft;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.screen.ClientScreenHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(
            method = "runGameLoop",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V",
                    ordinal = 2,
                    shift = At.Shift.AFTER))
    public void modularui$onRender(CallbackInfo ci) {
        if (ModularUI.proxy == null || ModularUI.proxy.getTimer60Fps() == null) return;
        ModularUI.proxy.getTimer60Fps().updateTimer();
        for (int j = 0; j < Math.min(20, ModularUI.proxy.getTimer60Fps().elapsedTicks); ++j) {
            ClientScreenHandler.onFrameUpdate();
        }
    }
}
