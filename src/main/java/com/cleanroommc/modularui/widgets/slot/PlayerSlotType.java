package com.cleanroommc.modularui.widgets.slot;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;

public enum PlayerSlotType {
    HOTBAR, MAIN_INVENTORY, OFFHAND, ARMOR;

    public static PlayerSlotType getPlayerSlotType(Slot slot) {
        int index = slot.getSlotIndex();
        if (index < 0 || index > 40 || !(slot.inventory instanceof InventoryPlayer)) {
            return null;
        }
        if (index < 9) return HOTBAR;
        if (index < 36) return MAIN_INVENTORY;
        if (index < 40) return ARMOR;
        return OFFHAND;
    }
}
