package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.factory.ClientGUI;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class EventHandler {

    @SubscribeEvent
    public void onItemUse(PlayerInteractEvent event) {
        ItemStack heldItem = event.entityPlayer.getHeldItem();
        if (event.entityPlayer.getEntityWorld().isRemote && heldItem != null && heldItem.getItem() == Items.diamond) {
            //GuiManager.openClientUI(Minecraft.getMinecraft().player, new TestGui());
            /*HoloUI.builder()
                    .inFrontOf(Minecraft.getMinecraft().thePlayer, 5, false)
                    .screenScale(0.5f)
                    .open(new TestGui());*/
            //ClientGUI.open(new ResizerTest());
            ClientGUI.open(new TestGuis());
        }
    }
}
