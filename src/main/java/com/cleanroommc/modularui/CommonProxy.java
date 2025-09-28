package com.cleanroommc.modularui;

import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.factory.inventory.InventoryTypes;
import com.cleanroommc.modularui.holoui.HoloScreenEntity;
import com.cleanroommc.modularui.network.NetworkHandler;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.test.ItemEditorGui;
import com.cleanroommc.modularui.test.TestBlock;

import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.Timer;
import net.minecraftforge.common.MinecraftForge;

public class CommonProxy {

    void preInit(FMLPreInitializationEvent event) {
        try {
            ConfigurationManager.registerConfig(ModularUIConfig.class);
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }

        FMLCommonHandler.instance().bus().register(new GuiManager());
        MinecraftForge.EVENT_BUS.register(new GuiManager());

        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);

        if (ModularUIConfig.enableTestGuis) {
            MinecraftForge.EVENT_BUS.register(TestBlock.class);
            TestBlock.preInit();
        }

        EntityRegistry.registerModEntity(HoloScreenEntity.class, "modular_screen", 0, ModularUI.INSTANCE, 0, 0, false);

        NetworkHandler.init();
        GuiFactories.init();
        InventoryTypes.init();
    }

    void postInit(FMLPostInitializationEvent event) {}

    void onServerLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new ItemEditorGui.Command());
    }

    @SideOnly(Side.CLIENT)
    public Timer getTimer60Fps() {
        throw new UnsupportedOperationException();
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (event.player.openContainer instanceof ModularContainer container) {
            container.onUpdate();
        }
    }
}
