package com.cleanroommc.modularui.core.mixins.late.thaumcraft;

import com.cleanroommc.modularui.core.mixins.early.minecraft.GuiContainerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import thaumcraft.client.lib.ClientTickEventsFML;

@Mixin(ClientTickEventsFML.class)
public abstract class ClientTickEventsFMLMixin {

    @Redirect(
            remap = false,
            method = "renderAspectsInGui",
            at = @At(
                    remap = false,
                    value = "INVOKE",
                    target = "Lthaumcraft/client/lib/ClientTickEventsFML;isMouseOverSlot(Lnet/minecraft/inventory/Slot;IIII)Z",
                    args = "log"
            ))
    private boolean modularui$isMouseOverMuiSlot(ClientTickEventsFML inst, Slot slot, int mouseX, int mouseY, int guiLeft, int guiTop) {
        GuiContainer gui = (GuiContainer) Minecraft.getMinecraft().currentScreen;
        Slot hoveredSlot = ((GuiContainerAccessor) gui).getHoveredSlot();
        return hoveredSlot == slot;
    }
}
