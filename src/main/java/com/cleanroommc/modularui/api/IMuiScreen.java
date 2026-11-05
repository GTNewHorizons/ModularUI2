package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.core.mixins.early.minecraft.GuiContainerAccessor;
import com.cleanroommc.modularui.screen.ClientScreenHandler;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.neverenoughanimations.api.IAnimatedScreen;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import me.eigenraven.lwjgl3ify.api.InputEvents;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.function.IntConsumer;

/**
 * Implement this interface on a {@link GuiScreen} to be able to use it as a custom wrapper.
 * The GuiScreen should have final {@link ModularScreen} field, which is set from the constructor.
 * Additionally, the GuiScreen MUST call {@link ModularScreen#construct(IMuiScreen)} in its constructor.
 * See {@link com.cleanroommc.modularui.screen.GuiScreenWrapper GuiScreenWrapper} and {@link com.cleanroommc.modularui.screen.GuiContainerWrapper GuiContainerWrapper}
 * for default implementations.
 * <p>
 * {@link InputEvents.KeyboardListener} is implemented for LWJGL3ify compat.
 */
@Optional.InterfaceList({
        @Optional.Interface(modid = ModularUI.ModIds.NEA, iface = "com.cleanroommc.neverenoughanimations.api.IAnimatedScreen"),
        @Optional.Interface(modid = ModularUI.ModIds.LWJGL3IFY, iface = "me.eigenraven.lwjgl3ify.api.InputEvents$KeyboardListener")
})
@SideOnly(Side.CLIENT)
public interface IMuiScreen extends IAnimatedScreen, InputEvents.KeyboardListener {

    /**
     * This should return an immutable instance field.
     *
     * @return the wrapped {@link ModularScreen}
     */
    @NotNull
    ModularScreen getScreen();

    /**
     * {@link GuiScreen GuiScreens} need to be focused when a text field is focused, to prevent key input from
     * behaving unexpectedly.
     *
     * @param focused if the screen should be focused
     */
    default void setFocused(boolean focused) {
        // doesn't exist in 1.7.10
        //getGuiScreen().setFocused(focused);
    }

    /**
     * This method decides how the gui background is drawn.
     * The intended usage is to override {@link GuiScreen#drawWorldBackground(int)} and call this method
     * with the super method reference as the second parameter.
     *
     * @param tint         background color tint
     * @param drawFunction a method reference to draw the world background normally with the tint as the parameter
     */
    @ApiStatus.NonExtendable
    default void handleDrawBackground(int tint, IntConsumer drawFunction) {
        if (ClientScreenHandler.shouldDrawWorldBackground()) {
            drawFunction.accept(tint);
        }
        ClientScreenHandler.drawDarkBackground(getGuiScreen(), tint);
        // after this recipe viewer will draw itself
        // for some reason recipe viewer is too stupid to set up OpenGL properly
        // without this (enableTexture2D() specifically) every item in recipe viewer will be texture less (white)
        Platform.setupDrawTex();
    }

    /**
     * This method is called every time the {@link ModularScreen} resizes.
     * This usually only affects {@link GuiContainer GuiContainers}.
     *
     * @param area area of the main panel
     */
    default void updateGuiArea(Rectangle area) {
        if (getGuiScreen() instanceof GuiContainer container) {
            ClientScreenHandler.updateGuiArea(container, area);
        }
    }

    /**
     * @return if this wrapper is a {@link GuiContainer}
     */
    @ApiStatus.NonExtendable
    default boolean isGuiContainer() {
        return getGuiScreen() instanceof GuiContainer;
    }

    /**
     * This method is called when a hovering widget detects that it's over a slot.
     * Hovering widgets are handled by {@link ModularGuiContext}.
     * Only affects {@link GuiContainer GuiContainers}.
     *
     * @param slot hovered slot
     */
    @ApiStatus.NonExtendable
    default void setHoveredSlot(Slot slot) {
        if (getGuiScreen() instanceof GuiContainerAccessor acc) {
            acc.setHoveredSlot(slot);
        }
    }

    /**
     * Returns the {@link GuiScreen} wrapping the {@link ModularScreen}.
     * In most cases this does not need to be overridden as this interface should be implemented on {@link GuiScreen GuiScreens}.
     *
     * @return the wrapping gui screen
     */
    default GuiScreen getGuiScreen() {
        return (GuiScreen) this;
    }

    @Override
    default int nea$getX() {
        return getScreen().getMainPanel().getArea().x;
    }

    @Override
    default int nea$getY() {
        return getScreen().getMainPanel().getArea().y;
    }

    @Override
    default int nea$getWidth() {
        return getScreen().getMainPanel().getArea().width;
    }

    @Override
    default int nea$getHeight() {
        return getScreen().getMainPanel().getArea().height;
    }

    @ApiStatus.Internal
    @Override
    default void onKeyEvent(InputEvents.KeyEvent event) {
        getScreen().onKeyEvent(event);
    }

    @ApiStatus.Internal
    @Override
    default void onTextEvent(InputEvents.TextEvent event) {
        getScreen().onTextEvent(event);
    }
}
