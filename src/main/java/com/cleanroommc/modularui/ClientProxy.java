package com.cleanroommc.modularui;

import com.cleanroommc.modularui.animation.AnimatorManager;
import com.cleanroommc.modularui.drawable.DrawableSerialization;
import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.factory.inventory.InventoryTypes;
import com.cleanroommc.modularui.holoui.HoloScreenEntity;
import com.cleanroommc.modularui.holoui.ScreenEntityRender;
import com.cleanroommc.modularui.mixins.early.forge.ForgeHooksClientMixin;
import com.cleanroommc.modularui.overlay.OverlayManager;
import com.cleanroommc.modularui.screen.ClientScreenHandler;
import com.cleanroommc.modularui.test.EventHandler;
import com.cleanroommc.modularui.test.OverlayTest;
import com.cleanroommc.modularui.test.TestItem;
import com.cleanroommc.modularui.theme.ThemeManager;
import com.cleanroommc.modularui.theme.ThemeReloadCommand;
import com.cleanroommc.modularui.utils.Platform;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.Timer;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {

    private final Timer timer60Fps = new Timer(60f);
    public static KeyBinding testKey;

    @Override
    void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        ClientScreenHandler clientScreenHandler = new ClientScreenHandler();
        // registered to both buses since handled events are not bound to a single bus
        FMLCommonHandler.instance().bus().register(clientScreenHandler);
        MinecraftForge.EVENT_BUS.register(clientScreenHandler);
        MinecraftForge.EVENT_BUS.register(new OverlayManager());
        AnimatorManager.init();

        if (ModularUIConfig.enableTestGuis) {
            MinecraftForge.EVENT_BUS.register(new EventHandler());
            testKey = new KeyBinding("key.test", Keyboard.KEY_NUMPAD4, "key.categories.modularui");
            ClientRegistry.registerKeyBinding(testKey);
        }
        if (ModularUIConfig.enableTestOverlays) {
            OverlayTest.init();
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

    @SubscribeEvent
    public void onKeyboard(InputEvent.KeyInputEvent event) {
        if (ModularUIConfig.enableTestGuis && testKey.isPressed() && ModularUI.Mods.BAUBLES.isLoaded()) {
            InventoryTypes.BAUBLES.visitAll(Platform.getClientPlayer(), (type, index, stack) -> {
                if (stack != null && stack.getItem() instanceof TestItem) {
                    GuiFactories.playerInventory().openFromBaublesClient(index);
                    return true;
                }
                return false;
            });
        }
    }

    @Override
    public Timer getTimer60Fps() {
        return this.timer60Fps;
    }
}
