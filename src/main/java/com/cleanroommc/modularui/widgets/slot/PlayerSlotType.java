package com.cleanroommc.modularui.widgets.slot;

import com.cleanroommc.modularui.utils.item.InvWrapper;
import com.cleanroommc.modularui.utils.item.PlayerArmorInvWrapper;
import com.cleanroommc.modularui.utils.item.PlayerInvWrapper;
import com.cleanroommc.modularui.utils.item.PlayerMainInvWrapper;
import com.cleanroommc.modularui.utils.item.SlotItemHandler;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;

public enum PlayerSlotType {
    HOTBAR, MAIN_INVENTORY, ARMOR;

    public static PlayerSlotType getPlayerSlotType(Slot slot) {
        int index = slot.getSlotIndex();
        if (index < 0 || index > 40) return null;
        if (slot instanceof SlotItemHandler slotitemhandler) {
            if (slotitemhandler.getItemHandler() instanceof PlayerMainInvWrapper) {
                return index < 9 ? HOTBAR : MAIN_INVENTORY;
            }
            if (slotitemhandler.getItemHandler() instanceof PlayerArmorInvWrapper) {
                return ARMOR;
            }
            if (!(slotitemhandler.getItemHandler() instanceof PlayerInvWrapper) &&
                    !(slotitemhandler.getItemHandler() instanceof InvWrapper invWrapper && invWrapper.getInv() instanceof InventoryPlayer)) {
                return null;
            }
        } else if (slot instanceof com.gtnewhorizons.modularui.api.forge.SlotItemHandler slotitemhandler) {
            if (slotitemhandler.getItemHandler() instanceof com.gtnewhorizons.modularui.api.forge.PlayerMainInvWrapper) {
                return index < 9 ? HOTBAR : MAIN_INVENTORY;
            }
            if (!(slotitemhandler.getItemHandler() instanceof InvWrapper invWrapper && invWrapper.getInv() instanceof InventoryPlayer)) {
                return null;
            }
        } else if (!(slot.inventory instanceof InventoryPlayer)) {
            return null;
        }
        if (index < 9) return HOTBAR;
        if (index < 36) return MAIN_INVENTORY;
        if (index < 40) return ARMOR;
        return null;
    }
}
