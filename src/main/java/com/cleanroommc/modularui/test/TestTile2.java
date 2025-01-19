package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ScrollWidget;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import com.cleanroommc.modularui.utils.item.IItemHandlerModifiable;
import com.cleanroommc.modularui.utils.item.ItemStackHandler;

import java.util.Iterator;

public class TestTile2 extends TileEntity implements IGuiHolder<PosGuiData> {

    private static final int SLOT_COUNT = 9999;

    private final IItemHandlerModifiable itemHandler = new ItemStackHandler(SLOT_COUNT);

    public TestTile2() {
        Iterator<Item> it = Item.itemRegistry.iterator();
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (!it.hasNext()) {
                it = Item.itemRegistry.iterator();
            }
            Item item = it.next();
            this.itemHandler.setStackInSlot(i, new ItemStack(item));
        }
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager) {
        ScrollWidget<?> sw = new ScrollWidget<>(new VerticalScrollData()).size(9 * 18).margin(7);
        sw.getScrollArea().getScrollY().setScrollSize(18 * (SLOT_COUNT / 9));
        for (int i = 0; i < SLOT_COUNT; i++) {
            int x = i % 9;
            int y = i / 9;
            sw.child(new ItemSlot().pos(x * 18, y * 18)
                    .slot(new ModularSlot(this.itemHandler, i)));
        }
        return ModularPanel.defaultPanel("test_tile_2", 176, 13 * 18 + 14 + 10)
                .bindPlayerInventory()
                .child(sw);
    }

    @Override
    public void updateEntity() {}
}
