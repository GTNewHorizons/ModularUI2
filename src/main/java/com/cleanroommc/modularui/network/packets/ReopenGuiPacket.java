package com.cleanroommc.modularui.network.packets;

import com.cleanroommc.modularui.network.IPacket;
import com.cleanroommc.modularui.network.ModularNetwork;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class ReopenGuiPacket implements IPacket {

    private int networkId;

    public ReopenGuiPacket() {}

    public ReopenGuiPacket(int networkId) {
        this.networkId = networkId;
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeInt(networkId);
    }

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.networkId = buf.readInt();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public @Nullable IPacket executeClient(NetHandlerPlayClient handler) {
        ModularNetwork.CLIENT.reopen(Minecraft.getMinecraft().thePlayer, this.networkId, false);
        return null;
    }

    @Override
    public @Nullable IPacket executeServer(NetHandlerPlayServer handler) {
        ModularNetwork.SERVER.reopen(handler.playerEntity, this.networkId, false);
        return null;
    }
}
