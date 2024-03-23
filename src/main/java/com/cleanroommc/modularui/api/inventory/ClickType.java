package com.cleanroommc.modularui.api.inventory;

public enum ClickType {
    PICKUP,
    QUICK_MOVE,
    SWAP,
    CLONE,
    THROW,
    QUICK_CRAFT,
    PICKUP_ALL;

    public static final ClickType[] VALUES = values();

    public static ClickType fromNumber(int number) {
        return VALUES[number];
    }

    public int toNumber() {
        return ordinal();
    }
}
