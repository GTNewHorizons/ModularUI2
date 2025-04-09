package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.api.IGuiHolder;

import com.gtnewhorizons.modularui.common.internal.network.NetworkUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Like {@link SimpleGuiFactory} but with ItemStack data attached. Unlike {@link ItemGuiFactory}, the item itself
 * is arbitrary and does not need to be an instance of {@link IGuiHolder}, does not need to be in the player's main hand, etc.
 */
public class ItemStackGuiFactory extends AbstractUIFactory<ItemStackGuiData> {

    private final IGuiHolder<ItemStackGuiData> guiHolder;

    public ItemStackGuiFactory(String name, IGuiHolder<ItemStackGuiData> guiHolder) {
        super(name);
        this.guiHolder = guiHolder;
        GuiManager.registerFactory(this);
    }

    @Override
    public @NotNull IGuiHolder<ItemStackGuiData> getGuiHolder(ItemStackGuiData data) {
        return guiHolder;
    }

    @Override
    public void writeGuiData(ItemStackGuiData guiData, PacketBuffer buffer) {
        NetworkUtils.writeItemStack(buffer, guiData.getItemStack());
    }

    @Override
    public @NotNull ItemStackGuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
        return new ItemStackGuiData(player, NetworkUtils.readItemStack(buffer));
    }
}
