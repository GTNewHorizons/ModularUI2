package com.cleanroommc.modularui.animation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SequentialAnimator extends BaseAnimator<SequentialAnimator> implements IAnimator {

    private final List<IAnimator> animators;
    private int currentIndex = 0;

    /**
     * Creates a sequential animator from a list. The list is copied and each child {@link BaseAnimator} gets this as parent.
     *
     * @param animators the animators to run in order
     */
    public SequentialAnimator(List<IAnimator> animators) {
        this.animators = new ArrayList<>(animators);
        this.animators.forEach(animator -> {
            if (animator instanceof BaseAnimator<?> baseAnimator) {
                baseAnimator.setParent(this);
            }
        });
    }

    /**
     * Creates a sequential animator from varargs. Each child {@link BaseAnimator} gets this as parent.
     *
     * @param animators the animators to run in order
     */
    public SequentialAnimator(IAnimator... animators) {
        this.animators = new ArrayList<>();
        Collections.addAll(this.animators, animators);
        this.animators.forEach(animator -> {
            if (animator instanceof BaseAnimator<?> baseAnimator) {
                baseAnimator.setParent(this);
            }
        });
    }

    /**
     * Starts the sequence by animating the current child. Does nothing if the sequence is empty.
     *
     * @param reverse whether to animate in reverse
     */
    @Override
    public void animate(boolean reverse) {
        if (this.animators.isEmpty()) return;
        super.animate(reverse);
        // start first animation
        this.animators.get(this.currentIndex).animate(reverse);
    }

    /**
     * Resets the sequence and all children. The current index is set to the last child if {@code atEnd} is true, otherwise to the first.
     *
     * @param atEnd whether the sequence should be reset to the end
     */
    @Override
    public void reset(boolean atEnd) {
        super.reset(atEnd);
        this.currentIndex = atEnd ? this.animators.size() - 1 : 0;
        this.animators.forEach(animator -> animator.reset(atEnd));
    }

    /**
     * Resumes the child at the current index in the given direction.
     *
     * @param reverse whether to animate in reverse
     */
    @Override
    public void resume(boolean reverse) {
        super.resume(reverse);
        this.animators.get(this.currentIndex).resume(reverse);
    }

    /**
     * Advances the current child and moves to the next one (or previous one when reversing) once it finishes.
     * The sequence stops when the index leaves the list bounds.
     *
     * @param elapsedTime time in milliseconds to advance by
     * @return the remaining time not consumed by the sequence
     */
    @Override
    public int advance(int elapsedTime) {
        while (isAnimating() && elapsedTime > 0) {
            IAnimator animator = this.animators.get(currentIndex);
            elapsedTime = animator.advance(elapsedTime);
            if (!animator.isAnimating()) {
                // animator has finished
                this.currentIndex += getDirection();
                //ModularUI.LOGGER.info("Finished {}th animator", this.currentIndex);
                if (this.currentIndex >= this.animators.size() || this.currentIndex < 0) {
                    // whole sequence has finished
                    stop(false);
                } else {
                    // start next animation
                    animator = this.animators.get(this.currentIndex);
                    animator.animate(isAnimatingReverse());
                }
            }
        }
        return elapsedTime;
    }

    /**
     * Checks progress of the first child only.
     *
     * @return {@code true} if the first child has progressed
     */
    @Override
    public boolean hasProgressed() {
        return !this.animators.isEmpty() && this.animators.get(0).hasProgressed();
    }

    /**
     * Appends an animator to this sequence. Only allowed while not animating; the sequence is reset first.
     *
     * @param animator the animator to append
     * @return this
     * @throws IllegalStateException if the sequence is currently animating
     */
    @Override
    public SequentialAnimator followedBy(IAnimator animator) {
        if (isAnimating()) {
            throw new IllegalStateException("Can't add animators while animating");
        }
        reset();
        this.animators.add(animator);
        return this;
    }
}
