package com.cleanroommc.modularui.animation;

import com.cleanroommc.modularui.api.drawable.IInterpolation;

/**
 * An object whose value can be interpolated between two states and animated towards a target.
 *
 * @param <T> the animatable type
 */
public interface IAnimatable<T extends IAnimatable<T>> {

    /**
     * Interpolates between two values.
     *
     * @param start the value at {@code t = 0}
     * @param end the value at {@code t = 1}
     * @param t interpolation factor, usually in the range {@code [0, 1]}
     * @return the interpolated value
     */
    T interpolate(T start, T end, float t);

    /**
     * Returns a copy of this value if it is mutable, or this instance if it is immutable.
     *
     * @return a value that can be used as the animation start state
     */
    T copyOrImmutable();

    /**
     * Checks whether an animation to the target is needed. Returns {@code false} when the target equals this value.
     *
     * @param target the target value
     * @return {@code true} if this value differs from the target
     */
    default boolean shouldAnimate(T target) {
        return !equals(target);
    }

    /**
     * Creates an animator that interpolates from a copy of this value to the target.
     *
     * @param target the target value
     * @return a new animator for this object
     */
    default MutableObjectAnimator<T> animator(T target) {
        T self = (T) this;
        return new MutableObjectAnimator<>(self, self.copyOrImmutable(), target);
    }

    /**
     * Animates to the target using default settings, forward direction.
     *
     * @param target the target value
     */
    default void animate(T target) {
        animate(target, false);
    }

    /**
     * Animates to the target if {@link #shouldAnimate(IAnimatable)} allows it.
     *
     * @param target the target value
     * @param reverse whether to animate in reverse
     */
    default void animate(T target, boolean reverse) {
        if (shouldAnimate(target)) {
            animator(target).animate(reverse);
        }
    }

    /**
     * Animates to the target with reverse-on-finish and repeat settings, if {@link #shouldAnimate(IAnimatable)} allows it.
     *
     * @param target the target value
     * @param reverse whether to animate in reverse
     * @param reverseOnFinish whether to bounce back after finishing
     * @param repeatsOnFinish how often to repeat, negative for infinite
     */
    default void animate(T target, boolean reverse, boolean reverseOnFinish, int repeatsOnFinish) {
        if (shouldAnimate(target)) {
            animator(target).reverseOnFinish(reverseOnFinish).repeatsOnFinish(repeatsOnFinish).animate(reverse);
        }
    }

    /**
     * Animates to the target with a custom duration, if {@link #shouldAnimate(IAnimatable)} allows it.
     *
     * @param target the target value
     * @param durationMs animation duration in milliseconds
     * @param reverse whether to animate in reverse
     */
    default void animate(T target, int durationMs, boolean reverse) {
        if (shouldAnimate(target)) {
            animator(target).duration(durationMs).animate(reverse);
        }
    }

    /**
     * Animates to the target with a custom interpolation curve, if {@link #shouldAnimate(IAnimatable)} allows it.
     *
     * @param target the target value
     * @param curve the interpolation curve
     * @param reverse whether to animate in reverse
     */
    default void animate(T target, IInterpolation curve, boolean reverse) {
        if (shouldAnimate(target)) {
            animator(target).curve(curve).animate(reverse);
        }
    }

    /**
     * Animates to the target with a custom curve and duration, without reverse-on-finish or repeats.
     *
     * @param target the target value
     * @param curve the interpolation curve
     * @param durationMs animation duration in milliseconds
     * @param reverse whether to animate in reverse
     */
    default void animate(T target, IInterpolation curve, int durationMs, boolean reverse) {
        animate(target, curve, durationMs, reverse, false, 0);
    }

    /**
     * Animates to the target with full configuration, if {@link #shouldAnimate(IAnimatable)} allows it.
     *
     * @param target the target value
     * @param curve the interpolation curve
     * @param durationMs animation duration in milliseconds
     * @param reverse whether to animate in reverse
     * @param reverseOnFinish whether to bounce back after finishing
     * @param repeatsOnFinish how often to repeat, negative for infinite
     */
    default void animate(T target, IInterpolation curve, int durationMs, boolean reverse, boolean reverseOnFinish, int repeatsOnFinish) {
        if (shouldAnimate(target)) {
            animator(target)
                    .curve(curve)
                    .duration(durationMs)
                    .reverseOnFinish(reverseOnFinish)
                    .repeatsOnFinish(repeatsOnFinish)
                    .animate(reverse);
        }
    }
}
