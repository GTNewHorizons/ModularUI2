package com.cleanroommc.modularui.core.mixins.early.minecraft;

import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.screen.IClickableGuiContainer;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiContainer.class)
public class GuiContainerMixin implements IClickableGuiContainer {

    @Shadow
    private Slot theSlot;

    @Unique
    private Slot modularUI$clickedSlot;

    /**
     * Mixin into ModularUI screen wrapper to return the true hovered slot.
     * The method is private and only the mouse pos is ever passed to this method.
     * That's why we can just return the current hovered slot.
     */
    @Inject(method = "getSlotAtPosition", at = @At("HEAD"), cancellable = true)
    public void getSlot(int x, int y, CallbackInfoReturnable<Slot> cir) {
        if (this.modularUI$clickedSlot != null) {
            cir.setReturnValue(this.modularUI$clickedSlot);
        } else if (IMuiScreen.class.isAssignableFrom(this.getClass())) {
            cir.setReturnValue(this.theSlot);
        }
    }

    @Override
    public void modularUI$setClickedSlot(Slot slot) {
        this.modularUI$clickedSlot = slot;
    }

    @Override
    public Slot modularUI$getClickedSlot() {
        return modularUI$clickedSlot;
    }

    /**
     * Inject to {@link net.minecraft.client.gui.inventory.GuiContainer#mouseClicked} Line366
     * Set flag1 to false if a slot is clicked, to fix the bug of tossing item when clicking slot outside the main panel area.
     * This fix should only apply to 1.7.10, since such bug only appears in 1.7.10.
     */
    @ModifyVariable(
            method = "mouseClicked",
            at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/inventory/Slot;slotNumber:I"), ordinal = 1)
    protected boolean mouseClickedOnSlot(boolean flag1) {
        return false;
    }
}
