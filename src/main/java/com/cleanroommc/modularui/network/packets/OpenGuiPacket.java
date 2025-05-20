package com.cleanroommc.modularui.network.packets;

import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.api.UIFactory;
import com.cleanroommc.modularui.network.IPacket;
import com.cleanroommc.modularui.network.NetworkUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class OpenGuiPacket<T extends GuiData> implements IPacket {

    private int windowId;
    private UIFactory<T> factory;
    private PacketBuffer data;

    public OpenGuiPacket() {
    }

    public OpenGuiPacket(int windowId, UIFactory<T> factory, PacketBuffer data) {
        this.windowId = windowId;
        this.factory = factory;
        this.data = data;
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeVarIntToBuffer(this.windowId);
        NetworkUtils.writeStringSafe(buf, this.factory.getFactoryName());
        NetworkUtils.writeByteBuf(buf, this.data);
    }

    @Override
    public void read(PacketBuffer buf) {
        this.windowId = buf.readVarIntFromBuffer();
        this.factory = (UIFactory<T>) GuiManager.getFactory(NetworkUtils.readStringSafe(buf));
        this.data = NetworkUtils.readPacketBuffer(buf);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public @Nullable IPacket executeClient(NetHandlerPlayClient handler) {
        GuiManager.openFromClient(this.windowId, this.factory, this.data, Minecraft.getMinecraft().thePlayer);
        return null;
    }

    @Override
    public @Nullable IPacket executeServer(NetHandlerPlayServer handler) {
        T guiData = this.factory.readGuiData(handler.player, this.data);
        GuiManager.open(this.factory, guiData, handler.player);
        return null;
    }
}
