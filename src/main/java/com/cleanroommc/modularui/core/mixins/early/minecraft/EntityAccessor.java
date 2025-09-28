package com.cleanroommc.modularui.core.mixins.early.minecraft;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {

    @Accessor
    void setFirstUpdate(boolean firstUpdate);
}
