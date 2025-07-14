package com.cleanroommc.modularui.mixinplugin;

import com.gtnewhorizon.gtnhmixins.builders.ITargetMod;
import com.gtnewhorizon.gtnhmixins.builders.TargetModBuilder;
import org.jetbrains.annotations.NotNull;

public enum TargetedMod implements ITargetMod {

    THAUMCRAFT("Thaumcraft");

    private final TargetModBuilder builder;

    TargetedMod(String modId) {
        this.builder = new TargetModBuilder().setModId(modId);
    }

    @NotNull
    @Override
    public TargetModBuilder getBuilder() {
        return builder;
    }
}
