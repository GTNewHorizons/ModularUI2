package com.cleanroommc.modularui;

import codechicken.nei.guihook.GuiContainerManager;
import com.cleanroommc.modularui.drawable.DrawableSerialization;
import com.cleanroommc.modularui.holoui.HoloScreenEntity;
import com.cleanroommc.modularui.holoui.ScreenEntityRender;
import com.cleanroommc.modularui.integration.nei.ModularUIContainerObjectHandler;
import com.cleanroommc.modularui.integration.nei.ModularUIInputHandler;
import com.cleanroommc.modularui.mixins.early.forge.ForgeHooksClientMixin;
import com.cleanroommc.modularui.test.EventHandler;
import com.cleanroommc.modularui.theme.ThemeManager;
import com.cleanroommc.modularui.theme.ThemeReloadCommand;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.util.Timer;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        GuiContainerManager.addInputHandler(new ModularUIInputHandler());
        GuiContainerManager.addObjectHandler(new ModularUIContainerObjectHandler());

        ModularUI.timer60Fps = new Timer(60f);

        FMLCommonHandler.instance().bus().register(ClientEventHandler.class);

        if (ModularUI.isDevEnv) {
            MinecraftForge.EVENT_BUS.register(new EventHandler());
        }

        DrawableSerialization.init();
        RenderingRegistry.registerEntityRenderingHandler(HoloScreenEntity.class, new ScreenEntityRender());

        // enable stencil buffer
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        if (MinecraftForgeClient.getStencilBits() == 0) {
            // is this correct way in 1.7.10?
            ForgeHooksClientMixin.setStencilBits(8);
        }
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new ThemeReloadCommand());
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(new ThemeManager());
    }
}
