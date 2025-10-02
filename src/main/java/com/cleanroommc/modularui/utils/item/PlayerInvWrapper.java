package com.cleanroommc.modularui.utils.item;

import net.minecraft.entity.player.InventoryPlayer;

public class PlayerInvWrapper extends CombinedInvWrapper {

    public PlayerInvWrapper(InventoryPlayer inv) {
        super(new PlayerMainInvWrapper(inv), new PlayerArmorInvWrapper(inv));
    }
}
