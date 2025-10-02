package com.cleanroommc.modularui.config;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.ModularUIConfig;

import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.SimpleGuiConfig;

import net.minecraft.client.gui.GuiScreen;

public class ModularUIGuiConfig extends SimpleGuiConfig {

    public ModularUIGuiConfig(GuiScreen parent) throws ConfigException {
        super(parent, ModularUI.ID, ModularUI.NAME, ModularUIConfig.class);
    }
}
