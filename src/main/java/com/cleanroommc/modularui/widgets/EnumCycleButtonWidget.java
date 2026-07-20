package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.IIntValue;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.value.EnumValue;

import java.util.Arrays;
import java.util.function.Function;

public class EnumCycleButtonWidget<E extends Enum<E>> extends AbstractCycleButtonWidget<EnumCycleButtonWidget<E>> {

    private final Class<E> clazz;

    public EnumCycleButtonWidget(Class<E> clazz) {
        this.clazz = clazz;

        overlay(e -> IKey.str(e.toString()));
    }

    @Override
    public EnumCycleButtonWidget<E> value(IIntValue<?> value) {
        return super.value(value);
    }

    public EnumCycleButtonWidget<E> value(EnumValue<E> value) {
        return super.value(value);
    }

    public EnumCycleButtonWidget<E> overlay(Function<E, IDrawable> overlay) {
        this.overlay = Arrays.stream(clazz.getEnumConstants()).map(overlay).toArray(IDrawable[]::new);
        return this;
    }

    public EnumCycleButtonWidget<E> hoverOverlay(Function<E, IDrawable> overlay) {
        this.hoverOverlay = Arrays.stream(clazz.getEnumConstants()).map(overlay).toArray(IDrawable[]::new);
        return this;
    }

    public EnumCycleButtonWidget<E> background(Function<E, IDrawable> overlay) {
        this.background = Arrays.stream(clazz.getEnumConstants()).map(overlay).toArray(IDrawable[]::new);
        return this;
    }

    public EnumCycleButtonWidget<E> hoverBackground(Function<E, IDrawable> overlay) {
        this.hoverBackground = Arrays.stream(clazz.getEnumConstants()).map(overlay).toArray(IDrawable[]::new);
        return this;
    }

    public EnumCycleButtonWidget<E> tooltip(Function<E, RichTooltip> overlay) {
        this.tooltip = Arrays.stream(clazz.getEnumConstants()).map(overlay).toArray(RichTooltip[]::new);
        return this;
    }
}
