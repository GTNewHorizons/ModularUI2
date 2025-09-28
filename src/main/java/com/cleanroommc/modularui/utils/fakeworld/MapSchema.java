package com.cleanroommc.modularui.utils.fakeworld;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.google.common.collect.AbstractIterator;
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.Iterator;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class MapSchema implements ISchema {

    private final World world;
    private final Object2ObjectOpenHashMap<BlockPos, BlockInfo> blocks = new Object2ObjectOpenHashMap<>();
    private BiPredicate<BlockPos, BlockInfo> renderFilter;
    private final BlockPos origin;
    private final Vector3d center;

    public MapSchema(Map<BlockPos, BlockInfo> blocks) {
        this(blocks, null);
    }

    public MapSchema(Map<BlockPos, BlockInfo> blocks, BiPredicate<BlockPos, BlockInfo> renderFilter) {
        this.world = new DummyWorld();
        this.renderFilter = renderFilter;
        BlockPos min = new BlockPos(BlockPosUtil.MAX.x, BlockPosUtil.MAX.y, BlockPosUtil.MAX.z);
        BlockPos max = new BlockPos(BlockPosUtil.MIN.x, BlockPosUtil.MAX.y, BlockPosUtil.MAX.z);
        if (!blocks.isEmpty()) {
            for (var entry : blocks.entrySet()) {
                if (entry.getValue().getBlock() != Blocks.air) {
                    this.blocks.put(entry.getKey(), entry.getValue());
                    entry.getValue().apply(this.world, entry.getKey());
                    BlockPosUtil.setMin(min, entry.getKey());
                    BlockPosUtil.setMax(max, entry.getKey());
                }
            }
        } else {
            min.set(0, 0, 0);
            max.set(0, 0, 0);
        }
        this.origin = min;
        this.center = BlockPosUtil.getCenterD(min, max);
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
    public World getWorld() {
        return this.world;
    }

    @Override
    public Vector3d getFocus() {
        return center;
    }

    @Override
    public BlockPos getOrigin() {
        return origin;
    }

    @NotNull
    @Override
    public Iterator<Map.Entry<BlockPos, BlockInfo>> iterator() {
        return new AbstractIterator<>() {

            private final ObjectIterator<Object2ObjectMap.Entry<BlockPos, BlockInfo>> it = blocks.object2ObjectEntrySet().fastIterator();

            @Override
            protected Map.Entry<BlockPos, BlockInfo> computeNext() {
                while (it.hasNext()) {
                    Map.Entry<BlockPos, BlockInfo> entry = it.next();
                    if (renderFilter == null || renderFilter.test(entry.getKey(), entry.getValue())) {
                        return entry;
                    }
                }
                return endOfData();
            }
        };
    }

    public static class Builder {

        private final Object2ObjectOpenHashMap<BlockPos, BlockInfo> blocks = new Object2ObjectOpenHashMap<>();
        private BiPredicate<BlockPos, BlockInfo> renderFilter;

        public Builder add(BlockPos pos, Block block) {
            return add(pos, block, 0, null);
        }

        public Builder add(BlockPos pos, Block block, int meta) {
            return add(pos, block, meta, null);
        }

        public Builder add(BlockPos pos, Block block, int blockMeta, TileEntity customTile) {
            if (block == Blocks.air) return this;
            this.blocks.put(pos, new BlockInfo(block, blockMeta, customTile));
            return this;
        }

        public Builder add(BlockPos pos, BlockInfo blockInfo) {
            this.blocks.put(pos, blockInfo.toImmutable());
            return this;
        }

        public Builder add(Iterable<BlockPos> posList, Function<BlockPos, BlockInfo> function) {
            for (BlockPos pos : posList) {
                BlockInfo info = function.apply(pos).toImmutable();
                add(pos, info);
            }
            return this;
        }

        public Builder add(Map<BlockPos, BlockInfo> blocks) {
            this.blocks.putAll(blocks);
            return this;
        }

        public Builder setRenderFilter(BiPredicate<BlockPos, BlockInfo> renderFilter) {
            this.renderFilter = renderFilter;
            return this;
        }

        public MapSchema build() {
            if (renderFilter == null) {
                return new MapSchema(this.blocks);
            }
            return new MapSchema(this.blocks, renderFilter);
        }
    }
}
