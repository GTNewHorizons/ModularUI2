package com.cleanroommc.modularui.api.value.sync;

import com.cleanroommc.modularui.api.value.IShortValue;

/**
 * A helper interface for sync values which can be turned into a short.
 *
 * @param <T> value type
 */
public interface IShortSyncValue<T> extends IValueSyncHandler<T>, IShortValue<T> {

    @Override
    default void setShortValue(short val) {
        setShortValue(val, true, true);
    }

    default void setShortValue(short val, boolean setSource) {
        setShortValue(val, setSource, true);
    }

    void setShortValue(short value, boolean setSource, boolean sync);
}
