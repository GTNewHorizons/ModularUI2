package com.cleanroommc.modularui.core.mixins.late.nei;


import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.integration.nei.INEIRecipeTransfer;
import com.cleanroommc.modularui.integration.nei.ModularUIGuiContainerStackPositioner;
import com.cleanroommc.modularui.integration.nei.NEIModularUIConfig;
import com.cleanroommc.modularui.screen.ModularContainer;

import net.minecraft.client.gui.inventory.GuiContainer;

import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.api.IStackPositioner;
import codechicken.nei.recipe.RecipeInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

/*
    Mixin to properly handle idents for modular uis
*/
@Mixin(RecipeInfo.class)
public class RecipeInfoMixin {

    @Inject(method = "hasOverlayHandler(Lnet/minecraft/client/gui/inventory/GuiContainer;Ljava/lang/String;)Z", remap = false, cancellable = true, at = @At("HEAD"))
    private static void modularui$hasOverlayHandler(GuiContainer gui, String ident, CallbackInfoReturnable<Boolean> ci) {
        if (gui instanceof IMuiScreen && gui.inventorySlots instanceof ModularContainer muc &&
                muc instanceof INEIRecipeTransfer<?> tr && Arrays.asList(tr.getIdents()).contains(ident)) {
            ci.setReturnValue(true);
        }
    }

    @Inject(method = "getStackPositioner", remap = false, cancellable = true, at = @At("HEAD"))
    private static void modularui$getStackPositioner(GuiContainer gui, String ident, CallbackInfoReturnable<IStackPositioner> ci) {
        ModularUIGuiContainerStackPositioner<?> positioner = ModularUIGuiContainerStackPositioner.of(gui, ident);
        if (positioner != null) {
            ci.setReturnValue(positioner);
        }
    }

    @Inject(method = "getOverlayHandler", remap = false, cancellable = true, at = @At("HEAD"))
    private static void modularui$getOverlayHandler(GuiContainer gui, String ident, CallbackInfoReturnable<IOverlayHandler> ci) {
        if (gui instanceof IMuiScreen && gui.inventorySlots instanceof ModularContainer muc &&
                muc instanceof INEIRecipeTransfer<?> tr && Arrays.asList(tr.getIdents()).contains(ident)) {
            ci.setReturnValue(NEIModularUIConfig.overlayHandler);
        }
    }
}
