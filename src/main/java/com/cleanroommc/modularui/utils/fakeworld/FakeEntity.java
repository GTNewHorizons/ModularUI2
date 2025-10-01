package com.cleanroommc.modularui.utils.fakeworld;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import cpw.mods.fml.common.registry.EntityRegistry;

import java.lang.reflect.InvocationTargetException;

/**
 * Utility class for creating entities for rendering in gui.
 */
public class FakeEntity {

    private static final World entityWorld = new DummyWorld();

    private FakeEntity() {}

    @SuppressWarnings("unchecked")
    public static <T extends Entity> T create(Class<T> entityClass) {
        return (T) create(EntityRegistry.instance().lookupModSpawn(entityClass, false));
    }

    public static Entity create(EntityRegistry.EntityRegistration entry) {
        try {
            return entry.getEntityClass().getConstructor(World.class).newInstance(entityWorld);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
