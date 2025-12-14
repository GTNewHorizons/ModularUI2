package com.cleanroommc.modularui.core.mixins.early.minecraft;

import com.cleanroommc.modularui.screen.ModularContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerMP.class)
public abstract class EntityPlayerMPMixin extends EntityPlayer {

    public EntityPlayerMPMixin(World p_i45324_1_, GameProfile p_i45324_2_) {
        super(p_i45324_1_, p_i45324_2_);
    }

    @Inject(method = "closeContainer", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Container;onContainerClosed(Lnet/minecraft/entity/player/EntityPlayer;)V"))
    public void closeContainer(CallbackInfo ci) {
        // replicates the container closed event listener from 1.12
        if (this.openContainer instanceof ModularContainer mc) {
            mc.onModularContainerClosed();
        }
    }
}
