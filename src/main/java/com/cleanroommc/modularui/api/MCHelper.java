package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.widget.Interactable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.guihook.IContainerTooltipHandler;
import gregtech.common.items.ItemFluidDisplay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        if (ModularUI.Mods.NEI.isLoaded() && getMc().currentScreen instanceof GuiContainer guiContainer)
            return getNEIItemTooltip(item, guiContainer);
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

    public static List<String> getFluidTooltip(FluidStack fluid) {
        List<String> tooltip = new ArrayList<>();
        tooltip.add(fluid.getLocalizedName());
        if (ModularUI.Mods.GT5U.isLoaded()) {
            String formula = ItemFluidDisplay.getChemicalFormula(fluid);
            if (!formula.isEmpty()) {
                tooltip.add(EnumChatFormatting.YELLOW + formula);
            }
        }
        if (Minecraft.getMinecraft().gameSettings.advancedItemTooltips) {
            tooltip.add(StatCollector.translateToLocalFormatted("modularui2.fluid.registry", fluid.getFluid().getName()));
            if (Interactable.hasShiftDown()) {
                tooltip.add(
                        StatCollector.translateToLocalFormatted(
                                "modularui2.fluid.unique_registry",
                                FluidRegistry.getDefaultFluidName(fluid.getFluid())));
            }
        }
        return tooltip;
    }

    public static List<String> getAdditionalFluidTooltip(FluidStack fluid) {
        List<String> tooltip = new ArrayList<>();
        if (Interactable.hasShiftDown()) {
            tooltip.add(StatCollector.translateToLocalFormatted("modularui2.fluid.temperature", fluid.getFluid().getTemperature(fluid)));
            tooltip.add(
                    StatCollector.translateToLocalFormatted(
                            "modularui2.fluid.state",
                            fluid.getFluid().isGaseous(fluid) ? StatCollector.translateToLocal("modularui2.fluid.gas")
                                    : StatCollector.translateToLocal("modularui2.fluid.liquid")));
            if (ModularUI.Mods.NEI.isLoaded()) {
                String amountDetail = GuiContainerManager.fluidAmountDetails(fluid);
                if (amountDetail != null) {
                    tooltip.add(EnumChatFormatting.GRAY + amountDetail);
                }
            }
        }
        return tooltip;
    }
}
