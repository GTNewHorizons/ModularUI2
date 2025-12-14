package com.cleanroommc.modularui.integration.nei;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import codechicken.nei.guihook.GuiContainerManager;

@SuppressWarnings("unused")
public class NEIModularUIConfig implements IConfigureNEI {

    public static final ModularUIGuiContainerStackOverlay overlayHandler = new ModularUIGuiContainerStackOverlay();

    @Override
    public void loadConfig() {
        GuiContainerManager.addInputHandler(new ModularUIContainerInputHandler());
        GuiContainerManager.addObjectHandler(new ModularUIContainerObjectHandler());
        API.registerNEIGuiHandler(new ModularUINEIGuiHandler());
    }

    @Override
    public String getName() {
        return "ModularUI NEI integration";
    }

    @Override
    public String getVersion() {
        return "";
    }
}
