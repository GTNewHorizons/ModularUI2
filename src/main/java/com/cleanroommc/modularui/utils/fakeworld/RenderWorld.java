package com.cleanroommc.modularui.utils.fakeworld;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;

public class RenderWorld implements IBlockAccess {

    private final ISchema schema;
    private final World world;

    public RenderWorld(ISchema schema) {
        this.schema = schema;
        this.world = schema.getWorld();
    }

    @Override
    public TileEntity getTileEntity(int x, int y, int z) {
        if (this.schema == null) return this.world.getTileEntity(x, y, z);
        BlockPos pos = new BlockPos(x, y, z);
        BlockInfo.Mut.SHARED.set(this.world, pos);
        return this.schema.getRenderFilter().test(pos, BlockInfo.Mut.SHARED) ? BlockInfo.Mut.SHARED.getTileEntity() : null;
    }

    @Override
    public int getLightBrightnessForSkyBlocks(int x, int y, int z, int lightValue) {
        return this.world.getLightBrightnessForSkyBlocks(x, y, z, lightValue);
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        if (this.schema == null) return this.world.getBlock(x, y, z);
        BlockPos pos = new BlockPos(x, y, z);
        BlockInfo.Mut.SHARED.set(this.world, pos);
        return this.schema.getRenderFilter().test(pos, BlockInfo.Mut.SHARED) ? BlockInfo.Mut.SHARED.getBlock() : Blocks.air;
    }

    @Override
    public int getBlockMetadata(int x, int y, int z) {
        if (this.schema == null) return this.world.getBlockMetadata(x, y, z);
        BlockPos pos = new BlockPos(x, y, z);
        BlockInfo.Mut.SHARED.set(this.world, pos);
        return this.schema.getRenderFilter().test(pos, BlockInfo.Mut.SHARED) ? BlockInfo.Mut.SHARED.getBlockMeta() : 0;
    }

    @Override
    public boolean isAirBlock(int x, int y, int z) {
        Block block = getBlock(x, y, z);
        return block.isAir(this.world, x, y, z);
    }

    @Override
    public BiomeGenBase getBiomeGenForCoords(int x, int z) {
        return this.world.getBiomeGenForCoords(x, z);
    }

    @Override
    public int isBlockProvidingPowerTo(int x, int y, int z, int directionIn) {
        return this.world.isBlockProvidingPowerTo(x, y, z, directionIn);
    }

    @Override
    public boolean isSideSolid(int x, int y, int z, ForgeDirection side, boolean _default) {
        if (x < -30000000 || z < -30000000 || x >= 30000000 || z >= 30000000) {
            return _default;
        }

        Chunk chunk = this.world.getChunkProvider().provideChunk(x >> 4, z >> 4);
        if (chunk == null || chunk.isEmpty()) {
            return _default;
        }
        return getBlock(x, y, z).isSideSolid(this, x, y, z, side);
    }

    @Override
    public int getHeight() {
        return this.world.getHeight();
    }

    @Override
    public boolean extendedLevelsInChunkCache() {
        return false;
    }
}
