package com.cleanroommc.modularui.future;

import com.cleanroommc.modularui.api.IItemStackLong;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;

/**
 * Exposes the player inventory WITHOUT the armor inventory as IItemHandler. Also takes care of inserting/extracting
 * having the same logic as picking up items.
 */
public class PlayerMainInvWrapper extends RangedWrapper {

    private final InventoryPlayer inventoryPlayer;

    public PlayerMainInvWrapper(InventoryPlayer inv) {
        super(new InvWrapper(inv), 0, inv.mainInventory.length);
        inventoryPlayer = inv;
    }

    @Override
    public IItemStackLong insertItem(int slot, IItemStackLong stack, boolean simulate) {
        IItemStackLong rest = super.insertItem(slot, stack, simulate);
        if (rest == null || rest.getStackSize() != stack.getStackSize()) {
            // the stack in the slot changed, animate it
            IItemStackLong inSlot = getStackInSlot(slot);
            if (inSlot != null) {
                if (getInventoryPlayer().player.worldObj.isRemote) {
                    if (inSlot != null && inSlot.getAsItemStack() != null) {
                        inSlot.getAsItemStack().animationsToGo = 5;
                    }
                } else if (getInventoryPlayer().player instanceof EntityPlayerMP) {
                    getInventoryPlayer().player.openContainer.detectAndSendChanges();
                }
            }
        }
        return rest;
    }

    public InventoryPlayer getInventoryPlayer() {
        return inventoryPlayer;
    }
}