package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.MCHelper;
import com.cleanroommc.modularui.utils.Platform;

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
        BlockPos pos = getPosFromTile(tile);
        PosGuiData data = new PosGuiData(player, pos.getX(), pos.getY(), pos.getZ());
        GuiManager.open(this, data, (EntityPlayerMP) player);
    }

    public void open(EntityPlayer player, int x, int y, int z) {
        Objects.requireNonNull(player);
        PosGuiData data = new PosGuiData(player, x, y, z);
        GuiManager.open(this, data, (EntityPlayerMP) player);
    }

    @SideOnly(Side.CLIENT)
    public <T extends TileEntity & IGuiHolder<PosGuiData>> void openClient(T tile) {
        BlockPos pos = getPosFromTile(tile);
        GuiManager.openFromClient(this, new PosGuiData(MCHelper.getPlayer(), pos.getX(), pos.getY(), pos.getZ()));
    }

    @SideOnly(Side.CLIENT)
    public void openClient(BlockPos pos) {
        Objects.requireNonNull(pos);
        GuiManager.openFromClient(this, new PosGuiData(MCHelper.getPlayer(), pos.getX(), pos.getY(), pos.getZ()));
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

    public static BlockPos getPosFromTile(TileEntity tile) {
        Objects.requireNonNull(tile);
        if (tile.isInvalid()) {
            throw new IllegalArgumentException("Can't open invalid TileEntity GUI!");
        }
        if (Platform.getClientPlayer().world != tile.getWorldObj()) {
            throw new IllegalArgumentException("TileEntity must be in same dimension as the player!");
        }
        return tile.getPos();
    }
}
