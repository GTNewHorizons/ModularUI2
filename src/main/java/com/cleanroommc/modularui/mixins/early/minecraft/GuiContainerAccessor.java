package com.cleanroommc.modularui.mixins.early.minecraft;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Set;

@Mixin(GuiContainer.class)
public interface GuiContainerAccessor {

    @Accessor
    void setXSize(int v);

    @Accessor
    int getXSize();

    @Accessor
    void setYSize(int v);

    @Accessor
    int getYSize();

    @Accessor
    void setGuiLeft(int v);

    @Accessor
    int getGuiLeft();

    @Accessor
    void setGuiTop(int v);

    @Accessor
    int getGuiTop();

    @Accessor("theSlot")
    void setHoveredSlot(Slot slot);

    @Accessor("theSlot")
    Slot getHoveredSlot();

    @Accessor
    Slot getClickedSlot();

    @Accessor
    ItemStack getDraggedStack();

    @Accessor
    boolean getIsRightMouseClick();

    @Accessor("field_147007_t")
    boolean getDragSplitting();

    @Accessor("field_147008_s")
    Set<Slot> getDragSplittingSlots();

    @Accessor("field_146987_F")
    int getDragSplittingLimit();

    @Invoker("func_146980_g")
    void invokeUpdateDragSplitting();

    @Accessor("field_147007_t")
    boolean isDragSplittingInternal();

    @Accessor("field_146996_I")
    int getDragSplittingRemnant();

    @Accessor
    ItemStack getReturningStack();

    @Accessor
    void setReturningStack(ItemStack stack);

    @Accessor
    Slot getReturningStackDestSlot();

    @Accessor("field_147011_y")
    int getTouchUpX();

    @Accessor("field_147010_z")
    int getTouchUpY();

    @Accessor
    long getReturningStackTime();

    @Invoker
    void invokeDrawGuiContainerForegroundLayer(int mouseX, int mouseY);

    @Invoker
    void invokeDrawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY);
}
