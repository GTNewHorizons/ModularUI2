package com.cleanroommc.modularui;

import codechicken.nei.guihook.GuiContainerManager;
import com.cleanroommc.modularui.drawable.DrawableSerialization;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.holoui.HoloScreenEntity;
import com.cleanroommc.modularui.holoui.ScreenEntityRender;
import com.cleanroommc.modularui.integration.nei.ModularUIContainerObjectHandler;
import com.cleanroommc.modularui.mixins.early.forge.ForgeHooksClientMixin;
import com.cleanroommc.modularui.test.EventHandler;
import com.cleanroommc.modularui.theme.ThemeManager;
import com.cleanroommc.modularui.theme.ThemeReloadCommand;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.util.Timer;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;

import static com.cleanroommc.modularui.ModularUI.MODID_NEI;
import static com.cleanroommc.modularui.ModularUI.isNEILoaded;

@SideOnly(Side.CLIENT)
@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {

    private final Timer timer60Fps = new Timer(60f);

    @Override
    void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        if (isNEILoaded) {
            registerNEIHandler();
        }

        FMLCommonHandler.instance().bus().register(new ClientEventHandler());

        if (ModularUIConfig.enableTestGuis) {
            MinecraftForge.EVENT_BUS.register(new EventHandler());
        }

        DrawableSerialization.init();
        RenderingRegistry.registerEntityRenderingHandler(HoloScreenEntity.class, new ScreenEntityRender());

        // enable stencil buffer
        if (MinecraftForgeClient.getStencilBits() == 0) {
            // is this correct way in 1.7.10?
            ForgeHooksClientMixin.setStencilBits(8);
        }
    }

    @Override
    void postInit(FMLPostInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new ThemeReloadCommand());
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(new ThemeManager());
    }

    @Override
    public Timer getTimer60Fps() {
        return this.timer60Fps;
    }

    @Optional.Method(modid = MODID_NEI)
    private void registerNEIHandler() {
        GuiContainerManager.addObjectHandler(new ModularUIContainerObjectHandler());
    }
}
