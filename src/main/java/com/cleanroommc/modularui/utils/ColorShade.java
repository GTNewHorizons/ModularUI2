package com.cleanroommc.modularui.utils;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterable;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * A named color with lighter and darker variants. Shades are registered by name so they can be looked up later.
 */
public class ColorShade implements IntIterable {

    /**
     * Starts building a new shade around the given main color.
     *
     * @param name unique name of the shade
     * @param main the main color
     * @return a new builder
     */
    public static Builder builder(String name, int main) {
        return new Builder(name, main);
    }

    private static final Map<String, ColorShade> COLOR_SHADES = new Object2ObjectOpenHashMap<>();

    public static @Nullable ColorShade getFromName(String name) {
        return COLOR_SHADES.get(name);
    }

    public static Collection<ColorShade> getAll() {
        return Collections.unmodifiableCollection(COLOR_SHADES.values());
    }

    public final String name;
    public final int main;
    private final int[] brighter;
    private final int[] darker;
    private final int[] all;

    /**
     * Creates the shade and registers it by name. The full sequence is ordered from brightest to darkest.
     */
    private ColorShade(String name, int main, int[] brighter, int[] darker) {
        this.name = name;
        this.main = main;
        this.brighter = brighter;
        this.darker = darker;
        this.all = new int[brighter.length + darker.length + 1];
        int k = 0;
        for (int i = brighter.length - 1; i >= 0; i--) {
            this.all[k++] = brighter[i];
        }
        this.all[k++] = this.main;
        for (int j : darker) {
            this.all[k++] = j;
        }
        COLOR_SHADES.put(name, this);
    }

    /**
     * Returns the darker shade at the given index.
     *
     * @param index index of the darker shade
     * @return the darker shade color
     */
    public int darker(int index) {
        return this.darker[index];
    }

    /**
     * Returns the darker shade at the index, clamped to the valid range.
     *
     * @param index shade index, clamped to the available darker shades
     * @return the darker shade color
     */
    public int darkerSafe(int index) {
        return this.darker[MathUtils.clamp(index, 0, this.darker.length - 1)];
    }

    public int darkerShadeCount() {
        return this.darker.length;
    }

    /**
     * Returns the brighter shade at the given index.
     *
     * @param index index of the brighter shade
     * @return the brighter shade color
     */
    public int brighter(int index) {
        return this.brighter[index];
    }

    /**
     * Returns the brighter shade at the index, clamped to the valid range.
     *
     * @param index shade index, clamped to the available brighter shades
     * @return the brighter shade color
     */
    public int brighterSafe(int index) {
        return this.brighter[MathUtils.clamp(index, 0, this.brighter.length - 1)];
    }

    public int brighterShadeCount() {
        return this.brighter.length;
    }

    @NotNull
    @Override
    public IntIterator iterator() {
        return IntIterators.wrap(this.all);
    }

    /**
     * Collects darker and brighter shades before creating a {@link ColorShade}.
     */
    public static class Builder {

        private final String name;
        private final int main;
        private final IntArrayList darker = new IntArrayList();
        private final IntArrayList brighter = new IntArrayList();

        public Builder(String name, int main) {
            this.name = name;
            this.main = main;
        }

        public Builder addDarker(int... darker) {
            this.darker.addElements(this.darker.size(), darker, 0, darker.length);
            return this;
        }

        public Builder addBrighter(int... brighter) {
            this.brighter.addElements(this.brighter.size(), brighter, 0, brighter.length);
            return this;
        }

        public ColorShade build() {
            this.darker.trim();
            this.brighter.trim();
            return new ColorShade(this.name, this.main, this.brighter.elements(), this.darker.elements());
        }
    }
}
