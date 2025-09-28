package com.cleanroommc.modularui.utils.fakeworld;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import com.google.common.collect.AbstractIterator;
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.Iterator;
import java.util.Map;
import java.util.function.BiPredicate;

public class SchemaWorld extends DummyWorld implements ISchema {

    private final ObjectLinkedOpenHashSet<BlockPos> blocks = new ObjectLinkedOpenHashSet<>();
    private BiPredicate<BlockPos, BlockInfo> renderFilter;
    private final BlockPos min = new BlockPos();
    private final BlockPos max = new BlockPos();

    public SchemaWorld() {
        this((blockPos, blockInfo) -> true);
    }

    public SchemaWorld(BiPredicate<BlockPos, BlockInfo> renderFilter) {
        this.renderFilter = renderFilter;
    }

    @Override
    public void setRenderFilter(@Nullable BiPredicate<BlockPos, BlockInfo> renderFilter) {
        this.renderFilter = renderFilter;
    }

    @Override
    public @Nullable BiPredicate<BlockPos, BlockInfo> getRenderFilter() {
        return renderFilter;
    }

    @Override
    public boolean setBlock(int x, int y, int z, Block blockIn, int metadataIn, int flags) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockInfo blockInfo = BlockInfo.of(this, pos);
        boolean renderTest;
        boolean state;
        if (renderFilter == null || renderFilter.test(pos, blockInfo)) {
            renderTest = true;
            state = super.setBlock(x, y, z, blockIn, metadataIn, flags);
        } else {
            renderTest = state = false;
        }

        if (blockInfo.getBlock().isAir(this, x, y, z)) {
            if (this.blocks.remove(pos) && BlockPosUtil.isOnBorder(min, max, pos)) {
                if (this.blocks.isEmpty()) {
                    this.min.set(0, 0, 0);
                    this.max.set(0, 0, 0);
                } else {
                    min.set(BlockPosUtil.MAX);
                    max.set(BlockPosUtil.MIN);
                    for (BlockPos pos1 : blocks) {
                        BlockPosUtil.setMin(min, pos1);
                        BlockPosUtil.setMax(max, pos1);
                    }
                }
            }
        } else if (this.blocks.isEmpty()) {
            if (!renderTest) return false;
            this.blocks.add(pos);
            this.min.set(pos);
            this.max.set(pos);
        } else if (renderTest && this.blocks.add(pos)) {
            BlockPosUtil.setMin(this.min, pos);
            BlockPosUtil.setMax(this.max, pos);
        }
        return renderTest && state;
    }

    @Override
    public World getWorld() {
        return this;
    }

    @Override
    public Vector3d getFocus() {
        return BlockPosUtil.getCenterD(this.min, this.max);
    }

    @Override
    public BlockPos getOrigin() {
        return this.min;
    }

    @NotNull
    @Override
    public Iterator<Map.Entry<BlockPos, BlockInfo>> iterator() {
        return new AbstractIterator<>() {
            private final ObjectListIterator<BlockPos> it = blocks.iterator();
            private final BlockInfo.Mut info = new BlockInfo.Mut();
            private final MutablePair<BlockPos, BlockInfo> pair = new MutablePair<>(null, this.info);

            @Override
            protected Map.Entry<BlockPos, BlockInfo> computeNext() {
                while (it.hasNext()) {
                    var pos = it.next();
                    this.info.set(SchemaWorld.this, pos);
                    this.pair.setLeft(pos);
                    if (renderFilter == null || renderFilter.test(pos, info)) {
                        return this.pair;
                    }
                }
                return endOfData();
            }
        };
    }

    @Override
    protected int func_152379_p() {
        return -1;
    }

    @Override
    public Entity getEntityByID(int p_73045_1_) {
        return null;
    }
}
