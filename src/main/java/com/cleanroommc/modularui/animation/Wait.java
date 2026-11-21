package com.cleanroommc.modularui.animation;

/**
 * An animator that does nothing but takes a fixed amount of time to finish. Useful for delays in sequences.
 */
public class Wait extends BaseAnimator {

    private int duration;
    private int progress = 0;

    public Wait() {
        this(250);
    }

    /**
     * Creates a wait with the given duration.
     *
     * @param duration wait time in milliseconds
     */
    public Wait(int duration) {
        this.duration = duration;
    }

    /**
     * Resets the progress to zero. The end state is not used, since a wait has no value to move to.
     *
     * @param atEnd unused
     */
    @Override
    public void reset(boolean atEnd) {
        this.progress = 0;
    }

    /**
     * Advances the wait timer and stops it once the duration is reached.
     *
     * @param elapsedTime time in milliseconds to advance by
     * @return the remaining time not consumed by the wait
     */
    @Override
    public int advance(int elapsedTime) {
        int max = this.duration - this.progress;
        int prog = Math.min(max, elapsedTime);
        this.progress += prog;
        if (this.progress >= this.duration) {
            stop(false);
        }
        return elapsedTime - prog;
    }

    /**
     * Checks whether the wait has started and is still running.
     *
     * @return {@code true} if progress is above zero and the wait is animating
     */
    @Override
    public boolean hasProgressed() {
        return progress > 0 && isAnimating();
    }

    /**
     * Sets the wait duration.
     *
     * @param duration wait time in milliseconds
     * @return this
     */
    public Wait duration(int duration) {
        this.duration = duration;
        return this;
    }
}
