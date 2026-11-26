package com.cleanroommc.modularui.api.value.sync;

import com.cleanroommc.modularui.api.value.IByteValue;

/**
 * A helper interface for sync values which can be turned into a byte.
 *
 * @param <T> value type
 */
public interface IByteSyncValue<T> extends IByteValue<T>, IValueSyncHandler<T> {

    @Override
    default void setByteValue(byte val) {
        setByteValue(val, true);
    }

    default void setByteValue(byte val, boolean setSource) {
        setByteValue(val, setSource, true);
    }

    void setByteValue(byte value, boolean setSource, boolean sync);
}
