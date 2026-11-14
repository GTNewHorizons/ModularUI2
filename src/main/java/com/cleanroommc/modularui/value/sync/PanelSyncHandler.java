package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.widget.ISynced;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.widget.WidgetTree;

import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

/**
 * If you want another panel where some widgets may be able to sync data, you will need this.
 * Register it in any {@link PanelSyncManager} (preferably the main one).
 * Then you can call {@link #openPanel()} and {@link #closePanel()} from any side.
 */
public final class PanelSyncHandler extends SyncHandler<PanelSyncHandler> implements IPanelHandler {

    public static final int SYNC_NOTIFY_OPEN = 0;
    public static final int SYNC_OPEN = 1;
    public static final int SYNC_CLOSE = 2;
    public static final int SYNC_DISPOSE = 3;

    private final IPanelBuilder panelBuilder;
    private final boolean subPanel;
    private String panelName;
    private ModularPanel openedPanel;
    private PanelSyncManager syncManager;
    private boolean open = false;

    /**
     * Creates a PanelSyncHandler
     *
     * @param panelBuilder a panel builder function
     * @param subPanel     true if this panel should close when its parent closes
     */
    PanelSyncHandler(IPanelBuilder panelBuilder, boolean subPanel) {
        this.panelBuilder = panelBuilder;
        this.subPanel = subPanel;
        allowC2S();
    }

    /**
     * Creates the panel using the panel builder.
     *
     * @param syncManager the sync manager for the new panel
     * @return the created panel
     */
    public ModularPanel createUI(PanelSyncManager syncManager) {
        return this.panelBuilder.buildUI(syncManager, this);
    }

    @Override
    public void openPanel() {
        openPanel(true);
    }

    /**
     * Opens the panel on the current side. On the client it first notifies the server if {@code syncToServer} is true.
     * The panel is created lazily on the first open.
     *
     * @param syncToServer true if the open should be sent to the server first
     * @throws IllegalStateException if the panel is already open in another screen
     */
    private void openPanel(boolean syncToServer) {
        if (isPanelOpen()) return;
        boolean client = getSyncManager().isClient();
        if (syncToServer && client) {
            syncToServer(SYNC_NOTIFY_OPEN);
            return;
        }
        if (this.syncManager != null && this.syncManager.getModularSyncManager() != getSyncManager().getModularSyncManager()) {
            throw new IllegalStateException("Can't reopen synced panel in another screen!");
        } else if (this.syncManager == null) {
            this.syncManager = new PanelSyncManager(getSyncManager().getModularSyncManager(), false);
            this.openedPanel = Objects.requireNonNull(createUI(this.syncManager));
            this.panelName = this.openedPanel.getName();
            this.openedPanel.setPanelSyncHandler(this);
            WidgetTree.collectSyncValues(this.syncManager, this.openedPanel, false);
            if (!client) {
                this.openedPanel = null;
            }
        }
        if (client) {
            ModularScreen screen = getSyncManager().getContainer().getScreen();
            if (!screen.isPanelOpen(this.openedPanel.getName())) {
                openInModularSyncManager();
                screen.getPanelManager().openPanel(this.openedPanel, this);
            } else {
                // this was not supposed to happen
                // make sure server side also closes the panel
                closePanelInternal();
                return;
            }
        } else {
            openInModularSyncManager();
        }
        this.open = true;
    }

    /**
     * Registers this panel as open in the modular sync manager.
     */
    private void openInModularSyncManager() {
        getSyncManager().getModularSyncManager().open(this.panelName, this.syncManager);
    }

    /**
     * Closes the panel. On the client the local panel is closed, on the server the close is synced to the client.
     */
    @Override
    public void closePanel() {
        if (getSyncManager().isClient()) {
            if (this.openedPanel != null) {
                this.openedPanel.closeIfOpen();
            }
        } else {
            syncToClient(SYNC_CLOSE);
        }
    }

