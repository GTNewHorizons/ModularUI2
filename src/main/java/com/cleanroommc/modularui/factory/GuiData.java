package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.api.NEISettings;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.NEISettingsImpl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.Objects;

/**
 * This class and subclasses are holding necessary data to find the exact same GUI on client and server.
 * For example, if the GUI was opened by right-clicking a TileEntity, then this data needs a world and a block pos.
 * Additionally, this can be used to configure NEI via {@link #getNEISettings()}.
 * <p>
 * Also see {@link PosGuiData} (useful for TileEntities), {@link SidedPosGuiData} (useful for covers from GregTech)
 * for default implementations.
 * </p>
 */
public class GuiData {

    private final EntityPlayer player;
    private NEISettings neiSettings;

    public GuiData(EntityPlayer player) {
        this.player = Objects.requireNonNull(player);
    }

    public EntityPlayer getPlayer() {
        return this.player;
    }

    public boolean isClient() {
        return NetworkUtils.isClient(this.player);
    }

    public ItemStack getMainHandItem() {
        return this.player.getHeldItem();
    }

    public NEISettings getNEISettings() {
        if (this.neiSettings == null) {
            throw new IllegalStateException("Not yet initialised!");
        }
        return this.neiSettings;
    }

    final NEISettingsImpl getNEISettingsImpl() {
        return (NEISettingsImpl) this.neiSettings;
    }

    final void setNEISettings(NEISettings neiSettings) {
        this.neiSettings = Objects.requireNonNull(neiSettings);
    }
}
