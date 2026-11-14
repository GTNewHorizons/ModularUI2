package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.ISyncedAction;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.utils.item.IItemHandler;
import com.cleanroommc.modularui.utils.item.PlayerInvWrapper;
import com.cleanroommc.modularui.utils.item.PlayerMainInvWrapper;
import com.cleanroommc.modularui.utils.item.SlotItemHandler;
import com.cleanroommc.modularui.widgets.slot.PlayerSlotGroup;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ModularSyncManager implements ISyncRegistrar<ModularSyncManager> {

    public static final String AUTO_SYNC_PREFIX = "auto_sync:";

    private static final String CURSOR_KEY = ISyncRegistrar.makeSyncKey("cursor_slot", 255255);

    private final Map<String, PanelSyncManager> panelSyncManagerMap = new Object2ObjectOpenHashMap<>();
    // A set of all panels which have been opened during the ui. May also contain closed panels.
    // This is used to detect if packets are arriving too late
    private final Set<String> panelHistory = new ObjectOpenHashSet<>();
    private PanelSyncManager mainPSM;
    private ModularContainer container;
    private final CursorSlotSyncHandler cursorSlotSyncHandler = new CursorSlotSyncHandler();
    private final boolean client;
    private State state = State.INIT;

    public ModularSyncManager(boolean client) {
        this.client = client;
    }

    void setMainPSM(PanelSyncManager mainPSM) {
        this.mainPSM = mainPSM;
    }

    /**
     * Binds this sync manager to its container, binds the player inventory to the main panel if not yet bound,
     * registers the cursor slot sync handler, and opens the main panel.
     *
     * @param container     the container this sync manager belongs to
     * @param mainPanelName name of the main panel to open
     */
    @ApiStatus.Internal
    public void construct(ModularContainer container, String mainPanelName) {
        this.container = container;
        if (this.mainPSM.getSlotGroup(PlayerSlotGroup.NAME) == null) {
            this.mainPSM.bindPlayerInventory(getPlayer());
        }
        this.mainPSM.syncValue(CURSOR_KEY, this.cursorSlotSyncHandler);
        open(mainPanelName, this.mainPSM);
    }

    public PanelSyncManager getMainPSM() {
        return mainPSM;
    }

    public boolean isClient() {
        return this.client;
    }

    @ApiStatus.Internal
    public void detectAndSendChanges(boolean init) {
        this.panelSyncManagerMap.values().forEach(psm -> psm.detectAndSendChanges(init));
    }

    /**
     * Disposes all panel sync managers and the container. Does nothing if already disposed.
     *
     * @throws IllegalStateException if this sync manager has not been closed yet
     */
    @ApiStatus.Internal
    public void dispose() {
        if (isDisposed()) return;
        if (!isClosed()) throw new IllegalStateException("Sync manager must be closed before disposing!");
        this.panelSyncManagerMap.values().forEach(PanelSyncManager::onClose);
        this.panelSyncManagerMap.clear();
        this.container.onModularContainerDisposed();
        setState(State.DISPOSED);
    }

    /**
     * Opens all panel sync managers and notifies the container. Does nothing if already open.
     *
     * @throws IllegalStateException if this sync manager has been disposed or is not yet constructed
     */
    @ApiStatus.Internal
    public void onOpen() {
        if (isOpen()) return;
        if (isDisposed()) throw new IllegalStateException("Can't open sync manager after it has been disposed!");
        if (this.container == null) {
            throw new IllegalStateException("Sync Manager can't be opened when its not yet constructed. ModularContainer is null.");
        }
        setState(State.OPEN);
        this.panelSyncManagerMap.values().forEach(PanelSyncManager::onOpen);
        this.container.onModularContainerOpened();
    }

    /**
     * Notifies the container of closing and sets the state to closed.
     *
     * @throws IllegalStateException if this sync manager is not open
     */
    @ApiStatus.Internal
    public void onClose() {
        if (!isOpen()) throw new IllegalStateException();
        this.container.onModularContainerClosed();
        setState(State.CLOSED);
    }

    public void onUpdate() {
        this.panelSyncManagerMap.values().forEach(PanelSyncManager::onUpdate);
    }

    public PanelSyncManager getPanelSyncManager(String panelName) {
        PanelSyncManager psm = this.panelSyncManagerMap.get(panelName);
        if (psm != null) return psm;
        throw new NullPointerException("No PanelSyncManager found for name '" + panelName + "'!");
    }

    public @Nullable SyncHandler<?> getSyncHandler(String panelName, String syncKey) {
        return getPanelSyncManager(panelName).getSyncHandlerFromMapKey(syncKey);
    }

    public SlotGroup getSlotGroup(String panelName, String slotGroupName) {
        return getPanelSyncManager(panelName).getSlotGroup(slotGroupName);
    }

    public ItemStack getCursorItem() {
        return getPlayer().inventory.getItemStack();
    }

    public void setCursorItem(ItemStack item) {
        getPlayer().inventory.setItemStack(item);
        this.cursorSlotSyncHandler.sync();
    }

    /**
     * Registers a panel sync manager under the given name, records it in the panel history and initializes it.
     *
     * @param name        the panel name
     * @param syncManager the panel sync manager to register
     */
    @ApiStatus.Internal
    public void open(String name, PanelSyncManager syncManager) {
        this.panelSyncManagerMap.put(name, syncManager);
        this.panelHistory.add(name);
        syncManager.initialize(name);
    }

    /**
     * Removes the panel sync manager with the given name and closes it, if present.
     *
     * @param name the panel name
     */
    @ApiStatus.Internal
    public void close(String name) {
        PanelSyncManager psm = this.panelSyncManagerMap.remove(name);
        if (psm != null) psm.onClose();
    }

    public boolean isOpen(String panelName) {
        return this.panelSyncManagerMap.containsKey(panelName);
    }

    /**
     * Forwards an incoming widget update packet to the matching panel. If the panel is not open but was opened
     * earlier during this UI session, the packet is discarded silently, since it arrived after the panel closed.
     * If the panel was never opened, an exception is logged.
     *
     * @param panelName the target panel name
     * @param mapKey    the sync key of the target sync handler
     * @param action    whether this is a synced action (true) or a value update (false)
     * @param id        the id of the target widget or sync handler
     * @param buf       the packet payload
     * @throws IOException if reading the payload fails
     */
    @ApiStatus.Internal
    public void receiveWidgetUpdate(String panelName, String mapKey, boolean action, int id, PacketBuffer buf) throws IOException {
        PanelSyncManager psm = this.panelSyncManagerMap.get(panelName);
        if (psm != null) {
            psm.receiveWidgetUpdate(mapKey, action, id, buf);
        } else if (!this.panelHistory.contains(panelName)) {
            ModularUI.LOGGER.throwing(new IllegalStateException("A packet was send to panel '" + panelName + "' which was not opened yet!."));
        }
        // else the panel was open at some point
        // we simply discard the packet silently and assume the packet was correctly send, but the panel closed earlier
    }

    public EntityPlayer getPlayer() {
        return this.container.getPlayer();
    }

    public ModularContainer getContainer() {
        return container;
    }

    /**
     * Checks whether the slot belongs to the player's main inventory (hotbar and storage, indices 0-35).
     *
     * @param slot the slot to check, may be null
     * @return true if the slot is a player inventory slot in that index range, false otherwise
     */
    private static boolean isPlayerSlot(Slot slot) {
        if (slot == null) return false;
        if (slot.inventory instanceof InventoryPlayer) {
            return slot.getSlotIndex() >= 0 && slot.getSlotIndex() < 36;
        }
        if (slot instanceof SlotItemHandler slotItemHandler) {
            IItemHandler iItemHandler = slotItemHandler.getItemHandler();
            if (iItemHandler instanceof PlayerMainInvWrapper || iItemHandler instanceof PlayerInvWrapper) {
                return slot.getSlotIndex() >= 0 && slot.getSlotIndex() < 36;
            }
        }
        return false;
    }

    @Override
    public boolean hasSyncHandler(SyncHandler<?> syncHandler) {
        return this.mainPSM.hasSyncHandler(syncHandler);
    }

    @Override
    public ModularSyncManager syncValue(String name, int id, SyncHandler<?> syncHandler) {
        this.mainPSM.syncValue(name, id, syncHandler);
        return this;
    }

    @Override
    public IPanelHandler syncedPanel(String key, boolean subPanel, PanelSyncHandler.IPanelBuilder panelBuilder) {
        return this.mainPSM.syncedPanel(key, subPanel, panelBuilder);
    }

    @Override
    public @Nullable IPanelHandler findPanelHandlerNullable(String key) {
        return this.mainPSM.findPanelHandlerNullable(key);
    }

    @Override
    public ModularSyncManager registerSlotGroup(SlotGroup slotGroup) {
        this.mainPSM.registerSlotGroup(slotGroup);
        return this;
    }

    @Override
    public ModularSyncManager registerSyncedAction(String mapKey, boolean executeClient, boolean executeServer, ISyncedAction action) {
        this.mainPSM.registerSyncedAction(mapKey, executeClient, executeServer, action);
        return this;
    }

    @Override
    public <T extends SyncHandler<?>> T getOrCreateSyncHandler(String name, int id, Class<T> clazz, Supplier<T> supplier) {
        return this.mainPSM.getOrCreateSyncHandler(name, id, clazz, supplier);
    }

    @Override
    public @Nullable SyncHandler<?> findSyncHandlerNullable(String name, int id) {
        return this.mainPSM.findSyncHandlerNullable(name, id);
    }

    @Override
    public SlotGroup getSlotGroup(String name) {
        return this.mainPSM.getSlotGroup(name);
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    public static String makeSyncKey(String name, int id) {
        return ISyncRegistrar.makeSyncKey(name, id);
    }

    public boolean isOpen() {
        return this.state == State.OPEN;
    }

    public boolean isClosed() {
        return this.state == State.CLOSED || this.state == State.DISPOSED;
    }

    public boolean isDisposed() {
        return this.state == State.DISPOSED;
    }

    private void setState(State state) {
        this.state = state;
    }

    enum State {
        INIT, OPEN, CLOSED, DISPOSED
    }
}
