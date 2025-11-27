package com.cleanroommc.modularui.factory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * See {@link GuiData} for an explanation for what this is for.
 */
public class SidedPosGuiData extends PosGuiData {

    private final ForgeDirection side;

    public SidedPosGuiData(@NotNull EntityPlayer player, int x, int y, int z, @NotNull ForgeDirection side) {
        super(player, x, y, z);
        this.side = Objects.requireNonNull(side);
    }

    @NotNull
    public ForgeDirection getSide() {
        return this.side;
    }
}
