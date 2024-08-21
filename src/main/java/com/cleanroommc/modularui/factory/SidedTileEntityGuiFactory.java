package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.api.IGuiHolder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SidedTileEntityGuiFactory extends AbstractUIFactory<SidedPosGuiData> {

    public static final SidedTileEntityGuiFactory INSTANCE = new SidedTileEntityGuiFactory();

    public static <T extends TileEntity & IGuiHolder<SidedPosGuiData>> void open(EntityPlayer player, T tile, ForgeDirection side) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(tile);
        Objects.requireNonNull(side);
        if (tile.isInvalid()) {
            throw new IllegalArgumentException("Can't open invalid TileEntity GUI!");
        }
        if (player.worldObj != tile.getWorldObj()) {
            throw new IllegalArgumentException("TileEntity must be in same dimension as the player!");
        }
        SidedPosGuiData data = new SidedPosGuiData(player, tile.xCoord, tile.yCoord, tile.zCoord, side);
        GuiManager.open(INSTANCE, data, (EntityPlayerMP) player);
    }

    public static void open(EntityPlayer player, int x, int y, int z, ForgeDirection side) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(side);
        SidedPosGuiData data = new SidedPosGuiData(player, x, y, z, side);
        GuiManager.open(INSTANCE, data, (EntityPlayerMP) player);
    }

    private SidedTileEntityGuiFactory() {
        super("mui:sided_tile");
    }

    @Override
    public @NotNull IGuiHolder<SidedPosGuiData> getGuiHolder(SidedPosGuiData data) {
        return Objects.requireNonNull(castGuiHolder(data.getTileEntity()), "Found TileEntity is not a gui holder!");
    }

    @Override
    public void writeGuiData(SidedPosGuiData guiData, PacketBuffer buffer) {
        buffer.writeVarIntToBuffer(guiData.getX());
        buffer.writeVarIntToBuffer(guiData.getY());
        buffer.writeVarIntToBuffer(guiData.getZ());
        buffer.writeByte(guiData.getSide().ordinal());
    }

    @Override
    public @NotNull SidedPosGuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
        return new SidedPosGuiData(player, buffer.readVarIntFromBuffer(), buffer.readVarIntFromBuffer(), buffer.readVarIntFromBuffer(), ForgeDirection.getOrientation(buffer.readByte()));
    }
}
