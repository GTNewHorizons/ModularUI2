package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.ClientGUI;
import com.cleanroommc.modularui.screen.RichTooltipEvent;
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
            ClientGUI.open(new TestGui());
        }
    }

    @SubscribeEvent
    public void onRichTooltip(RichTooltipEvent.Pre event) {
        event.getTooltip()
                .add(IKey.str("Powered By: ").style(IKey.GOLD, IKey.ITALIC))
                .add(GuiTextures.MUI_LOGO.asIcon().size(18)).newLine();
    }
}
