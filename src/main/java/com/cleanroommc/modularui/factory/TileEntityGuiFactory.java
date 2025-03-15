package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.api.IGuiHolder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class TileEntityGuiFactory extends AbstractUIFactory<PosGuiData> {

    public static final TileEntityGuiFactory INSTANCE = new TileEntityGuiFactory();

    private TileEntityGuiFactory() {
        super("mui:tile_entity");
    }

    public <T extends TileEntity & IGuiHolder<PosGuiData>> void open(EntityPlayer player, T tile) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(tile);
        if (tile.isInvalid()) {
            throw new IllegalArgumentException("Can't open invalid TileEntity GUI!");
        }
        if (player.worldObj != tile.getWorldObj()) {
            throw new IllegalArgumentException("TileEntity must be in same dimension as the player!");
        }
        PosGuiData data = new PosGuiData(player, tile.xCoord, tile.yCoord, tile.zCoord);
        GuiManager.open(this, data, (EntityPlayerMP) player);
    }

    public void open(EntityPlayer player, int x, int y, int z) {
        Objects.requireNonNull(player);
        PosGuiData data = new PosGuiData(player, x, y, z);
        GuiManager.open(this, data, (EntityPlayerMP) player);
    }

    @Override
    public @NotNull IGuiHolder<PosGuiData> getGuiHolder(PosGuiData data) {
        return Objects.requireNonNull(castGuiHolder(data.getTileEntity()), "Found TileEntity is not a gui holder!");
    }

    @Override
    public boolean canInteractWith(EntityPlayer player, PosGuiData guiData) {
        return player == guiData.getPlayer() && guiData.getTileEntity() != null && guiData.getSquaredDistance(player) <= 64;
    }

    @Override
    public void writeGuiData(PosGuiData guiData, PacketBuffer buffer) {
        buffer.writeVarIntToBuffer(guiData.getX());
        buffer.writeVarIntToBuffer(guiData.getY());
        buffer.writeVarIntToBuffer(guiData.getZ());
    }

    @Override
    public @NotNull PosGuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
        return new PosGuiData(player, buffer.readVarIntFromBuffer(), buffer.readVarIntFromBuffer(), buffer.readVarIntFromBuffer());
    }
}
