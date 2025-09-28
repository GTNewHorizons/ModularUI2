package com.cleanroommc.modularui.utils.fakeworld;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.google.common.base.Preconditions;
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * BlockInfo represents immutable information for block in world
 * This includes block state and tile entity, and needed for complete representation
 * of some complex blocks like machines, when rendering or manipulating them without world instance
 */
public class BlockInfo {

    public static final BlockInfo EMPTY = new BlockInfo(Blocks.air);
    public static final BlockInfo INVALID = new BlockInfo(Blocks.air);

    public static BlockInfo of(IBlockAccess world, BlockPos pos) {
        Block block = world.getBlock(pos.x, pos.y, pos.z);
        int blockMeta = world.getBlockMetadata(pos.x, pos.y, pos.z);
        if (block.isAir(world, pos.x, pos.y, pos.z)) {
            return EMPTY;
        }
        TileEntity tile = null;
        if (block.hasTileEntity(blockMeta)) {
            tile = world.getTileEntity(pos.x, pos.y, pos.z);
        }
        return new BlockInfo(block, blockMeta, tile);
    }

    private Block block;
    private int blockMeta;
    private TileEntity tileEntity;

    public BlockInfo(@NotNull Block block) {
        this(block, 0, null);
    }

    public BlockInfo(@NotNull Block block, int blockMeta) {
        this(block, blockMeta, null);
    }

    public BlockInfo(@NotNull Block block, @Nullable TileEntity tileEntity) {
        this(block, 0, tileEntity);
    }

    public BlockInfo(@NotNull Block block, int blockMeta, @Nullable TileEntity tileEntity) {
        set(block, blockMeta, tileEntity);
    }

    public Block getBlock() {
        return block;
    }

    public int getBlockMeta() {
        return blockMeta;
    }

    public TileEntity getTileEntity() {
        return tileEntity;
    }

    public void apply(World world, BlockPos pos) {
        world.setBlock(pos.x, pos.y, pos.z, block);
        world.setBlockMetadataWithNotify(pos.x, pos.y, pos.z, blockMeta, 2);
        if (tileEntity != null) {
            world.setTileEntity(pos.x, pos.y, pos.z, tileEntity);
        } else {
            tileEntity = world.getTileEntity(pos.x, pos.y, pos.z);
        }
    }

    BlockInfo set(Block block, int blockMeta, TileEntity tile) {
        Preconditions.checkNotNull(block, "Block must not be null!");
        Preconditions.checkArgument(tile == null || block.hasTileEntity(blockMeta),
                "Cannot create block info with tile entity for block not having it!");
        this.block = block;
        this.blockMeta = blockMeta;
        this.tileEntity = tile;
        return this;
    }

    public boolean isMutable() {
        return false;
    }

    public Mut toMutable() {
        return new Mut(this.block, this.blockMeta, this.tileEntity);
    }

    public BlockInfo toImmutable() {
        return this;
    }

    public BlockInfo copy() {
        return new BlockInfo(this.block, this.blockMeta, this.tileEntity);
    }

    public static class Mut extends BlockInfo {

        public static final Mut SHARED = new Mut();

        public Mut() {
            this(Blocks.air);
        }

        public Mut(@NotNull Block block) {
            super(block);
        }

        public Mut(@NotNull Block block, int blockMeta) {
            super(block, blockMeta);
        }

        public Mut(@NotNull Block block, @Nullable TileEntity tileEntity) {
            super(block, tileEntity);
        }

        public Mut(@NotNull Block block, int blockMeta, @Nullable TileEntity tileEntity) {
            super(block, blockMeta, tileEntity);
        }

        @Override
        public Mut set(Block block, int blockMeta, TileEntity tile) {
            return (Mut) super.set(block, blockMeta, tile);
        }

        public Mut set(IBlockAccess world, BlockPos pos) {
            Block block = world.getBlock(pos.x, pos.y, pos.z);
            int blockMeta = world.getBlockMetadata(pos.x, pos.y, pos.z);
            TileEntity tile = null;
            if (block.hasTileEntity(blockMeta)) {
                tile = world.getTileEntity(pos.x, pos.y, pos.z);
            }
            return set(block, blockMeta, tile);
        }

        @Override
        public boolean isMutable() {
            return true;
        }

        @Override
        public Mut toMutable() {
            return this;
        }

        @Override
        public BlockInfo toImmutable() {
            return new BlockInfo(getBlock(), getBlockMeta(), getTileEntity());
        }

        @Override
        public Mut copy() {
            return new Mut(getBlock(), getBlockMeta(), getTileEntity());
        }
    }
}
