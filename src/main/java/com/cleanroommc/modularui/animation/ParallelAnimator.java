package com.cleanroommc.modularui.animation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParallelAnimator extends BaseAnimator<ParallelAnimator> implements IAnimator {

    private final List<IAnimator> animators;
    private int waitTimeBetweenAnimators;

    private int startedAnimating = 0;
    private int finishedAnimating = 0;
    private int waitTime = 0;

    /**
     * Creates a parallel animator from a list. The list is copied and each child {@link BaseAnimator} gets this as parent.
     *
     * @param animators the animators to run together
     */
    public ParallelAnimator(List<IAnimator> animators) {
        this.animators = new ArrayList<>(animators);
        this.animators.forEach(animator -> {
            if (animator instanceof BaseAnimator<?> baseAnimator) {
                baseAnimator.setParent(this);
            }
        });
    }

    /**
     * Creates a parallel animator from varargs. Each child {@link BaseAnimator} gets this as parent.
     *
     * @param animators the animators to run together
     */
    public ParallelAnimator(IAnimator... animators) {
        this.animators = new ArrayList<>();
        Collections.addAll(this.animators, animators);
        this.animators.forEach(animator -> {
            if (animator instanceof BaseAnimator<?> baseAnimator) {
                baseAnimator.setParent(this);
            }
        });
    }

    /**
     * Starts the children. If no wait time is set, all children start immediately; otherwise only the first child
     * starts and the rest are started by {@link #advance(int)} after the wait time elapses.
     *
     * @param reverse whether to animate in reverse
     */
    @Override
    public void animate(boolean reverse) {
        super.animate(reverse);
        if (this.waitTimeBetweenAnimators <= 0) {
            for (IAnimator animator : animators) {
                animator.animate(reverse);
            }
            this.startedAnimating = this.animators.size();
        } else {
            this.animators.get(this.startedAnimating).animate(reverse);
        }
    }

    /**
     * Stops this animator and, if fully stopped, all children.
     *
     * @param force whether to stop immediately, ignoring reverse and repeat settings
     * @return {@code true} if this animator is fully stopped
     */
    @Override
    public boolean stop(boolean force) {
        if (super.stop(force)) {
            for (IAnimator animator : animators) {
                animator.stop(force);
            }
            return true;
        }
        return false;
    }

    /**
     * Resets the started and finished counters and resets all children.
     *
     * @param atEnd whether the animator should be reset to the end
     */
    @Override
    public void reset(boolean atEnd) {
        super.reset(atEnd);
        this.startedAnimating = 0;
        this.finishedAnimating = 0;
        for (IAnimator animator : animators) {
            animator.reset(atEnd);
        }
    }

    /**
     * Advances all started children and starts further children once the wait time between them has elapsed.
     * Stops the whole animator once every child has finished.
     *
     * @param elapsedTime time in milliseconds to advance by
     * @return the remaining time not consumed by this animator
     */
    @Override
    public int advance(int elapsedTime) {
        int remainingTime = 0;
        for (int i = 0; i < this.startedAnimating; i++) {
            IAnimator animator = this.animators.get(i);
            if (!animator.isAnimating()) continue;
            remainingTime = Math.max(remainingTime, animator.advance(elapsedTime));
            if (!animator.isAnimating()) {
                this.finishedAnimating++;
                if (isFinished()) {
                    stop(false);
                    return remainingTime;
                }
            }
        }
        while (elapsedTime > 0 && this.startedAnimating < this.animators.size()) {
            int prog = Math.min(elapsedTime, this.waitTimeBetweenAnimators - this.waitTime);
            this.waitTime += prog;
            elapsedTime -= prog;
            if (this.waitTime >= this.waitTimeBetweenAnimators) {
                this.animators.get(this.startedAnimating).animate(isAnimatingReverse());
                this.waitTime -= this.waitTimeBetweenAnimators;
                this.startedAnimating++;
            }
        }
        return Math.min(elapsedTime, remainingTime);
    }

    /**
     * Checks whether all children have finished.
     *
     * @return {@code true} if every child has finished
     */
    public boolean isFinished() {
        return this.finishedAnimating == this.animators.size();
    }

    /**
     * Checks whether this animator is running and at least one child has started.
     *
     * @return {@code true} if progress has begun
     */
    @Override
    public boolean hasProgressed() {
        return isAnimating() && this.startedAnimating > 0;
    }

    /**
     * Sets the delay in milliseconds between starting each child. A value of zero or less starts all children at once.
     *
     * @param waitTime delay in milliseconds between children
     * @return this
     */
    public ParallelAnimator waitTimeBetweenAnimators(int waitTime) {
        this.waitTimeBetweenAnimators = waitTime;
        return this;
    }

    /**
     * Adds an animator to run in parallel. Only allowed while not animating; the animator is reset first.
     *
     * @param animator the animator to add
     * @return this
     * @throws IllegalStateException if this animator is currently animating
     */
    @Override
    public ParallelAnimator inParallelWith(IAnimator animator) {
        if (isAnimating()) {
            throw new IllegalStateException("Can't add animators while animating");
        }
        reset();
        this.animators.add(animator);
        return this;
    }
}
