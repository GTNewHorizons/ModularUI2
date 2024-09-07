package com.cleanroommc.modularui.api;

import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.guihook.IContainerTooltipHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import java.util.Collections;
import java.util.List;

import static com.cleanroommc.modularui.ModularUI.isNEILoaded;
import static com.cleanroommc.modularui.screen.ClientScreenHandler.getDefaultContext;

public class MCHelper {

    public static boolean hasMc() {
        return getMc() != null;
    }

    public static Minecraft getMc() {
        return Minecraft.getMinecraft();
    }

    public static EntityPlayerSP getPlayer() {
        if (hasMc()) {
            return getMc().thePlayer;
        }
        return null;
    }

    public static boolean closeScreen() {
        if (!hasMc()) return false;
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        if (player != null) {
            player.closeScreen();
            return true;
        }
        Minecraft.getMinecraft().displayGuiScreen(null);
        return false;
    }

    public static boolean displayScreen(GuiScreen screen) {
        Minecraft mc = getMc();
        if (mc != null) {
            mc.displayGuiScreen(screen);
            return true;
        }
        return false;
    }

    public static GuiScreen getCurrentScreen() {
        Minecraft mc = getMc();
        return mc != null ? mc.currentScreen : null;
    }

    public static FontRenderer getFontRenderer() {
        if (hasMc()) return getMc().fontRenderer;
        return null;
    }

    public static List<String> getItemToolTip(ItemStack item) {
        if (!hasMc()) return Collections.emptyList();
        if (isNEILoaded && getMc().currentScreen instanceof GuiContainer guiContainer) return getNEIItemTooltip(item, guiContainer);
        List<String> list = item.getTooltip(getPlayer(), getMc().gameSettings.advancedItemTooltips);
        for (int i = 0; i < list.size(); ++i) {
            if (i == 0) {
                list.set(i, item.getItem().getRarity(item).rarityColor + list.get(i));
            } else {
                list.set(i, EnumChatFormatting.GRAY + list.get(i));
            }
        }
        return list;
    }

    private static List<String> getNEIItemTooltip(ItemStack item, GuiContainer guiContainer) {
        List<String> tooltips = GuiContainerManager.itemDisplayNameMultiline(item, guiContainer, true);

        GuiContainerManager.applyItemCountDetails(tooltips, item);

        if (GuiContainerManager.getManager() != null && GuiContainerManager.shouldShowTooltip(guiContainer)) {
            for (IContainerTooltipHandler handler : GuiContainerManager.getManager().instanceTooltipHandlers)
                tooltips = handler.handleItemTooltip(guiContainer, item, getDefaultContext().getMouseX(), getDefaultContext().getMouseY(), tooltips);
        }

        return tooltips;
    }
}
