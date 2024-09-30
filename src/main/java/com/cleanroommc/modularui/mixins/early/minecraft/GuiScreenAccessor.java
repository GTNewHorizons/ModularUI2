package com.cleanroommc.modularui.mixins.early.minecraft;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;

import net.minecraft.client.renderer.entity.RenderItem;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.io.IOException;
import java.util.List;

@Mixin(GuiScreen.class)
public interface GuiScreenAccessor {

    @Accessor("field_146298_h")
    int getTouchValue();

    @Accessor("field_146298_h")
    void setTouchValue(int value);

    @Accessor
    int getEventButton();

    @Accessor
    void setEventButton(int button);

    @Accessor
    long getLastMouseEvent();

    @Accessor
    void setLastMouseEvent(long event);

    @Accessor
    static RenderItem getItemRender() {
        throw new UnsupportedOperationException("Mixin failed to inject!");
    }

    @Accessor("fontRendererObj")
    FontRenderer getFontRenderer();

    @Accessor
    List<GuiButton> getButtonList();

    @Accessor
    List<GuiLabel> getLabelList();

    @Invoker
    void invokeKeyTyped(char typedChar, int keyCode) throws IOException;

    @Invoker
    void invokeMouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException;

    @Invoker("mouseMovedOrUp")
    void invokeMouseReleased(int mouseX, int mouseY, int state);

    @Invoker
    void invokeMouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick);
}
