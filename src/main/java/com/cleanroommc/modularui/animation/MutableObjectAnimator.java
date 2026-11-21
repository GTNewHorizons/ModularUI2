package com.cleanroommc.modularui.animation;

import java.util.function.Consumer;

/**
 * An {@link Animator} that interpolates an {@link IAnimatable} object from a start value to an end value on every update.
 *
 * @param <T> the animatable type
 */
public class MutableObjectAnimator<T extends IAnimatable<T>> extends Animator {

    private final T from;
    private final T to;
    private final T animatable;
    private Consumer<T> intermediateConsumer;

    /**
     * Creates an animator that interpolates the given object from {@code from} to {@code to} over the range {@code [0, 1]}.
     *
     * @param animatable the object whose value is updated on each frame
     * @param from the start value
     * @param to the end value
     */
    public MutableObjectAnimator(T animatable, T from, T to) {
        this.from = from;
        this.to = to;
        this.animatable = animatable;
        bounds(0f, 1f);
    }

    /**
     * Resumes the animation and immediately applies the interpolated value for the current progress.
     *
     * @param reverse whether to animate in reverse
     */
    @Override
    public void resume(boolean reverse) {
        super.resume(reverse);
        this.animatable.interpolate(this.from, this.to, getRawValue());
    }

    /**
     * Applies the interpolated value to the animatable, runs the intermediate consumer, then the base update callback.
     *
     * @return {@code true} when the animation should be stopped
     */
    @Override
    protected boolean onUpdate() {
        T intermediate = this.animatable.interpolate(this.from, this.to, getRawValue());
        if (this.intermediateConsumer != null) {
            this.intermediateConsumer.accept(intermediate);
        }
        return super.onUpdate();
    }

    /**
     * Sets an additional consumer function, that gets executed on every progress update (animation frame)
     * before the {@link Animator#onUpdate()}.
     * @param consumer the extra consumer
     * @return this
     */
    public MutableObjectAnimator<T> intermediateConsumer(Consumer<T> consumer) {
        this.intermediateConsumer = consumer;
        return this;
    }
}
