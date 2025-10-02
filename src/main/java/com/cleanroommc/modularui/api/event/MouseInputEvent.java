package com.cleanroommc.modularui.api.event;

import cpw.mods.fml.common.eventhandler.Cancelable;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiScreenEvent;

public class MouseInputEvent extends GuiScreenEvent {
    public MouseInputEvent(GuiScreen gui) {
        super(gui);
    }

    /**
     * This event fires when mouse input is detected by a GuiScreen.
     * Cancel this event to bypass {@link GuiScreen#handleMouseInput()}.
     */
    @Cancelable
    public static class Pre extends MouseInputEvent {
        public Pre(GuiScreen gui) {
            super(gui);
        }
    }

    /**
     * This event fires after {@link GuiScreen#handleMouseInput()} provided that the active
     * screen has not been changed as a result of {@link GuiScreen#handleMouseInput()}.
     * Cancel this event when you successfully use the mouse input to prevent other handlers from using the same input.
     */
    @Cancelable
    public static class Post extends MouseInputEvent {
        public Post(GuiScreen gui) {
            super(gui);
        }
    }
}
