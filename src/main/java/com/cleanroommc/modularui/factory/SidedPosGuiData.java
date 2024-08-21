package com.cleanroommc.modularui.factory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.ForgeDirection;

public class SidedPosGuiData extends PosGuiData {

    private final ForgeDirection side;

    public SidedPosGuiData(EntityPlayer player, int x, int y, int z, ForgeDirection side) {
        super(player, x, y, z);
        this.side = side;
    }

    public ForgeDirection getSide() {
        return this.side;
    }
}
