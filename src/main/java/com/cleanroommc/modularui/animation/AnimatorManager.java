package com.cleanroommc.modularui.animation;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.List;

public class AnimatorManager {

    private static final List<IAnimator> animators = new ArrayList<>(16);
    private static final List<IAnimator> queuedAnimators = new ArrayList<>(8);
    private static long lastTime = 0;

    static void startAnimation(IAnimator animator) {
        if (!animators.contains(animator) && !queuedAnimators.contains(animator)) {
            queuedAnimators.add(animator);
        }
    }

    private AnimatorManager() {}

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new AnimatorManager());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onDraw(GuiScreenEvent.DrawScreenEvent.Pre event) {
        long time = Minecraft.getSystemTime();
        int elapsedTime = IAnimator.getTimeDiff(lastTime, time);
        if (lastTime > 0 && !animators.isEmpty()) {
            animators.removeIf(animator -> {
                if (animator == null) return true;
                if (animator.isPaused()) return false;
                animator.advance(elapsedTime);
                return !animator.isAnimating();
            });
        }
        lastTime = time;
        animators.addAll(queuedAnimators);
        queuedAnimators.clear();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDraw(GuiOpenEvent event) {
        if (event.gui == null) {
            // stop and yeet all animators on gui close
            animators.forEach(iAnimator -> iAnimator.stop(false));
            animators.clear();
        }
    }
}
