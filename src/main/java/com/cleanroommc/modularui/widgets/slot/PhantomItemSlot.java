package com.cleanroommc.modularui.widgets.slot;

import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerGhostIngredientSlot;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.value.sync.PhantomItemSlotSH;
import com.cleanroommc.modularui.value.sync.SyncHandler;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public class PhantomItemSlot extends ItemSlot implements RecipeViewerGhostIngredientSlot<ItemStack> {

    private PhantomItemSlotSH syncHandler;

    @Override
    public void onInit() {
        super.onInit();
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        this.syncHandler = castIfTypeElseNull(syncHandler, PhantomItemSlotSH.class);
        return this.syncHandler != null && super.isValidSyncHandler(syncHandler);
    }

    @Override
    protected void drawOverlay() {
        // in 1.12 this draws a green overlay if there is a JEI ghost dragging ingredient
        super.drawOverlay();
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        MouseData mouseData = MouseData.create(mouseButton);
        this.syncHandler.syncToServer(PhantomItemSlotSH.SYNC_CLICK, mouseData::writeToPacket);
        return Result.SUCCESS;
    }

    @Override
    public boolean onMouseRelease(int mouseButton) {
        return true;
    }

    @Override
    public boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int amount) {
        MouseData mouseData = MouseData.create(scrollDirection.modifier);
        this.syncHandler.syncToServer(PhantomItemSlotSH.SYNC_SCROLL, mouseData::writeToPacket);
        return true;
    }

    @Override
    public void onMouseDrag(int mouseButton, long timeSinceClick) {
        // TODO custom drag impl
    }

    @Override
    public boolean handleDragAndDrop(@NotNull ItemStack draggedStack, int button) {
        if (!areAncestorsEnabled() || !this.syncHandler.isItemValid(draggedStack)) return false;
        this.syncHandler.updateFromClient(draggedStack, button);
        draggedStack.stackSize = 0;
        return true;
    }

    @Override
    @NotNull
    public PhantomItemSlotSH getSyncHandler() {
        if (this.syncHandler == null) {
            throw new IllegalStateException("Widget is not initialised!");
        }
        return syncHandler;
    }

    @Override
    public PhantomItemSlot slot(ModularSlot slot) {
        slot.slotNumber = -1;
        this.syncHandler = new PhantomItemSlotSH(slot);
        super.isValidSyncHandler(this.syncHandler);
        setSyncHandler(this.syncHandler);
        return this;
    }

    @Override
    public boolean handleAsVanillaSlot() {
        return false;
    }
}
