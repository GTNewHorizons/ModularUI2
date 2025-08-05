package com.cleanroommc.modularui.integration.nei;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import codechicken.nei.guihook.GuiContainerManager;

import com.cleanroommc.modularui.screen.GuiContainerWrapper;

import com.gtnewhorizons.modularui.common.internal.wrapper.ModularUIContainer;

@SuppressWarnings("unused")
public class NEIModularUIConfig implements IConfigureNEI {

    public static final GuiContainerWrapperStackPositioner stackPositioner = new GuiContainerWrapperStackPositioner();
    public static final GuiContainerWrapperOverlayHandler overlayHandler = new GuiContainerWrapperOverlayHandler();

    @Override
    public void loadConfig() {
        GuiContainerManager.addInputHandler(new ModularUIContainerInputHandler());
        GuiContainerManager.addObjectHandler(new ModularUIContainerObjectHandler());
        API.registerNEIGuiHandler(new ModularUINEIGuiHandler());
        //Done via mixins
        //API.registerGuiOverlay(GuiContainerWrapper.class, "", new GuiContainerWrapperStackPositioner());
        //API.registerGuiOverlayHandler(GuiContainerWrapper.class,  new GuiContainerWrapperOverlayHandler(),"");
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
