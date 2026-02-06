package com.cleanroommc.modularui.network;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.network.packets.CloseAllGuiPacket;
import com.cleanroommc.modularui.network.packets.CloseGuiPacket;
import com.cleanroommc.modularui.network.packets.OpenGuiPacket;
import com.cleanroommc.modularui.network.packets.PacketSyncHandler;
import com.cleanroommc.modularui.network.packets.ReopenGuiPacket;
import com.cleanroommc.modularui.network.packets.SClipboard;
import com.cleanroommc.modularui.network.packets.SyncConfig;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class NetworkHandler {

    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(ModularUI.ID);
    private static int packetId = 0;

    public static void init() {
        registerS2C(SClipboard.class);

        registerC2S(SyncConfig.class);

        registerBoth(OpenGuiPacket.class);
        registerBoth(ReopenGuiPacket.class);
        registerBoth(CloseGuiPacket.class);
        registerBoth(CloseAllGuiPacket.class);
        registerBoth(PacketSyncHandler.class);
    }

    private static void registerC2S(Class<? extends IPacket> clazz) {
        CHANNEL.registerMessage(C2SHandler, clazz, packetId++, Side.SERVER);
    }

    private static void registerS2C(Class<? extends IPacket> clazz) {
        CHANNEL.registerMessage(S2CHandler, clazz, packetId++, Side.CLIENT);
    }

    private static void registerBoth(Class<? extends IPacket> clazz) {
        registerS2C(clazz);
        registerC2S(clazz);
    }

    public static void sendToServer(IPacket packet) {
        CHANNEL.sendToServer(packet);
    }

    public static void sendToWorld(IPacket packet, World world) {
        CHANNEL.sendToDimension(packet, world.provider.dimensionId);
    }

    public static void sendToPlayer(IPacket packet, EntityPlayerMP player) {
        CHANNEL.sendTo(packet, player);
    }

    final static IMessageHandler<IPacket, IPacket> S2CHandler = (message, ctx) -> {
        NetHandlerPlayClient handler = ctx.getClientHandler();
        return message.executeClient(handler);
    };
    final static IMessageHandler<IPacket, IPacket> C2SHandler = (message, ctx) -> {
        NetHandlerPlayServer handler = ctx.getServerHandler();
        return message.executeServer(handler);
    };
}
