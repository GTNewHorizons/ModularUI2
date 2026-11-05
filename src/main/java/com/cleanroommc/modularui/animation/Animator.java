package com.cleanroommc.modularui.animation;

import com.cleanroommc.modularui.api.drawable.IInterpolation;
import com.cleanroommc.modularui.utils.Interpolation;

import java.util.function.DoubleConsumer;
import java.util.function.DoublePredicate;

public class Animator extends BaseAnimator<Animator> implements IAnimator {

    private float min = 0.0f;
    private float max = 1.0f;
    private int duration = 250;
    private IInterpolation curve = Interpolation.LINEAR;

    private DoublePredicate onUpdate;
    private Runnable onFinish;

    private int progress = 0;

    /**
     * Resets the animation progress to the start or the end and resets the base animator state.
     *
     * @param atEnd whether the progress should be set to the end ({@code true}) or the start ({@code false})
     */
    @Override
    public void reset(boolean atEnd) {
        super.reset(atEnd);
        this.progress = atEnd ? this.duration : 0;
    }

    /**
     * Creates a new animator with the same curve, duration, repeat and finish settings and callbacks.
     *
     * @param reversed whether the bounds of the copy should be swapped so it animates in reverse
     * @return a new animator with the copied configuration
     */
    public Animator copy(boolean reversed) {
        Animator animator = new Animator()
                .curve(this.curve)
                .reverseOnFinish(this.reverseOnFinish)
                .repeatsOnFinish(this.repeats)
                .onUpdate(this.onUpdate)
                .duration(this.duration)
                .onFinish(this.onFinish);
        if (reversed) {
            animator.bounds(this.max, this.min);
        } else {
            animator.bounds(this.min, this.max);
        }
        return animator;
    }

    /**
     * Advances the animation by the given time. The time is consumed in steps, stopping early when the update callback
     * requests a stop or when the animation finishes.
     *
     * @param elapsedTime time in milliseconds to advance by
     * @return the remaining time that was not consumed by this animation
     */
    @Override
    public int advance(int elapsedTime) {
        if (!isAnimating()) return elapsedTime;
        while (elapsedTime > 0) {
            int max = isAnimatingForward() ? this.duration - this.progress : this.progress;
            int prog = Math.min(max, elapsedTime);
            this.progress += prog * getDirection();
            elapsedTime -= prog;
            if (onUpdate()) {
                stop(true);
                break;
            }
            if ((isAnimatingForward() && this.progress >= this.duration) || (isAnimatingReverse() && this.progress <= 0)) {
                stop(false);
                if (!isAnimating()) {
                    onAnimationFinished(true, true);
                    break;
                }
            }
        }
        return elapsedTime;
    }

    /**
     *
     * @return {@code true} when the animation should be stopped
     */
    protected boolean onUpdate() {
        return this.onUpdate != null && this.onUpdate.test(getRawValue());
    }

    /**
     * Called when the animation finishes a cycle. Runs the finish callback if one is set.
     *
     * @param finishedOneCycle whether a single cycle has completed
     * @param finishedAllRepeats whether all repeats have completed
     */
    protected void onAnimationFinished(boolean finishedOneCycle, boolean finishedAllRepeats) {
        if (this.onFinish != null) {
            this.onFinish.run();
        }
    }

    public boolean isAtEnd() {
        return this.progress >= this.duration;
    }

    public boolean isAtStart() {
        return this.progress <= 0;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public int getDuration() {
        return duration;
    }

    public IInterpolation getCurve() {
        return curve;
    }

    /**
     * Computes the current value by interpolating between min and max using the curve and the current progress.
     *
     * @return the interpolated value
     */
    protected float getRawValue() {
        return this.curve.interpolate(this.min, this.max, (float) this.progress / this.duration);
    }

    public float getValue() {
        //advance();
        return getRawValue();
    }

    @Override
    public boolean hasProgressed() {
        if (!isAnimating()) return false;
        return isAnimatingForward() ? this.progress > 0 : this.progress < this.duration;
    }

    /**
     * Sets the min bound of the value that will be interpolated.
     *
     * @param min min value
     * @return this
     */
    public Animator min(float min) {
        this.min = min;
        return this;
    }

    /**
     * Sets the max bound of the value that will be interpolated.
     *
     * @param max max value
     * @return this
     */
    public Animator max(float max) {
        this.max = max;
        return this;
    }

    /**
     * Sets the bounds of the value that will be interpolated.
     *
     * @param min min value
     * @param max max value
     * @return this
     */
    public Animator bounds(float min, float max) {
        this.min = min;
        this.max = max;
        return this;
    }

    /**
     * The duration of this animation in milliseconds. Note this is not 100% accurate.
     * Usually it's plus minus 2ms, but can rarely be more.
     *
     * @param duration duration in milliseconds
     * @return this
     */
    public Animator duration(int duration) {
        this.duration = duration;
        return this;
    }

    /**
     * Sets the interpolation curve, which is used to interpolate between the bounds.
     *
     * @param curve curve to interpolate on
     * @return this
     */
    public Animator curve(IInterpolation curve) {
        this.curve = curve;
        return this;
    }

    /**
     * Sets a predicate function which is executed everytime the progress updates, that is on every frame.
     * The argument of the function is the interpolated value.<br>
     * The predicate's return value effects {@link Animator#onUpdate()}'s return value.
     *
     * @param onUpdate update function
     * @return this
     */
    public Animator onUpdate(DoublePredicate onUpdate) {
        this.onUpdate = onUpdate;
        return this;
    }

    /**
     * Sets a consumer function which is executed everytime the progress updates, that is on every frame.
     * The argument of the function is the interpolated value.<br>
     * The consumer will always be wrapped in q {@link DoublePredicate} returning false.
     *
     * @see Animator#onUpdate()
     * @see Animator#onUpdate(DoublePredicate)
     *
     * @param onUpdate update function
     * @return this
     */
    public Animator onUpdate(DoubleConsumer onUpdate) {
        return onUpdate(val -> {
            onUpdate.accept(val);
            return false;
        });
    }

    /**
     * Sets a function which is executed everytime, on animation, cycle or all repeats is finished.
     *
     * @param onFinish finish function
     * @return this
     */
    public Animator onFinish(Runnable onFinish) {
        this.onFinish = onFinish;
        return this;
    }
}
