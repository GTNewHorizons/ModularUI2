package com.cleanroommc.modularui.mixins.early.minecraft;

import com.cleanroommc.modularui.screen.GuiContainerWrapper;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiContainer.class)
public class GuiContainerMixin {

    @Shadow
    private Slot theSlot;

    /**
     * Mixin into ModularUI screen wrapper to return the true hovered slot.
     * The method is private and only the mouse pos is ever passed to this method.
     * That's why we can just return the current hovered slot.
     */
    @Inject(method = "getSlotAtPosition", at = @At("HEAD"), cancellable = true)
    public void modularui$injectGetSlotAtPosition(int x, int y, CallbackInfoReturnable<Slot> cir) {
        if (((Object) this).getClass() == GuiContainerWrapper.class) {
            cir.setReturnValue(this.theSlot);
        }
    }

    // https://github.com/MinecraftForge/MinecraftForge/pull/2378

    @ModifyVariable(method = "mouseClicked",
            at = @At(value = "STORE"),
            name = "flag1")
    private boolean modularui$fixSlotClickOutsideBoundaryOnMouseClick(boolean flag1, @Local(name = "slot") Slot slot) {
        return flag1 && slot == null;
    }

    @ModifyVariable(method = "mouseMovedOrUp",
            at = @At(value = "STORE"),
            name = "flag")
    private boolean modularui$fixSlotClickOutsideBoundaryOnMouseRelease(boolean flag, @Local(name = "slot") Slot slot) {
        return flag && slot == null;
    }
}
