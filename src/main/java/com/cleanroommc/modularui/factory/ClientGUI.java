package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.screen.ContainerCustomizer;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.NEISettingsImpl;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Helper class to open client only GUIs. This class is safe to use inside a Modular GUI.
 * Direct calls to {@link net.minecraft.client.Minecraft#displayGuiScreen(GuiScreen)} are redirected to this class if
 * the current screen is a Modular GUI.
 */
@SideOnly(Side.CLIENT)
public class ClientGUI {

    private ClientGUI() {
    }

    /**
     * Opens a modular screen on the next client tick with default NEI settings.
     *
     * @param screen new modular screen
     */
    public static void open(@NotNull ModularScreen screen) {
        open(screen, new NEISettingsImpl(), null);
    }

    /**
     * Opens a modular screen on the next client tick with custom NEI settings.
     * It needs to be opened in next tick, because we might break the current GUI if we open it now.
     *
     * @param screen      new modular screen
     * @param neiSettings custom NEI settings
     */
    public static void open(@NotNull ModularScreen screen, @NotNull NEISettingsImpl neiSettings) {
        GuiManager.openScreen(screen, neiSettings, null);
    }

    /**
     * Opens a modular screen on the next client tick with custom NEI settings.
     * It needs to be opened in next tick, because we might break the current GUI if we open it now.
     *
     * @param screen              new modular screen
     * @param containerCustomizer container customizer
     */
    public static void open(@NotNull ModularScreen screen, @NotNull ContainerCustomizer containerCustomizer) {
        GuiManager.openScreen(screen, new NEISettingsImpl(), containerCustomizer);
    }

    /**
     * Opens a modular screen on the next client tick with custom NEI settings.
     * It needs to be opened in next tick, because we might break the current GUI if we open it now.
     *
     * @param screen              new modular screen
     * @param neiSettings         custom NEI settings
     * @param containerCustomizer container customizer
     */
    public static void open(@NotNull ModularScreen screen, @NotNull NEISettingsImpl neiSettings, @Nullable ContainerCustomizer containerCustomizer) {
        GuiManager.openScreen(screen, neiSettings, containerCustomizer);
    }

    /**
     * Opens a {@link GuiScreen} on the next client tick.
     *
     * @param screen screen to open
     */
    public static void open(GuiScreen screen) {
        Minecraft.getMinecraft().displayGuiScreen(screen);
    }

    /**
     * Closes any GUI that is open in this tick.
     */
    public static void close() {
        Minecraft.getMinecraft().displayGuiScreen(null);
    }
}
