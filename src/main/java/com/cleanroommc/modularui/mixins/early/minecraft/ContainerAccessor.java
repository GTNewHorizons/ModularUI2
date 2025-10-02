package com.cleanroommc.modularui.mixins.early.minecraft;

import net.minecraft.inventory.Container;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Container.class)
public interface ContainerAccessor {

    @Accessor("field_94536_g")
    int getDragEvent();
}
