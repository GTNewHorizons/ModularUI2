package com.cleanroommc.modularui.future;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public class PlayerArmorInvWrapper extends RangedWrapper {
    private final InventoryPlayer inventoryPlayer;

    public PlayerArmorInvWrapper(InventoryPlayer inv) {
        super(new InvWrapper(inv), inv.mainInventory.length, inv.mainInventory.length + inv.armorInventory.length);
        inventoryPlayer = inv;
    }

    @Override
    @Nullable
    public ItemStack insertItem(int slot, @Nullable ItemStack stack, boolean simulate) {
        // check if it's valid for the armor slot
        if (slot >= 0 && slot <= 3 && stack != null && stack.getItem().isValidArmor(stack, slot, getInventoryPlayer().player)) {
            return super.insertItem(slot, stack, simulate);
        }
        return stack;
    }

    public InventoryPlayer getInventoryPlayer() {
        return inventoryPlayer;
    }
}
