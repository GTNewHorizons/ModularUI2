package com.cleanroommc.modularui.api.widget;

import net.minecraft.inventory.Slot;

/**
 * A widget that forwards its vanilla slot behaviour to another widget returned by {@link #getDelegate()}.
 */
public interface IDelegatingWidget extends IWidget, IVanillaSlot {

    /**
     * @return the widget this widget delegates to
     */
    IWidget getDelegate();

    @Override
    default Slot getVanillaSlot() {
        return getDelegate() instanceof IVanillaSlot vanillaSlot ? vanillaSlot.getVanillaSlot() : null;
    }

    @Override
    default boolean handleAsVanillaSlot() {
        return getDelegate() instanceof IVanillaSlot vanillaSlot && vanillaSlot.handleAsVanillaSlot();
    }
}
