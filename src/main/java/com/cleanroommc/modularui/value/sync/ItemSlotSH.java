package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.utils.item.ItemHandlerHelper;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.PlayerSlotType;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Wraps a slot and handles interactions for phantom slots.
 * Use {@link ModularSlot} directly.
 */
public class ItemSlotSH extends SyncHandler<ItemSlotSH> {

    public static final int SYNC_ITEM = 0;
    public static final int SYNC_ENABLED = 1;

    private final ModularSlot slot;
    private final PlayerSlotType playerSlotType;
    private ItemStack lastStoredItem;
    private boolean registered = false;

    /**
     * Creates a sync handler for the given slot. Client packets are allowed.
     *
     * @param slot the slot to sync
     */
    public ItemSlotSH(ModularSlot slot) {
        this.slot = slot;
        this.playerSlotType = PlayerSlotType.getPlayerSlotType(slot);
        allowC2S();
    }

    /**
     * Initializes the slot on first init, registers it in the container and stores the current stack.
     *
     * @param key         the key this sync handler is registered under
     * @param syncHandler the panel sync manager
     */
    @Override
    public void init(String key, PanelSyncManager syncHandler) {
        super.init(key, syncHandler);
        if (!registered) {
            this.slot.initialize(this, isPhantom());
            getSyncManager().getContainer().registerSlot(getSyncManager().getPanelName(), this.slot);
            this.registered = true;
        }
        ItemStack currentStack = getSlot().getStack();
        this.lastStoredItem = currentStack != null ? currentStack.copy() : null;
    }

    /**
     * Disposes the slot in addition to the base cleanup.
     */
    @Override
    public void dispose() {
        super.dispose();
        this.slot.dispose();
    }

    /**
     * Checks the slot for changes and syncs them to the client.
     *
     * @param init true if this is the initial sync
     */
    @Override
    public void detectAndSendChanges(boolean init) {
        checkUpdate(init);
    }

    /**
     * Checks the slot for changes on the server and syncs them if any were found.
     */
    public void checkUpdate() {
        checkUpdate(false);
    }

    /**
     * Compares the current stack with the last stored one and syncs the slot if it changed.
     * If only the stack size changed, only the size is updated in the cache.
     *
     * @param init true if this is the initial sync
     */
    private void checkUpdate(boolean init) {
        if (!isValid() || getSyncManager().isClient()) return;
        ItemStack itemStack = getSlot().getStack();
        if (itemStack == null && this.lastStoredItem == null) return;
        boolean onlyAmountChanged = false;
        if (init ||
                !ItemHandlerHelper.canItemStacksStack(this.lastStoredItem, itemStack) ||
                (onlyAmountChanged = itemStack.stackSize != this.lastStoredItem.stackSize)) {
            onSlotUpdate(itemStack, onlyAmountChanged, false, init);
            if (onlyAmountChanged) {
                this.lastStoredItem.stackSize = itemStack.stackSize;
            } else {
                this.lastStoredItem = itemStack == null ? null : itemStack.copy();
            }
            final boolean finalOnlyAmountChanged = onlyAmountChanged;
            final boolean forceSync = false;
            syncToClient(SYNC_ITEM, buffer -> {
                buffer.writeBoolean(finalOnlyAmountChanged);
                NetworkUtils.writeItemStack(buffer, itemStack);
                buffer.writeBoolean(init);
                buffer.writeBoolean(forceSync);
            });
        }
    }

    /**
     * Applies a sync packet on the client. Handles item updates and enabled state changes.
     *
     * @param id  the packet id ({@link #SYNC_ITEM} or {@link #SYNC_ENABLED})
     * @param buf the packet data
     */
    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        if (id == SYNC_ITEM) {
            boolean onlyAmountChanged = buf.readBoolean();
            this.lastStoredItem = NetworkUtils.readItemStack(buf);
            onSlotUpdate(this.lastStoredItem, onlyAmountChanged, true, buf.readBoolean());
            if (buf.readBoolean()) {
                // force sync
                this.slot.putStack(this.lastStoredItem);
            }
        } else if (id == SYNC_ENABLED) {
            setEnabled(buf.readBoolean(), false);
        }
    }

    /**
     * Applies a sync packet on the server. Only enabled state changes are accepted.
     *
     * @param id  the packet id ({@link #SYNC_ENABLED})
     * @param buf the packet data
     * @throws IOException if reading the packet fails
     */
    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        if (id == SYNC_ENABLED) {
            setEnabled(buf.readBoolean(), false);
        }
    }

    /**
     * Forwards a slot change to the slot.
     *
     * @param stack             the new stack
     * @param onlyAmountChanged true if only the stack size changed
     * @param client            true if this runs on the client
     * @param init              true if this is the initial sync
     */
    protected void onSlotUpdate(ItemStack stack, boolean onlyAmountChanged, boolean client, boolean init) {
        getSlot().onSlotChangedReal(stack, onlyAmountChanged, client, init);
    }

    /**
     * Enables or disables the slot and optionally syncs the new state.
     *
     * @param enabled true to enable the slot
     * @param sync    true to send the state to the other side
     */
    public void setEnabled(boolean enabled, boolean sync) {
        this.slot.setEnabled(enabled);
        if (sync) {
            sync(SYNC_ENABLED, buffer -> buffer.writeBoolean(enabled));
        }
    }

    /**
     * Forces the current slot stack to be sent to the client, even if it did not change.
     */
    public void forceSyncItem() {
        boolean onlyAmountChanged = false;
        ItemStack stack = slot.getStack();
        boolean init = false;
        boolean forceSync = true;
        onSlotUpdate(stack, onlyAmountChanged, getSyncManager().isClient(), init);
        this.lastStoredItem = stack;
        syncToClient(SYNC_ITEM, buffer -> {
            buffer.writeBoolean(onlyAmountChanged);
            NetworkUtils.writeItemStack(buffer, stack);
            buffer.writeBoolean(init);
            buffer.writeBoolean(forceSync);
        });
    }

    public ModularSlot getSlot() {
        return this.slot;
    }

    /**
     * Checks whether the slot accepts the given stack.
     *
     * @param itemStack the stack to check
     * @return true if the slot accepts the stack
     */
    public boolean isItemValid(ItemStack itemStack) {
        return getSlot().isItemValid(itemStack);
    }

    /**
     * Phantom slots do not hold real items. Subclasses override this.
     *
     * @return false by default
     */
    public boolean isPhantom() {
        return false;
    }

    public @Nullable PlayerSlotType getPlayerSlotType() {
        return this.playerSlotType;
    }

    public boolean isPlayerSlot() {
        return playerSlotType != null;
    }

    @Nullable
    public String getSlotGroup() {
        return this.slot.getSlotGroupName();
    }
}
