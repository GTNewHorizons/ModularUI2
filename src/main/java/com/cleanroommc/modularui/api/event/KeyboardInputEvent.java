package com.cleanroommc.modularui.api.event;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiScreenEvent;
import cpw.mods.fml.common.eventhandler.Cancelable;

public class KeyboardInputEvent extends GuiScreenEvent {

    public KeyboardInputEvent(GuiScreen gui) {
        super(gui);
    }

    /**
     * This event fires when keyboard input is detected by a GuiScreen.
     * Cancel this event to bypass {@link GuiScreen#handleKeyboardInput()}.
     */
    @Cancelable
    public static class Pre extends KeyboardInputEvent {

        public Pre(GuiScreen gui) {
            super(gui);
        }
    }

    /**
     * This event fires after {@link GuiScreen#handleKeyboardInput()} provided that the active
     * screen has not been changed as a result of {@link GuiScreen#handleKeyboardInput()}.
     * Cancel this event when you successfully use the keyboard input to prevent other handlers from using the same input.
     */
    @Cancelable
    public static class Post extends KeyboardInputEvent {

        public Post(GuiScreen gui) {
            super(gui);
        }
    }
}
