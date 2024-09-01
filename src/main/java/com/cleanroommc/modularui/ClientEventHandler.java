package com.cleanroommc.modularui;

import com.cleanroommc.modularui.api.event.KeyboardInputEvent;
import com.cleanroommc.modularui.api.event.MouseInputEvent;
import com.cleanroommc.modularui.drawable.Stencil;

import com.cleanroommc.modularui.screen.GuiContainerWrapper;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.client.event.GuiScreenEvent;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ClientEventHandler {

    private static long ticks = 0L;

    public static long getTicks() {
        return ticks;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            ticks++;
        }
    }

    @SubscribeEvent
    public void preDraw(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            GL11.glEnable(GL11.GL_STENCIL_TEST);
        }
        Stencil.reset();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onGuiInput(MouseInputEvent.Pre event) {
        if (hasDraggable(event)) {
            // cancel interactions with other mods
            event.gui.handleMouseInput();
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onGuiInput(KeyboardInputEvent.Pre event) {
        if (hasDraggable(event)) {
            // cancel interactions with other mods
            event.gui.handleKeyboardInput();
            event.setCanceled(true);
        }
    }

    private static boolean hasDraggable(GuiScreenEvent event) {
        return event.gui instanceof GuiContainerWrapper screenWrapper && screenWrapper.getScreen().getContext().hasDraggable();
    }
}
