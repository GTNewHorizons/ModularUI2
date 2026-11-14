package com.cleanroommc.modularui.animation;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class AnimatorManager {

    private static final List<IAnimator> animators = new ArrayList<>(16);
    private static final List<IAnimator> queuedAnimators = new ArrayList<>(8);
    private static long lastTime = 0;

    /**
     * Queues an animator to be started on the next draw. Animators that are already running or queued are ignored.
     *
     * @param animator the animator to start
     */
    static void startAnimation(IAnimator animator) {
        if (!animators.contains(animator) && !queuedAnimators.contains(animator)) {
            queuedAnimators.add(animator);
        }
    }

    private AnimatorManager() {}

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new AnimatorManager());
    }

    /**
     * Advances all active animators by the time elapsed since the previous draw. Finished animators are removed,
     * paused ones are kept without advancing, and queued animators are moved into the active list.
     *
     * @param event the pre-draw event of the screen
     */
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

    /**
     * Stops and removes all active animators when the current GUI is closed.
     *
     * @param event the GUI open event, whose GUI is {@code null} when a GUI is closed
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDraw(GuiOpenEvent event) {
        if (event.gui == null) {
            // stop and yeet all animators on gui close
            animators.forEach(iAnimator -> iAnimator.stop(false));
            animators.clear();
        }
    }
}
