package com.cleanroommc.modularui.config;

import com.gtnewhorizon.gtnhlib.config.SimpleGuiFactory;

import net.minecraft.client.gui.GuiScreen;

@SuppressWarnings("unused")
public class ModularUIGuiConfigFactory implements SimpleGuiFactory {

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return ModularUIGuiConfig.class;
    }
}