    /**
     * Closes all sub panels of this panel.
     */
    @Override
    public void closeSubPanels() {
        this.syncManager.closeSubPanels();
    }

    /**
     * Marks the panel as closed and closes it in the modular sync manager. On the client the close is sent to the server.
     */
    @ApiStatus.Internal
    @Override
    public void closePanelInternal() {
        this.open = false;
        // Sync infrastructure may already be torn down if the screen was disposed
        // (e.g. deferred PanelManager.dispose() running after ModularNetwork.closeAll())
        if (!isValid()) return;
        getSyncManager().getModularSyncManager().close(this.panelName);
        if (getSyncManager().isClient()) {
            syncToServer(SYNC_CLOSE);
        }
    }

    /**
     * Deletes the cached panel and syncs the disposal to the client. Only possible if the panel is closed
     * and contains no {@link ItemSlotSH} sync handlers.
     *
     * @throws UnsupportedOperationException if the panel is open or contains item slot sync handlers
     */
    @Override
    public void deleteCachedPanel() {
        if (openedPanel == null || isPanelOpen()) return;
        boolean canDispose = WidgetTree.foreachChild(openedPanel, iWidget -> {
            if (iWidget instanceof ISynced<?> synced && synced.isSynced()) {
                return !(synced.getSyncHandler() instanceof ItemSlotSH);
            }
            return true;
        }, false);

        // This is because we can't guarantee that the sync handlers of the new panel are the same.
        // Dynamic sync handler changing is very error-prone.
        if (!canDispose) {
            throw new UnsupportedOperationException("Can't delete cached panel if it's still open or has ItemSlot Sync Handlers!");
        }

        disposePanel();
        sync(SYNC_DISPOSE);
    }

    /**
     * Clears the cached panel name, sync manager, and panel references.
     */
    private void disposePanel() {
        this.panelName = null;
        this.syncManager = null;
        this.openedPanel = null;
    }

    @Override
    public boolean isSubPanel() {
        return subPanel;
    }

    @Override
    public boolean isPanelOpen() {
        return this.open;
    }

    /**
     * Handles a sync packet received on the client.
     *
     * @param i            the sync action id ({@link #SYNC_OPEN}, {@link #SYNC_CLOSE} or {@link #SYNC_DISPOSE})
     * @param packetBuffer the packet data
     * @throws IOException if reading the packet fails
     */
    @Override
    public void readOnClient(int i, PacketBuffer packetBuffer) throws IOException {
        if (i == SYNC_OPEN) {
            openPanel(false);
        } else if (i == SYNC_CLOSE) {
            closePanel();
        } else if (i == SYNC_DISPOSE) {
            disposePanel();
        }
    }

    /**
     * Handles a sync packet received on the server. Opening is confirmed back to the client.
     *
     * @param i            the sync action id ({@link #SYNC_NOTIFY_OPEN}, {@link #SYNC_CLOSE} or {@link #SYNC_DISPOSE})
     * @param packetBuffer the packet data
     * @throws IOException if reading the packet fails
     */
    @Override
    public void readOnServer(int i, PacketBuffer packetBuffer) throws IOException {
        if (i == SYNC_NOTIFY_OPEN) {
            openPanel(false);
            syncToClient(SYNC_OPEN);
        } else if (i == SYNC_CLOSE) {
            closePanelInternal();
        } else if (i == SYNC_DISPOSE) {
            disposePanel();
        }
    }

    /**
     * A function which creates a secondary {@link ModularPanel}
     */
    public interface IPanelBuilder {

        /**
         * Creates a {@link ModularPanel}. It must NOT return null or the main panel.
         *
         * @param syncManager the sync manager for this panel
         * @param syncHandler the sync handler that sync opening and closing of this panel
         * @return the created panel
         */
        @NotNull
        ModularPanel buildUI(@NotNull PanelSyncManager syncManager, @NotNull IPanelHandler syncHandler);
    }
}
