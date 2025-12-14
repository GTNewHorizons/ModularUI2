package com.cleanroommc.modularui.core.mixinplugin;

import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;
import org.jetbrains.annotations.NotNull;

public enum Mixins implements IMixins {

    MINECRAFT(new MixinBuilder()
            .addClientMixins(
                    "forge.ForgeHooksClientMixin",
                    "minecraft.FontRendererAccessor",
                    "minecraft.GuiAccessor",
                    "minecraft.GuiButtonMixin",
                    "minecraft.GuiContainerAccessor",
                    "minecraft.GuiContainerMixin",
                    "minecraft.GuiScreenAccessor",
                    "minecraft.GuiScreenMixin",
                    "minecraft.MinecraftMixin",
                    "minecraft.SimpleResourceAccessor")
            .addCommonMixins(
                    "minecraft.ContainerAccessor",
                    "minecraft.EntityAccessor",
                    "minecraft.EntityPlayerMPMixin",
                    "minecraft.InventoryCraftingAccessor",
                    "forge.SimpleNetworkWrapperMixin")
            .setPhase(Phase.EARLY)),
    THAUMCRAFT(new MixinBuilder()
            .addClientMixins("thaumcraft.ClientTickEventsFMLMixin")
            .setPhase(Phase.LATE)
            .addRequiredMod(TargetedMod.THAUMCRAFT)),
    NEI(new MixinBuilder()
            .addCommonMixins("nei.RecipeInfoMixin")
            .setPhase(Phase.LATE)
            .addRequiredMod(TargetedMod.NEI));

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
