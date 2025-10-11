package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.utils.Platform;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SidedTileEntityGuiFactory extends AbstractUIFactory<SidedPosGuiData> {

    public static final SidedTileEntityGuiFactory INSTANCE = new SidedTileEntityGuiFactory();

    public <T extends TileEntity & IGuiHolder<SidedPosGuiData>> void open(EntityPlayer player, T tile, ForgeDirection facing) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(facing);
        TileEntityGuiFactory.verifyTile(player, tile);
        SidedPosGuiData data = new SidedPosGuiData(player, tile.xCoord, tile.yCoord, tile.zCoord, facing);
        GuiManager.open(this, data, (EntityPlayerMP) player);
    }

    public void open(EntityPlayer player, int x, int y, int z, ForgeDirection facing) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(facing);
        SidedPosGuiData data = new SidedPosGuiData(player, x, y, z, facing);
        GuiManager.open(this, data, (EntityPlayerMP) player);
    }

    @SideOnly(Side.CLIENT)
    public <T extends TileEntity & IGuiHolder<SidedPosGuiData>> void openClient(T tile, ForgeDirection facing) {
        Objects.requireNonNull(facing);
        TileEntityGuiFactory.verifyTile(Platform.getClientPlayer(), tile);
        SidedPosGuiData data = new SidedPosGuiData(Platform.getClientPlayer(), tile.xCoord, tile.yCoord, tile.zCoord, facing);
        GuiManager.openFromClient(this, data);
    }

    @SideOnly(Side.CLIENT)
    public void openClient(int x, int y, int z, ForgeDirection facing) {
        Objects.requireNonNull(facing);
        SidedPosGuiData data = new SidedPosGuiData(Platform.getClientPlayer(), x, y, z, facing);
        GuiManager.openFromClient(this, data);
    }

    private SidedTileEntityGuiFactory() {
        super("mui:sided_tile");
    }

    @Override
    public @NotNull IGuiHolder<SidedPosGuiData> getGuiHolder(SidedPosGuiData data) {
        return Objects.requireNonNull(castGuiHolder(data.getTileEntity()), "Found TileEntity is not a gui holder!");
    }

    @Override
    public boolean canInteractWith(EntityPlayer player, SidedPosGuiData guiData) {
        return player == guiData.getPlayer() && guiData.getTileEntity() != null && guiData.getSquaredDistance(player) <= 64;
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
