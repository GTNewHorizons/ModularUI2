package com.cleanroommc.modularui.mixins.early.forge;

import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ForgeHooksClient.class)
public interface ForgeHooksClientMixin {

    @Accessor(remap = false)
    static void setStencilBits(int stencilBits) {
        throw new UnsupportedOperationException();
    }
}
