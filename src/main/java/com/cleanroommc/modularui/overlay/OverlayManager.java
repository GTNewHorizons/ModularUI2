package com.cleanroommc.modularui.overlay;

import com.cleanroommc.modularui.screen.ModularScreen;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.GuiOpenEvent;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ApiStatus.Experimental
public class OverlayManager {

    public static final List<OverlayHandler> overlays = new ArrayList<>();

    public static void register(OverlayHandler handler) {
        if (!overlays.contains(handler)) {
            overlays.add(handler);
            overlays.sort(OverlayHandler::compareTo);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui != Minecraft.getMinecraft().currentScreen) {
            OverlayStack.closeAll();
            if (event.gui == null) return;
            for (OverlayHandler handler : overlays) {
                if (handler.isValidFor(event.gui)) {
                    ModularScreen overlay = Objects.requireNonNull(handler.createOverlay(event.gui), "Overlays must not be null!");
                    overlay.constructOverlay(event.gui);
                    OverlayStack.open(overlay);
                }
            }
        }
    }
}
