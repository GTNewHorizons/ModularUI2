package com.cleanroommc.modularui.hud;

import com.cleanroommc.modularui.utils.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Manages all registered {@link HudElement HUD elements}.
 * <p>
 * Subscribes to Forge render/tick events and draws each visible element. HUD elements are
 * display-only (no input capture). See {@link HudElement} for details.
 */
@ApiStatus.Internal
@SideOnly(Side.CLIENT)
public class HudManager {

    private static final List<HudElement> elements = new ArrayList<>();
    private static int lastWidth = -1;
    private static int lastHeight = -1;
    private static boolean initialized = false;
    private static final HudManager INSTANCE = new HudManager();

    private HudManager() {}

    /**
     * Registers the HUD manager on the Forge event bus. Must be called once during client init,
     * typically from {@code ClientProxy.preInit()}.
     */
    public static void init() {
        if (initialized) return;
        initialized = true;
        // must be an instance method - Forge's ASMEventHandler crashes on a static @SubscribeEvent
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(INSTANCE);
    }

    /**
     * Registers a HUD element so it is rendered and ticked.
     *
     * @param element the element to register
     */
    public static void register(HudElement element) {
        if (element == null) throw new NullPointerException("Element must not be null!");
        if (!elements.contains(element)) {
            elements.add(element);
        }
    }

    /**
     * Unregisters a HUD element.
     *
     * @param element the element to unregister
     * @return true if the element was registered and is now removed
     */
    public static boolean unregister(HudElement element) {
        return elements.remove(element);
    }

    /**
     * Removes all registered HUD elements.
     */
    public static void clear() {
        elements.clear();
    }

    /**
     * Renders HUD elements over the game world (no screen open), after the vanilla HUD.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != ElementType.ALL) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen != null) return;
        drawVisible(event.mouseX, event.mouseY, event.partialTicks, HudElement::isVisibleInWorld);
    }

    /**
     * Renders HUD elements underneath an open {@link GuiScreen} (before the screen draws).
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onGuiDrawPre(GuiScreenEvent.DrawScreenEvent.Pre event) {
        drawVisible(event.mouseX, event.mouseY, event.renderPartialTicks, HudElement::isVisibleUnderGui);
    }

    /**
     * Renders HUD elements over an open {@link GuiScreen} (after the screen draws).
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onGuiDrawPost(GuiScreenEvent.DrawScreenEvent.Post event) {
        drawVisible(event.mouseX, event.mouseY, event.renderPartialTicks, HudElement::isVisibleOverGui);
    }

    private static void drawVisible(int mouseX, int mouseY, float partialTicks, Predicate<HudElement> visibility) {
        if (elements.isEmpty()) return;
        checkResize();
        boolean anyVisible = false;
        for (HudElement e : elements) {
            if (visibility.test(e)) {
                anyVisible = true;
                e.getScreen()
                        .getContext()
                        .updateState(mouseX, mouseY, partialTicks);
            }
        }
        if (!anyVisible) return;

        // Render from lowest to highest priority so higher priority draws on top.
        elements.stream()
                .filter(visibility)
                .sorted(Comparator.comparingInt(HudElement::getRenderPriority))
                .forEach(e -> {
                    GlStateManager.enableBlend();
                    GlStateManager.color(1f, 1f, 1f, 1f);
                    e.getScreen()
                            .drawScreen();
                    e.getScreen()
                            .drawForeground();
                });
    }

    /**
     * Ticks all enabled, drawable HUD elements at 20 Hz (client tick).
     */
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (elements.isEmpty()) return;
        for (HudElement e : elements) {
            if (e.canDraw()) {
                e.getScreen()
                        .onUpdate();
            }
        }
    }

    private static void checkResize() {
        Minecraft mc = Minecraft.getMinecraft();
        int scaledWidth;
        int scaledHeight;
        if (mc.currentScreen != null) {
            scaledWidth = mc.currentScreen.width;
            scaledHeight = mc.currentScreen.height;
        } else {
            net.minecraft.client.gui.ScaledResolution res = new net.minecraft.client.gui.ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
            scaledWidth = res.getScaledWidth();
            scaledHeight = res.getScaledHeight();
        }

        boolean resized = scaledWidth != lastWidth || scaledHeight != lastHeight;
        if (resized) {
            lastWidth = scaledWidth;
            lastHeight = scaledHeight;
        }
        for (HudElement e : elements) {
            if (resized || !e.getScreen().getPanelManager().isOpen()) {
                e.getScreen().onResize(scaledWidth, scaledHeight);
            }
        }
    }
}
