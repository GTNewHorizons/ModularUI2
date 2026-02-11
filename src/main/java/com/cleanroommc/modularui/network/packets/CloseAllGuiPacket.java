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

public class CloseAllGuiPacket implements IPacket {

    public CloseAllGuiPacket() {}

    @Override
    public void write(PacketBuffer buf) throws IOException {}

    @Override
    public void read(PacketBuffer buf) throws IOException {}

    @SideOnly(Side.CLIENT)
    @Override
    public @Nullable IPacket executeClient(NetHandlerPlayClient handler) {
        ModularNetwork.CLIENT.closeAll(Minecraft.getMinecraft().thePlayer, false);
        return null;
    }

    @Override
    public @Nullable IPacket executeServer(NetHandlerPlayServer handler) {
        ModularNetwork.SERVER.closeAll(handler.playerEntity, false);
        return null;
    }
}
