package com.cleanroommc.modularui.core.mixins.early.minecraft;

import net.minecraft.client.resources.SimpleResource;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SimpleResource.class)
public interface SimpleResourceAccessor {

    @Accessor("srResourceLocation")
    ResourceLocation getResourceLocation();
}
