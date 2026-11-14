package com.cleanroommc.modularui.animation;

import net.minecraft.client.Minecraft;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface IAnimator {

    @Nullable IAnimator getParent();

    /**
     * Resets the animator to the start (or end when reversing) and then resumes it in the given direction.
     *
     * @param reverse whether to animate in reverse
     */
    default void animate(boolean reverse) {
        reset(reverse);
        resume(reverse);
    }

    default void animate() {
        animate(false);
    }

    /**
     * Stops the animation.
     *
     * @param force whether to stop immediately, ignoring reverse and repeat settings
     * @return {@code true} if the animation is fully stopped
     */
    boolean stop(boolean force);

    void pause();

    void resume(boolean reverse);

    void reset(boolean atEnd);

    default void reset() {
        reset(false);
    }

    /**
     * Advances the animation by a given duration.
     *
     * @param elapsedTime elapsed time in ms
     * @return remaining time (elapsed time - consumed time)
     */
    @ApiStatus.OverrideOnly
    int advance(int elapsedTime);

    boolean isPaused();

    boolean isAnimating();

    boolean isAnimatingReverse();

    boolean hasProgressed();

    default boolean isAnimatingForward() {
        return isAnimating() && !isAnimatingReverse();
    }

    /**
     * Calculates the time elapsed since the given start time using the current system time.
     *
     * @param startTime start time in ms
     * @return elapsed time in ms
     */
    static int getTimeDiff(long startTime) {
        return getTimeDiff(startTime, Minecraft.getSystemTime());
    }

    /**
     * Calculates the absolute time difference, clamped to {@link Integer#MAX_VALUE}.
     *
     * @param startTime start time in ms
     * @param currentTime current time in ms
     * @return absolute difference in ms
     */
    static int getTimeDiff(long startTime, long currentTime) {
        long elapsedTime = Math.abs(currentTime - startTime);
        return (int) Math.min(Integer.MAX_VALUE, elapsedTime);
    }
}
