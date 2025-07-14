package com.cleanroommc.modularui.mixinplugin;

import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;
import org.jetbrains.annotations.NotNull;

public enum Mixins implements IMixins {

    MINECRAFT(new MixinBuilder()
            .addClientMixins(
                    "minecraft.FontRendererAccessor",
                    "forge.ForgeHooksClientMixin",
                    "minecraft.GuiAccessor",
                    "minecraft.GuiButtonMixin",
                    "minecraft.GuiContainerAccessor",
                    "minecraft.GuiContainerMixin",
                    "minecraft.GuiScreenAccessor",
                    "minecraft.GuiScreenMixin",
                    "minecraft.MinecraftMixin",
                    "minecraft.SimpleResourceAccessor")
            .addCommonMixins(
                    "minecraft.EntityAccessor",
                    "minecraft.ContainerAccessor",
                    "minecraft.InventoryCraftingAccessor",
                    "forge.SimpleNetworkWrapperMixin")
            .setPhase(Phase.EARLY)),
    THAUMCRAFT(new MixinBuilder()
            .addClientMixins("thaumcraft.ClientTickEventsFMLMixin")
            .setPhase(Phase.LATE)
            .addRequiredMod(TargetedMod.THAUMCRAFT));

    private final MixinBuilder builder;

    Mixins(MixinBuilder builder) {
        this.builder = builder;
    }

    @NotNull
    @Override
    public MixinBuilder getBuilder() {
        return builder;
    }
}
