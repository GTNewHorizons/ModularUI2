package com.cleanroommc.modularui.mixins.thaumcraft;

import com.cleanroommc.modularui.mixins.GuiContainerAccessor;
import com.cleanroommc.modularui.screen.ModularContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;

import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.client.lib.ClientTickEventsFML;

import static codechicken.lib.gui.GuiDraw.gui;

@Mixin(ClientTickEventsFML.class)
public abstract class ClientTickEventsFMLMixin {

    @Shadow(remap = false)
    abstract boolean isMouseOverSlot(Slot slot, int mouseX, int mouseY, int guiLeft, int guiTop);

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
