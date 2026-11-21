package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.utils.ICopy;
import com.cleanroommc.modularui.utils.serialization.IByteBufAdapter;
import com.cleanroommc.modularui.utils.serialization.IByteBufDeserializer;
import com.cleanroommc.modularui.utils.serialization.IByteBufSerializer;
import com.cleanroommc.modularui.utils.serialization.IEquals;

import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Base class for syncing collections. Elements are serialized one by one, and changes are detected by comparing against the source.
 *
 * @param <T> the element type
 * @param <C> the collection type
 * @param <S> the self type
 */
public abstract class GenericCollectionSyncHandler<T, C extends Collection<T>, S extends GenericCollectionSyncHandler<T, C, S>> extends ValueSyncHandler<C, S> {

    private final Supplier<C> getter;
    private final Consumer<C> setter;
    private final IByteBufDeserializer<T> deserializer;
    private final IByteBufSerializer<T> serializer;
    private final IEquals<T> equals;
    private final ICopy<T> copy;

    protected GenericCollectionSyncHandler(@NotNull Supplier<C> getter,
                                           @Nullable Consumer<C> setter,
                                           @NotNull IByteBufDeserializer<T> deserializer,
                                           @NotNull IByteBufSerializer<T> serializer,
                                           @Nullable IEquals<T> equals,
                                           @Nullable ICopy<T> copy) {
        this.getter = Objects.requireNonNull(getter);
        this.setter = setter;
        this.deserializer = deserializer;
        this.serializer = serializer;
        this.equals = equals != null ? IEquals.wrapNullSafe(equals) : Objects::equals;
        this.copy = copy != null ? copy : ICopy.ofSerializer(serializer, deserializer);
    }

    /**
     * Sets the cached value and optionally writes it back to the source and syncs it.
     *
     * @param value     the new value
     * @param setSource true to write the value to the setter
     * @param sync      true to send the value to the other side
     */
    @Override
    public void setValue(C value, boolean setSource, boolean sync) {
        setCache(value);
        onSetCache(setSource, sync);
    }

    /**
     * Replaces the cached collection content with the given value.
     *
     * @param value the new value
     */
    protected abstract void setCache(C value);

    /**
     * Called after the cache was set. Writes to the setter if requested, notifies listeners and syncs if requested.
     *
     * @param setSource true to write the value to the setter
     * @param sync      true to send the value to the other side
     */
    protected void onSetCache(boolean setSource, boolean sync) {
        if (setSource && this.setter != null) {
            this.setter.accept(getValue());
        }
        onValueChanged();
        if (sync) sync();
    }

    /**
     * Updates the cache from the getter if it is the first sync or the source has changed.
     *
     * @param isFirstSync true if this is the first sync
     * @return true if the cache was updated
     */
    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        C c = this.getter.get();
        if (isFirstSync || didValuesChange(c)) {
            setValue(c, false, false);
            return true;
        }
        return false;
    }

    /**
     * Reads the current value from the getter and syncs it to the other side.
     */
    @Override
    public void notifyUpdate() {
        setValue(this.getter.get(), false, true);
    }

    /**
     * Checks whether the given values differ from the cached values.
     *
     * @param newValues the values from the source
     * @return true if the values changed
     */
    protected abstract boolean didValuesChange(C newValues);

    /**
     * Writes the collection size followed by all elements.
     *
     * @param buffer the buffer to write to
     * @throws IOException if writing fails
     */
    @Override
    public void write(PacketBuffer buffer) throws IOException {
        C c = getValue();
        buffer.writeVarIntToBuffer(c.size());
        for (T t : c) {
            this.serializer.serialize(buffer, t);
        }
    }

    @Override
    public abstract C getValue();

    /**
     * Compares two elements using the configured equality check.
     *
     * @param a the first element
     * @param b the second element
     * @return true if the elements are equal
     */
    public boolean areValuesEqual(T a, T b) {
        return this.equals.areEqual(a, b);
    }

    /**
     * Reads a single element from the buffer.
     *
     * @param buffer the buffer to read from
     * @return the deserialized element
     * @throws IOException if reading fails
     */
    protected T deserializeValue(PacketBuffer buffer) throws IOException {
        return this.deserializer.deserialize(buffer);
    }

    /**
     * Creates a deep copy of an element.
     *
     * @param value the element to copy
     * @return the copied element
     */
    protected T copyValue(T value) {
        return this.copy.createDeepCopy(value);
    }

    /**
     * Builder for collection sync handlers. Adapters and copy strategies are optional.
     *
     * @param <T> the element type
     * @param <C> the collection type
     * @param <B> the self type
     */
    public static class Builder<T, C extends Collection<T>, B extends Builder<T, C, B>> {

        protected Supplier<C> getter;
        protected Consumer<C> setter;
        protected IByteBufDeserializer<T> deserializer;
        protected IByteBufSerializer<T> serializer;
        protected IEquals<T> equals;
        protected ICopy<T> copy;

        public B getter(Supplier<C> getter) {
            this.getter = getter;
            return getSelf();
        }

        public B setter(Consumer<C> setter) {
            this.setter = setter;
            return getSelf();
        }

        public B deserializer(IByteBufDeserializer<T> deserializer) {
            this.deserializer = deserializer;
            return getSelf();
        }

        public B serializer(IByteBufSerializer<T> serializer) {
            this.serializer = serializer;
            return getSelf();
        }

        /**
         * Sets the equality check for elements. Protected because sets rely on the element equals and hashCode.
         *
         * @param equals the equality check
         * @return this builder
         */
        protected B equals(IEquals<T> equals) {
            this.equals = equals;
            return getSelf();
        }

        /**
         * Sets the deserializer, serializer, and equality check from one adapter.
         *
         * @param adapter the adapter to use
         * @return this builder
         */
        public B adapter(IByteBufAdapter<T> adapter) {
            return deserializer(adapter).serializer(adapter).equals(adapter);
        }

        public B copy(ICopy<T> copy) {
            this.copy = copy;
            return getSelf();
        }

        /**
         * Uses an immutable copy strategy for elements.
         *
         * @return this builder
         */
        public B immutableCopy() {
            return copy(ICopy.immutable());
        }

        protected B getSelf() {
            return (B) this;
        }
    }
}
