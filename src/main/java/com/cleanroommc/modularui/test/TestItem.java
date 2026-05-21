package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.ClientProxy;
import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.animation.Animator;
import com.cleanroommc.modularui.animation.IAnimatable;
import com.cleanroommc.modularui.animation.MutableObjectAnimator;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.factory.PlayerInventoryGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.ISimpleBauble;
import com.cleanroommc.modularui.utils.Interpolation;
import com.cleanroommc.modularui.utils.Interpolations;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import com.cleanroommc.modularui.widgets.ItemDisplayWidget;
import com.cleanroommc.modularui.widgets.TransformWidget;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import baubles.api.expanded.BaubleExpandedSlots;

import java.util.List;

public class TestItem extends Item implements IGuiHolder<PlayerInventoryGuiData>, ISimpleBauble {

    public static final TestItem testItem = new TestItem();

    @Override
    public ModularScreen createScreen(PlayerInventoryGuiData data, ModularPanel mainPanel) {
        return new ModularScreen(ModularUI.ID, mainPanel);
    }

    @Override
    public ModularPanel buildUI(PlayerInventoryGuiData guiData, PanelSyncManager guiSyncManager, UISettings settings) {
        return ModularPanel.defaultPanel("main").size(150)
                .child(getFallingItem(TestEventHandler.getRandomItem(), 38, 65))
                .child(getFallingItem(TestEventHandler.getRandomItem(), 48, 65))
                .child(getFallingItem(TestEventHandler.getRandomItem(), 56, 65));
    }

    static class Pos implements IAnimatable<Pos> {

        private int x, y;

        public Pos(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public Pos interpolate(Pos start, Pos end, float t) {
            this.x = Interpolations.lerp(start.x, end.x, t);
            this.y = Interpolations.lerp(start.y, end.y, t);
            return this;
        }

        @Override
        public Pos copyOrImmutable() {
            return new Pos(x, y);
        }

        public void set(Pos other) {
            this.x = other.x;
            this.y = other.y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }

    private static TransformWidget getFallingItem(ItemStack itemStack, int xPos, int yPos) {
        Pos fallingPosition = getAnimatedPosition(new Pos(0, 0), new Pos(0, -80), Interpolation.BOUNCE_IN, 1000);
        ItemDisplayWidget widget = new ItemDisplayWidget().item(itemStack).background(IDrawable.NONE).hoverBackground(IDrawable.NONE).pos(xPos, yPos);
        return new TransformWidget(widget)
                .transform(stack -> {
                    stack.translate((float) fallingPosition.getX(), (float) fallingPosition.getY());
                });
    }

    private static Pos getAnimatedPosition(Pos fromPos, Pos toPos, Interpolation interpolation, int duration) {
        Animator animator = new MutableObjectAnimator<>(fromPos, fromPos.copyOrImmutable(), toPos)
                .bounds(0, 1)
                .curve(interpolation)
                .duration(duration);
        animator.reset(true);
        animator.animate(true);
        return fromPos;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player) {
        if (!worldIn.isRemote) {
            GuiFactories.playerInventory().openFromMainHand(player);
        }
        return super.onItemRightClick(itemStackIn, worldIn, player);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean showDebugInfo) {
        super.addInformation(stack, player, tooltip, showDebugInfo);
        tooltip.add("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.");
        tooltip.add(EnumChatFormatting.GREEN + "Press " + Platform.getKeyDisplay(ClientProxy.testKey) + " to open GUI from Baubles");
    }

    @Override
    public String[] getBaubleTypes(ItemStack itemstack) {
        return new String[]{BaubleExpandedSlots.amuletType};
    }
}
