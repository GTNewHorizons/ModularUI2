package com.cleanroommc.modularui.utils.fakeworld;

import net.minecraft.world.World;

import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.function.BiPredicate;

public interface ISchema extends Iterable<Pair<BlockPos, BlockInfo>> {

    World getWorld();

    Vector3d getFocus();

    BlockPos getOrigin();

    void setRenderFilter(@Nullable BiPredicate<BlockPos, BlockInfo> renderFilter);

    @Nullable BiPredicate<BlockPos, BlockInfo> getRenderFilter();
}
