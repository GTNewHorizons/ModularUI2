package com.cleanroommc.modularui.mixins.late.nei;

import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.api.IStackPositioner;
import codechicken.nei.recipe.RecipeInfo;
import com.cleanroommc.modularui.integration.nei.GuiContainerWrapperOverlayHandler;
import com.cleanroommc.modularui.integration.nei.GuiContainerWrapperStackPositioner;
import com.cleanroommc.modularui.integration.nei.INEIRecipeTransfer;
import com.cleanroommc.modularui.integration.nei.NEIModularUIConfig;
import com.cleanroommc.modularui.screen.GuiContainerWrapper;
import com.cleanroommc.modularui.screen.ModularContainer;
import net.minecraft.client.gui.inventory.GuiContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

/*
    Mixin to properly handle idents for modular uis
*/
@Mixin(RecipeInfo.class)
public class ModularIdentMixin {


    @Inject(method = "hasOverlayHandler(Lnet/minecraft/client/gui/inventory/GuiContainer;Ljava/lang/String;)Z", remap = false,cancellable = true, at=@At("HEAD"))
    private static void hasOverlayHandler(GuiContainer gui, String ident, CallbackInfoReturnable<Boolean> ci) {
        if (gui instanceof GuiContainerWrapper muw) {
            if (gui.inventorySlots instanceof ModularContainer muc && muc instanceof INEIRecipeTransfer<?> tr) {
                if (Arrays.asList(tr.getIdents()).contains(ident)) {
                    ci.setReturnValue(true);
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "getStackPositioner", remap = false,cancellable = true, at=@At("HEAD"))
    private static void getStackPositioner(GuiContainer gui, String ident, CallbackInfoReturnable<IStackPositioner> ci) {
        if (gui instanceof GuiContainerWrapper muw) {
            if (gui.inventorySlots instanceof ModularContainer muc && muc instanceof INEIRecipeTransfer<?> tr) {
                if (Arrays.asList(tr.getIdents()).contains(ident)) {
                    //Hacky way around it, but should work
                    GuiContainerWrapperStackPositioner positioner=NEIModularUIConfig.stackPositioner;
                    positioner.wrapper=muw;
                    positioner.container=muc;
                    positioner.recipeTransfer=tr;
                    ci.setReturnValue(positioner);
                    ci.cancel();
                    return;
                }
            }
        }
    }

    @Inject(method = "getOverlayHandler", remap = false,cancellable = true, at=@At("HEAD"))
    private static void getOverlayHandler(GuiContainer gui, String ident, CallbackInfoReturnable<IOverlayHandler> ci) {
        if (gui instanceof GuiContainerWrapper muw) {
            if (gui.inventorySlots instanceof ModularContainer muc && muc instanceof INEIRecipeTransfer<?> tr) {
                if (Arrays.asList(tr.getIdents()).contains(ident)) {
                    ci.setReturnValue(NEIModularUIConfig.overlayHandler);
                    ci.cancel();
                    return;
                }
            }
        }
    }
}
