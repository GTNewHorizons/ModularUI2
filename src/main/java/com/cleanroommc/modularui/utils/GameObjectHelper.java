package com.cleanroommc.modularui.utils;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.NoSuchElementException;

public class GameObjectHelper {

    public static ItemStack getItemStack(String mod, String path) {
        return getItemStack(mod, path, 0);
    }

    public static ItemStack getItemStack(String mod, String path, int meta) {
        ItemStack item = GameRegistry.findItemStack(mod, path, 1);
        if (item == null) throw new NoSuchElementException("Item '" + mod + ":" + path + "' was not found!");
        Items.feather.setDamage(item, meta);
        return item;
    }
}
