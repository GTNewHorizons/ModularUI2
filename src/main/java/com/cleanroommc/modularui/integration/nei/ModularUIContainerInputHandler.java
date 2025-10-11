package com.cleanroommc.modularui.integration.nei;

import com.cleanroommc.modularui.screen.ClientScreenHandler;

import net.minecraft.client.gui.inventory.GuiContainer;

import codechicken.nei.guihook.IContainerInputHandler;

import java.io.IOException;

public class ModularUIContainerInputHandler implements IContainerInputHandler {

    @Override
    public boolean keyTyped(GuiContainer gui, char keyChar, int keyCode) {
        // This input handler must be after LayoutManager but not in lastKeyTyped because it cannot handle esc key
        try {
            return ClientScreenHandler.handleKeyboardInput(ClientScreenHandler.getMuiScreen(), gui);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onKeyTyped(GuiContainer gui, char keyChar, int keyID) {}

    @Override
    public boolean lastKeyTyped(GuiContainer gui, char keyChar, int keyID) {
        return false;
    }

    @Override
    public boolean mouseClicked(GuiContainer gui, int mousex, int mousey, int button) {
        return false;
    }

    @Override
    public void onMouseClicked(GuiContainer gui, int mousex, int mousey, int button) {}

    @Override
    public void onMouseUp(GuiContainer gui, int mousex, int mousey, int button) {}

    @Override
    public boolean mouseScrolled(GuiContainer gui, int mousex, int mousey, int scrolled) {
        return false;
    }

    @Override
    public void onMouseScrolled(GuiContainer gui, int mousex, int mousey, int scrolled) {}

    @Override
    public void onMouseDragged(GuiContainer gui, int mousex, int mousey, int button, long heldTime) {}
}
