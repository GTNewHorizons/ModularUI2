package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.api.value.sync.IDoubleSyncValue;
import com.cleanroommc.modularui.api.value.sync.IStringSyncValue;
import com.cleanroommc.modularui.network.NetworkUtils;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

public class DoubleSyncValue extends ValueSyncHandler<Double> implements IDoubleSyncValue<Double>, IStringSyncValue<Double> {

    private final DoubleSupplier getter;
    private final DoubleConsumer setter;
    private double cache;

    public DoubleSyncValue(@NotNull DoubleSupplier getter, @Nullable DoubleConsumer setter) {
        this.getter = Objects.requireNonNull(getter);
        this.setter = setter;
        this.cache = getter.getAsDouble();
    }

    public DoubleSyncValue(@NotNull DoubleSupplier getter) {
        this(getter, (DoubleConsumer) null);
    }

    @Contract("null, null -> fail")
    public DoubleSyncValue(@Nullable DoubleSupplier clientGetter,
                           @Nullable DoubleSupplier serverGetter) {
        this(clientGetter, null, serverGetter, null);
    }

    @Contract("null, _, null, _ -> fail")
    public DoubleSyncValue(@Nullable DoubleSupplier clientGetter, @Nullable DoubleConsumer clientSetter,
                           @Nullable DoubleSupplier serverGetter, @Nullable DoubleConsumer serverSetter) {
        if (clientGetter == null && serverGetter == null) {
            throw new NullPointerException("Client or server getter must not be null!");
        }
        if (NetworkUtils.isClient()) {
            this.getter = clientGetter != null ? clientGetter : serverGetter;
            this.setter = clientSetter != null ? clientSetter : serverSetter;
        } else {
            this.getter = serverGetter != null ? serverGetter : clientGetter;
            this.setter = serverSetter != null ? serverSetter : clientSetter;
        }
        this.cache = this.getter.getAsDouble();
    }

    @Override
    public Double getValue() {
        return this.cache;
    }

    @Override
    public double getDoubleValue() {
        return this.cache;
    }

    @Override
    public void setValue(@NotNull Double value, boolean setSource, boolean sync) {
        setDoubleValue(value, setSource, sync);
    }

    @Override
    public void setDoubleValue(double value, boolean setSource, boolean sync) {
        this.cache = value;
        if (setSource && this.setter != null) {
            this.setter.accept(value);
        }
        if (sync) {
            sync(0, this::write);
        }
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (isFirstSync || this.getter.getAsDouble() != this.cache) {
            setDoubleValue(this.getter.getAsDouble(), false, false);
            return true;
        }
        return false;
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeDouble(getDoubleValue());
    }

    @Override
    public void read(PacketBuffer buffer) {
        setDoubleValue(buffer.readDouble(), true, false);
    }

    @Override
    public void setStringValue(String value, boolean setSource, boolean sync) {
        setDoubleValue(Double.parseDouble(value), setSource, sync);
    }

    @Override
    public String getStringValue() {
        return String.valueOf(this.cache);
    }
}
