package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.ClientProxy;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.factory.PlayerInventoryGuiData;
import com.cleanroommc.modularui.factory.inventory.InventoryType;
import com.cleanroommc.modularui.factory.inventory.InventoryTypes;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.ISimpleBauble;
import com.cleanroommc.modularui.utils.ItemStackItemHandler;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.utils.item.IItemHandlerModifiable;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import baubles.api.expanded.BaubleExpandedSlots;

import java.util.List;

public class TestItem extends Item implements IGuiHolder<PlayerInventoryGuiData>, ISimpleBauble {

    public static final TestItem testItem = new TestItem();

    @Override
    public ModularScreen createScreen(PlayerInventoryGuiData data, ModularPanel mainPanel) {
        return new ModularScreen(ModularUI.ID, mainPanel);
    }

    @Override
    public ModularPanel buildUI(PlayerInventoryGuiData guiData, PanelSyncManager guiSyncManager, UISettings settings) {
        IItemHandlerModifiable itemHandler = new ItemStackItemHandler(guiData, 4);
        guiSyncManager.registerSlotGroup("mixer_items", 2);

        // if the player slot is the slot with this item, then disallow any interaction
        // if the item is not in the player inventory (bauble for example), then this items slot is not on the screen, and we don't need to
        // limit accessibility
        if (guiData.getInventoryType() == InventoryTypes.PLAYER) {
            guiSyncManager.bindPlayerInventory(guiData.getPlayer(), (inv, index) -> index == guiData.getSlotIndex() ?
                    new ModularSlot(inv, index).accessibility(false, false) :
                    new ModularSlot(inv, index));
        }
        ModularPanel panel = ModularPanel.defaultPanel("knapping_gui").resizeableOnDrag(true);
        panel.child(new Column().margin(7)
                .child(new ParentWidget<>().widthRel(1f).expanded()
                        .child(SlotGroupWidget.builder()
                                .row("II")
                                .row("II")
                                .key('I', index -> new ItemSlot().slot(SyncHandlers.itemSlot(itemHandler, index)
                                        .ignoreMaxStackSize(true)
                                        .slotGroup("mixer_items")
                                        // do not allow putting items which can hold other items into the item
                                        // some mods don't do this on their backpacks, so it won't catch those cases // TODO 1.7.10
                                        //.filter(stack -> !stack.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
                                ))
                                .build()
                                .align(Alignment.Center)))
                .child(SlotGroupWidget.playerInventory(false)));

        return panel;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player) {
        if (!worldIn.isRemote) {
            GuiFactories.playerInventory().openFromMainHand(player);
        }
        return super.onItemRightClick(itemStackIn, worldIn, player);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean showDebugInfo) {
        super.addInformation(stack, player, tooltip, showDebugInfo);
        tooltip.add("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.");
        tooltip.add(EnumChatFormatting.GREEN + "Press " + Platform.getKeyDisplay(ClientProxy.testKey) + " to open GUI from Baubles");
    }

    @Override
    public String[] getBaubleTypes(ItemStack itemstack) {
        return new String[]{BaubleExpandedSlots.amuletType};
    }
}
