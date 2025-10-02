package com.cleanroommc.modularui.utils.fakeworld;

import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.Map;
import java.util.function.BiPredicate;

public interface ISchema extends Iterable<Map.Entry<BlockPos, BlockInfo>> {

    World getWorld();

    Vector3d getFocus();

    BlockPos getOrigin();

    void setRenderFilter(@Nullable BiPredicate<BlockPos, BlockInfo> renderFilter);

    @Nullable BiPredicate<BlockPos, BlockInfo> getRenderFilter();
}
