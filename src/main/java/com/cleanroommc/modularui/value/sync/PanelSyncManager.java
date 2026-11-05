package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.ISyncedAction;
import com.cleanroommc.modularui.network.ModularNetwork;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.utils.item.PlayerMainInvWrapper;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2ReferenceArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PanelSyncManager implements ISyncRegistrar<PanelSyncManager> {

    private final Map<String, SyncHandler<?>> syncHandlers = new Object2ReferenceLinkedOpenHashMap<>();
    private final Map<String, SlotGroup> slotGroups = new Object2ReferenceOpenHashMap<>();
    private final Map<SyncHandler<?>, String> reverseSyncHandlers = new Reference2ObjectOpenHashMap<>();
    private final Map<String, SyncedAction> syncedActions = new Object2ReferenceOpenHashMap<>();
    private final Map<String, PanelSyncHandler> subPanels = new Object2ReferenceArrayMap<>();
    private final ModularSyncManager modularSyncManager;
    private String panelName;
    private boolean init = true;
    private boolean locked = false;
    private boolean allowSyncHandlerRegistration = false;
    private final boolean client;

    private final List<Consumer<EntityPlayer>> openListener = new ArrayList<>();
    private final List<Consumer<EntityPlayer>> closeListener = new ArrayList<>();
    private final List<Runnable> tickListener = new ArrayList<>();

    /**
     * Creates a panel sync manager and registers it as the main one if {@code main} is true.
     *
     * @param msm  the modular sync manager this panel belongs to
     * @param main true if this panel is the main panel of the container
     */
    @ApiStatus.Internal
    public PanelSyncManager(ModularSyncManager msm, boolean main) {
        this.modularSyncManager = msm;
        this.client = msm.isClient();
        if (main) msm.setMainPSM(this);
    }

    /**
     * Locks this manager and initializes all registered sync handlers and sub panels under the given panel name.
     *
     * @param panelName the name of the panel
     */
    @ApiStatus.Internal
    public void initialize(String panelName) {
        this.panelName = panelName;
        this.locked = true;
        this.syncHandlers.forEach((mapKey, syncHandler) -> syncHandler.init(mapKey, this));
        this.init = true;
        this.subPanels.forEach((s, syncHandler) -> this.modularSyncManager.getMainPSM().registerPanelSyncHandler(s, syncHandler));
    }

    private void registerPanelSyncHandler(String name, SyncHandler<?> syncHandler) {
        // only called on main psm
        SyncHandler<?> currentSh = this.syncHandlers.get(name);
        if (currentSh != null && currentSh != syncHandler) {
            throw new IllegalStateException("Failed to register panel sync handler during initialization. " +
                    "There already exists a sync handler for the name '" + name + "'.");
        }
        String currentName = this.reverseSyncHandlers.get(syncHandler);
        if (currentName != null && !name.equals(currentName)) {
            throw new IllegalStateException("Failed to register panel sync handler for name '" + name + "' during initialization. " +
                    "The panel sync handler is already registered under the name '" + currentName + "'.");
        }
        this.syncHandlers.put(name, syncHandler);
        this.reverseSyncHandlers.put(syncHandler, name);
        syncHandler.init(name, this);
    }

    /**
     * Closes all sub panels marked as sub panels of this panel.
     */
    void closeSubPanels() {
        this.subPanels.values().forEach(syncHandler -> {
            if (syncHandler.isSubPanel()) {
                syncHandler.closePanel();
            }
        });
    }

    /**
     * Notifies all open listeners. Called when the container is opened.
     */
    @ApiStatus.Internal
    public void onOpen() {
        this.openListener.forEach(listener -> listener.accept(getPlayer()));
    }

    /**
     * Notifies all close listeners and disposes all sync handlers. Called when the container is closed.
     */
    @ApiStatus.Internal
    public void onClose() {
        this.closeListener.forEach(listener -> listener.accept(getPlayer()));
        this.syncHandlers.values().forEach(SyncHandler::dispose);
        // Previously panel sync handlers were removed from the main psm, however this problematic if the screen will be reopened at some
        // point. We can just not remove the sync handlers since mui has proper checks for re-registering panels.
    }

    public boolean isInitialised() {
        return this.panelName != null;
    }

    /**
     * Detects changes in all sync handlers on the server and sends them to the client.
     * Does nothing on the client.
     *
     * @param init true if this is the initial sync
     */
    void detectAndSendChanges(boolean init) {
        if (!isClient()) {
            for (SyncHandler<?> syncHandler : this.syncHandlers.values()) {
                syncHandler.detectAndSendChanges(init || this.init);
            }
        }
        this.init = false;
    }

    /**
     * Runs all registered tick listeners.
     */
    void onUpdate() {
        this.tickListener.forEach(Runnable::run);
    }

    /**
     * Handles an incoming packet for a sync handler or a synced action.
     * On the client the packet is read directly. On the server it is only accepted if the sync handler allows client packets.
     *
     * @param mapKey the key of the sync handler or synced action
     * @param action true if the packet targets a synced action instead of a sync handler
     * @param id     the id of the sync handler update
     * @param buf    the packet data
     * @throws IOException if reading the packet data fails
     */
    @ApiStatus.Internal
    public void receiveWidgetUpdate(String mapKey, boolean action, int id, PacketBuffer buf) throws IOException {
        if (action) {
            invokeSyncedAction(mapKey, buf);
            return;
        }
        if (!this.syncHandlers.containsKey(mapKey)) {
            ModularUI.LOGGER.warn("SyncHandler '{}' does not exist for panel '{}'! ID was {}.", mapKey, panelName, id);
            return;
        }
        SyncHandler<?> syncHandler = this.syncHandlers.get(mapKey);
        if (isClient()) {
            syncHandler.readOnClient(id, buf);
        } else if (syncHandler.isAllowC2S()) {
            syncHandler.readOnServer(id, buf);
        } else {
            ModularUI.LOGGER.throwing(Level.WARN, new SecurityException("Tried to send a packet to server, but the sync handler does not accept client packets."));
        }
    }

    /**
     * Invokes the synced action with the given key. Sync handler registration is temporarily allowed
     * if the action is executed on both sides.
     *
     * @param mapKey the key of the synced action
     * @param buf    the packet data passed to the action
     * @return true if the action should be forwarded to the other side, false otherwise
     */
    private boolean invokeSyncedAction(String mapKey, PacketBuffer buf) {
        SyncedAction syncedAction = this.syncedActions.get(mapKey);
        if (syncedAction == null) {
            ModularUI.LOGGER.warn("SyncAction '{}' does not exist for panel '{}'!.", mapKey, panelName);
            return false;
        }
        if (!isLocked() || this.allowSyncHandlerRegistration || !syncedAction.isExecuteClient() || !syncedAction.isExecuteServer()) {
            syncedAction.invoke(this.client, buf);
        } else {
            // only allow sync handler registration if it is executed on client and server
            allowTemporarySyncHandlerRegistration(true);
            syncedAction.invoke(this.client, buf);
            allowTemporarySyncHandlerRegistration(false);
        }
        // true if the action should be executed on the other side
        return syncedAction.isExecute(!this.client);
    }

    public ItemStack getCursorItem() {
        return getModularSyncManager().getCursorItem();
    }

    public void setCursorItem(ItemStack stack) {
        getModularSyncManager().setCursorItem(stack);
    }

    @Override
    public boolean hasSyncHandler(SyncHandler<?> syncHandler) {
        if (this.reverseSyncHandlers.containsKey(syncHandler)) return true;
        return this != getHyperVisor() && getHyperVisor().hasSyncHandler(syncHandler);
    }

    /**
     * Registers a sync handler under the key built from {@code name} and {@code id}.
     * If the sync handler was already registered under another key, the old entry is replaced.
     *
     * @param name        the name of the sync handler
     * @param id          the id of the sync handler
     * @param syncHandler the sync handler to register
     * @throws IllegalStateException if registration is locked or the auto sync rules are violated
     */
    private void putSyncValue(String name, int id, SyncHandler<?> syncHandler) {
        if (isLocked()) {
            // registration of sync handlers forbidden
            if (this.allowSyncHandlerRegistration) {
                // lock can be bypassed currently, but it wasn't used
                throw new IllegalStateException("SyncHandlers must be registered during panel building. Please use getOrCreateSyncHandler() inside DynamicSyncHandler!");
            } else {
                throw new IllegalStateException("SyncHandlers must be registered during panel building. The only exceptions is via a DynamicSyncHandler and sync functions!");
            }
        }
        String key = makeSyncKey(name, id);
        String currentKey = this.reverseSyncHandlers.get(syncHandler);
        if (currentKey != null) {
            if (!currentKey.equals(key)) {
                boolean auto = name.startsWith(ModularSyncManager.AUTO_SYNC_PREFIX);
                if (auto != currentKey.startsWith(ModularSyncManager.AUTO_SYNC_PREFIX)) {
                    throw new IllegalStateException("Old and new sync handler must both be either not auto or auto!");
                }
                if (auto && !currentKey.startsWith(name)) {
                    throw new IllegalStateException("Sync Handler was previously added with a different panel!");
                }
            }
            this.syncHandlers.remove(currentKey);
        }
        this.syncHandlers.put(key, syncHandler);
        this.reverseSyncHandlers.put(syncHandler, key);
        if (isInitialised()) {
            syncHandler.init(key, this);
        }
    }

    @Override
    public PanelSyncManager syncValue(String name, int id, SyncHandler<?> syncHandler) {
        Objects.requireNonNull(name, "Name must not be null");
        Objects.requireNonNull(syncHandler, "Sync Handler must not be null");
        putSyncValue(name, id, syncHandler);
        return this;
    }

    /**
     * @deprecated replaced by {@link #syncedPanel(String, boolean, PanelSyncHandler.IPanelBuilder)}
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "3.3.0")
    @Deprecated
    public IPanelHandler panel(String key, PanelSyncHandler.IPanelBuilder panelBuilder, boolean subPanel) {
        SyncHandler<?> sh = this.subPanels.get(key);
        if (sh != null) return (IPanelHandler) sh;
        PanelSyncHandler syncHandler = new PanelSyncHandler(panelBuilder, subPanel);
        this.subPanels.put(key, syncHandler);
        return syncHandler;
    }

    /**
     * Creates a synced panel handler. This can be used to automatically handle syncing for synced panels.
     * Synced panels do not need to be synced themselves, but contain at least one widget which is synced.
     * <p><b>NOTE</b></p>
     * A panel sync handler is only created once. If one was already registered, that one will be returned.
     * (This is only relevant for nested sub panels.) Furthermore, the panel handler has to be created on client and server with the same
     * key. Like any other sync handler, the panel sync handler has to be created before the panel opened. The only exception is inside
     * dynamic sync handlers.
     *
     * @param key          the key used for syncing
     * @param subPanel     true if this panel should close when its parent closes (the parent is defined by <i>this</i> {@link PanelSyncManager})
     * @param panelBuilder the panel builder, that will create the new panel. It must not return null or any existing panels.
     * @return a synced panel handler.
     * @throws NullPointerException     if the build panel of the builder is null
     * @throws IllegalArgumentException if the build panel of the builder is the main panel
     * @throws IllegalStateException    if this method was called too late
     */
    @Override
    public IPanelHandler syncedPanel(String key, boolean subPanel, PanelSyncHandler.IPanelBuilder panelBuilder) {
        IPanelHandler ph = findPanelHandlerNullable(key);
        if (ph != null) return ph;
        if (isLocked() && !this.allowSyncHandlerRegistration) {
            // registration of sync handlers forbidden
            throw new IllegalStateException("Synced panels must be registered during panel building. The only exceptions is via a DynamicSyncHandler and sync functions!");
        }
        PanelSyncHandler syncHandler = new PanelSyncHandler(panelBuilder, subPanel);
        this.subPanels.put(key, syncHandler);
        if (isInitialised() && (this == this.modularSyncManager.getMainPSM() ||
                this.modularSyncManager.getMainPSM().findSyncHandlerNullable(this.panelName, PanelSyncHandler.class) == null)) {
            // current panel is open
            this.modularSyncManager.getMainPSM().registerPanelSyncHandler(key, syncHandler);
        }
        return syncHandler;
    }

    @Override
    public @Nullable IPanelHandler findPanelHandlerNullable(String key) {
        return this.subPanels.get(key);
    }

    /**
     * Registers a slot group. Singleton slot groups are not stored.
     *
     * @param slotGroup the slot group to register
     * @return {@code this} for chaining
     */
    @Override
    public PanelSyncManager registerSlotGroup(SlotGroup slotGroup) {
        if (!slotGroup.isSingleton()) {
            this.slotGroups.put(slotGroup.getName(), slotGroup);
        }
        return this;
    }

    public interface SlotFunction {

        @NotNull
        ModularSlot apply(@NotNull PlayerMainInvWrapper playerInv, int index);
    }

    /**
     * @param listener The function to run whenever the gui {@link net.minecraft.inventory.Container} is open, ran on both the server and client threads.
     * @return {@code this} for chaining
     */
    public PanelSyncManager addOpenListener(Consumer<EntityPlayer> listener) {
        this.openListener.add(listener);
        return this;
    }

    /**
     * @param listener The function to run whenever the gui {@link net.minecraft.inventory.Container} is closed, ran on both the server and client threads.
     * @return {@code this} for chaining
     */
    public PanelSyncManager addCloseListener(Consumer<EntityPlayer> listener) {
        this.closeListener.add(listener);
        return this;
    }

    public PanelSyncManager onClientTick(Runnable runnable) {
        if (this.client) {
            this.tickListener.add(runnable);
        }
        return this;
    }

    public PanelSyncManager onServerTick(Runnable runnable) {
        if (!this.client) {
            this.tickListener.add(runnable);
        }
        return this;
    }

    public PanelSyncManager onCommonTick(Runnable runnable) {
        this.tickListener.add(runnable);
        return this;
    }

    /**
     * Registers a synced action. Actions that are executed neither on the client nor on the server are ignored.
     *
     * @param mapKey        the key of the action
     * @param executeClient true if the action should run on the client
     * @param executeServer true if the action should run on the server
     * @param action        the action to run
     * @return {@code this} for chaining
     */
    @Override
    public PanelSyncManager registerSyncedAction(String mapKey, boolean executeClient, boolean executeServer, ISyncedAction action) {
        if (executeClient || executeServer) {
            this.syncedActions.put(mapKey, new SyncedAction(action, executeClient, executeServer));
        }
        return this;
    }

    /**
     * Invokes a synced action and forwards it to the other side if required.
     *
     * @param mapKey the key of the action
     * @param packet the packet data passed to the action
     */
    public void callSyncedAction(String mapKey, PacketBuffer packet) {
        if (invokeSyncedAction(mapKey, packet)) {
            ModularNetwork.get(isClient()).sendActionPacket(getModularSyncManager(), this.panelName, mapKey, packet, getPlayer());
        }
    }

    /**
     * Builds a packet with the given builder and invokes the synced action with it.
     *
     * @param mapKey        the key of the action
     * @param packetBuilder function that writes the packet data
     */
    public void callSyncedAction(String mapKey, Consumer<PacketBuffer> packetBuilder) {
        PacketBuffer packet = new PacketBuffer(Unpooled.buffer());
        packetBuilder.accept(packet);
        callSyncedAction(mapKey, packet);
    }

    public void callSyncedAction(String mapKey) {
        callSyncedAction(mapKey, new PacketBuffer(Unpooled.buffer(0)));
    }

    /**
     * Returns the existing sync handler for the given name and id, or creates and registers a new one using the supplier.
     *
     * @param name     the name of the sync handler
     * @param id       the id of the sync handler
     * @param clazz    the expected type of the sync handler
     * @param supplier creates the sync handler if none exists
     * @param <T>      the type of the sync handler
     * @return the existing or newly created sync handler
     * @throws IllegalStateException if registration is locked or the existing handler has a different type
     */
    @Override
    public <T extends SyncHandler<?>> T getOrCreateSyncHandler(String name, int id, Class<T> clazz, Supplier<T> supplier) {
        SyncHandler<?> syncHandler = findSyncHandlerNullable(name, id);
        if (syncHandler == null) {
            if (isLocked() && !this.allowSyncHandlerRegistration) {
                // registration is locked, and we don't have permission to temporarily bypass lock
                throw new IllegalStateException("SyncHandlers must be registered during panel building. The only exceptions is via a DynamicSyncHandler and sync functions!");
            }
            T t = supplier.get();
            boolean l = this.locked;
            this.locked = false; // bypass possible lock
            putSyncValue(name, id, t);
            this.locked = l;
            return t;
        }
        if (clazz.isAssignableFrom(syncHandler.getClass())) {
            return clazz.cast(syncHandler);
        }
        throw new IllegalStateException("SyncHandler for key " + makeSyncKey(name, id) + " is of type " + syncHandler.getClass() + ", but type " + clazz + " was expected!");
    }

    @Override
    public SlotGroup getSlotGroup(String name) {
        return this.slotGroups.get(name);
    }

    public Collection<SlotGroup> getSlotGroups() {
        return this.slotGroups.values();
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    public @Nullable SyncHandler<?> getSyncHandler(String mapKey) {
        return getSyncHandlerFromMapKey(mapKey);
    }

    public @Nullable SyncHandler<?> getSyncHandlerFromMapKey(String mapKey) {
        return this.syncHandlers.get(mapKey);
    }

    @Override
    public @Nullable SyncHandler<?> findSyncHandlerNullable(String name, int id) {
        return this.syncHandlers.get(makeSyncKey(name, id));
    }

    public EntityPlayer getPlayer() {
        return getModularSyncManager().getPlayer();
    }

    public ModularSyncManager getModularSyncManager() {
        return modularSyncManager;
    }

    public ISyncRegistrar<?> getHyperVisor() {
        return this.modularSyncManager.getMainPSM();
    }

    public ModularContainer getContainer() {
        return getModularSyncManager().getContainer();
    }

    public String getPanelName() {
        return panelName;
    }

    public boolean isClient() {
        return this.client;
    }

    public boolean isLocked() {
        return locked;
    }

    /**
     * Temporarily allows or forbids sync handler registration, used while executing synced actions.
     *
     * @param allow true to allow registration
     */
    void allowTemporarySyncHandlerRegistration(boolean allow) {
        this.allowSyncHandlerRegistration = allow;
    }

    /**
     * Builds the map key of a sync handler from its name and id.
     *
     * @param name the name of the sync handler
     * @param id   the id of the sync handler
     * @return the key in the form {@code name:id}
     */
    public static String makeSyncKey(String name, int id) {
        return name + ":" + id;
    }
}
