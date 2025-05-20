package com.cleanroommc.modularui.factory.inventory;

import com.cleanroommc.modularui.ModularUI;

import com.cleanroommc.modularui.utils.item.IItemHandlerModifiable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

import baubles.api.BaublesApi;

public class InventoryTypes {

    public static final InventoryType PLAYER = new Inventory("player") {
        @Override
        public IInventory getInventory(EntityPlayer player) {
            return player.inventory;
        }
    };

    public static final InventoryType BAUBLES = new ItemHandler("baubles") {

        @Override
        public boolean isActive() {
            return ModularUI.isBaubleLoaded();
        }

        @Override
        public IItemHandlerModifiable getInventory(EntityPlayer player) {
            if (isActive()) {
                return BaublesApi.getBaublesHandler(player);
            }
            throw new IllegalArgumentException("Tried to receive bauble item, but bauble is not loaded");
        }
    };
}
