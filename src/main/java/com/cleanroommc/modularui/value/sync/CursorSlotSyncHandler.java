package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.network.NetworkUtils;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;

public class CursorSlotSyncHandler extends SyncHandler {

    public void sync() {
        sync(0, buffer -> NetworkUtils.writeItemStack(buffer, getSyncManager().getPlayer().inventory.getItemStack()));
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {
        getSyncManager().getPlayer().inventory.setItemStack(NetworkUtils.readItemStack(buf));
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        getSyncManager().getPlayer().inventory.setItemStack(NetworkUtils.readItemStack(buf));
    }
}
