package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.utils.item.IItemHandlerModifiable;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.ItemStackItemHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Column;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

public class TestItem extends Item implements IGuiHolder<GuiData> {

    public static final TestItem testItem = new TestItem();

    @Override
    public ModularPanel buildUI(GuiData guiData, PanelSyncManager guiSyncManager) {
        IItemHandlerModifiable itemHandler = new ItemStackItemHandler(guiData.getMainHandItem(), 4);
        guiSyncManager.registerSlotGroup("mixer_items", 2);

        ModularPanel panel = ModularPanel.defaultPanel("knapping_gui");
        panel.child(new Column().margin(7)
                .child(new ParentWidget<>().widthRel(1f).expanded()
                        .child(SlotGroupWidget.builder()
                                .row("II")
                                .row("II")
                                .key('I', index -> new ItemSlot().slot(SyncHandlers.itemSlot(itemHandler, index)
                                        .ignoreMaxStackSize(true)
                                        .slotGroup("mixer_items")))
                                .build()
                                .align(Alignment.Center)))
                .child(SlotGroupWidget.playerInventory(0)));

        return panel;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player) {
        if (!worldIn.isRemote) {
            GuiFactories.item().open(player);
        }
        return super.onItemRightClick(itemStackIn, worldIn, player);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean showDebugInfo) {
        super.addInformation(stack, player, tooltip, showDebugInfo);
        tooltip.add("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.");
    }
}
