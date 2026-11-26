package com.cleanroommc.modularui.api;

import net.minecraft.network.PacketBuffer;

import io.netty.buffer.Unpooled;

import java.io.IOException;

/**
 * A functional interface allowing you to write data to the supplied {@link PacketBuffer}.
 */
@FunctionalInterface
public interface IPacketWriter {

    /**
     * Writes any data to a packet buffer
     *
     * @param buffer buffer to write to
     * @throws IOException if data cannot be written for some reason
     */
    void write(PacketBuffer buffer) throws IOException;

    default PacketBuffer toPacket() {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        try {
            write(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return buffer;
    }
}
