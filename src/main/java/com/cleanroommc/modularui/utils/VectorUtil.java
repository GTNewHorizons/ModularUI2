package com.cleanroommc.modularui.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.lwjgl.util.vector.Vector3f;

public class VectorUtil {

    public static Vector3f set(Vector3f target, float x, float y, float z) {
        if (target == null) target = new Vector3f();
        target.set(x, y, z);
        return target;
    }

    @NotNull
    public static Vector3f set(@Nullable Vector3f target, Vector3d vec) {
        return set(target, (float) vec.x, (float) vec.y, (float) vec.z);
    }

    @NotNull
    public static Vector3f set(@Nullable Vector3f target, Vector3i vec) {
        return set(target, vec.x, vec.y, vec.z);
    }

    public static Vector3f vec3f(Vector3d vec3d) {
        return set(null, vec3d);
    }

    public static Vector3f vec3f(Vector3i vec3i) {
        return set(null, vec3i);
    }

    public static Vector3f vec3fAdd(Vector3f source, Vector3f target, float x, float y, float z) {
        if (target == null) target = new Vector3f();
        if (source == null) return set(target, x, y, z);
        if (target != source) target.set(source);
        return target.translate(x, y, z);
    }

    @NotNull
    public static Vector3f vec3fAdd(Vector3f source, @Nullable Vector3f target, Vector3i vec) {
        return vec3fAdd(source, target, vec.x, vec.y, vec.z);
    }

    @NotNull
    public static Vector3f vec3fAdd(Vector3f source, @Nullable Vector3f target, Vector3d vec) {
        return vec3fAdd(source, target, (float) vec.x, (float) vec.y, (float) vec.z);
    }
}
