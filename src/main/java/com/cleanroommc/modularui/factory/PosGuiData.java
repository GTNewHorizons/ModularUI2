package com.cleanroommc.modularui.factory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * See {@link GuiData} for an explanation for what this is for.
 */
public class PosGuiData extends GuiData {

    private final int x, y, z;

    public PosGuiData(EntityPlayer player, int x, int y, int z) {
        super(player);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public World getWorld() {
        return getPlayer().worldObj;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    public TileEntity getTileEntity() {
        return getWorld().getTileEntity(this.x, this.y, this.z);
    }
}
