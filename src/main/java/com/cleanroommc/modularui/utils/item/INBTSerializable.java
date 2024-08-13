package com.cleanroommc.modularui.utils.item;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import net.minecraft.nbt.NBTBase;

public interface INBTSerializable<T extends NBTBase> {

    T serializeNBT();

    void deserializeNBT(T nbt);
}
