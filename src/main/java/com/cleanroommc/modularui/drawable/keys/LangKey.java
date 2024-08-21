package com.cleanroommc.modularui.drawable.keys;

import com.cleanroommc.modularui.ClientEventHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import net.minecraft.client.resources.I18n;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

public class LangKey implements IKey {

    private final Supplier<String> keySupplier;
    private final Supplier<Object[]> argsSupplier;
    private String string;
    private long time = 0;

    public LangKey(@NotNull String key) {
        this(key, () -> null);
    }

    public LangKey(@NotNull String key, @Nullable Object[] args) {
        this(() -> Objects.requireNonNull(key), () -> args == null || args.length == 0 ? null : args);
    }

    public LangKey(@NotNull String key, @NotNull Supplier<Object[]> argsSupplier) {
        this(() -> Objects.requireNonNull(key), argsSupplier);
    }

    public LangKey(@NotNull Supplier<String> keySupplier) {
        this(keySupplier, () -> null);
    }

    public LangKey(@NotNull Supplier<String> keySupplier, @NotNull Supplier<Object[]> argsSupplier) {
        this.keySupplier = Objects.requireNonNull(keySupplier);
        this.argsSupplier = Objects.requireNonNull(argsSupplier);
    }

    public Supplier<String> getKeySupplier() {
        return keySupplier;
    }

    public Supplier<Object[]> getArgsSupplier() {
        return argsSupplier;
    }

    @Override
    public String get() {
        if (this.time == ClientEventHandler.getTicks()) {
            return this.string;
        }
        this.time = ClientEventHandler.getTicks();
        this.string = I18n.format(Objects.requireNonNull(this.keySupplier.get()), this.argsSupplier.get());
        return string;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof LangKey langKey) {
            return this.get().equals(langKey.get());
        }
        return false;
    }

    @Override
    public String toString() {
        return this.get();
    }
}
