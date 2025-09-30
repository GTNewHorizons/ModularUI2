package com.cleanroommc.modularui.utils.fakeworld;

import com.cleanroommc.modularui.ModularUI;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import cpw.mods.fml.common.registry.GameRegistry;

import com.google.common.collect.AbstractIterator;
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.chars.CharArraySet;
import it.unimi.dsi.fastutil.chars.CharSet;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

public class ArraySchema implements ISchema {

    private static final BlockPos ORIGIN = new BlockPos(0, 0, 0);

    public static Builder builder() {
        return new Builder();
    }

    public static ArraySchema of(Entity entity, int radius) {
        return of(entity.worldObj, (int) entity.posX, (int) (entity.posY + 0.5), (int) entity.posZ, radius);
    }

    public static ArraySchema of(World world, int centerX, int centerY, int centerZ, int radius) {
        int s = 2 * radius + 1;
        BlockInfo[][][] blocks = new BlockInfo[s][s][s];
        BlockPos pos = new BlockPos();
        BlockPosUtil.add(pos.set(centerX, centerY, centerZ), -radius, -radius, -radius);
        for (int x = 0; x < s; x++) {
            for (int y = 0; y < s; y++) {
                for (int z = 0; z < s; z++) {
                    blocks[x][y][z] = BlockInfo.of(world, pos);
                    BlockPosUtil.add(pos, 0, 0, 1);
                }
                BlockPosUtil.add(pos, 0, 1, -s);
            }
            BlockPosUtil.add(pos, 1, -s, 0);
        }
        return new ArraySchema(blocks);
    }

    public static ArraySchema of(World world, int ax, int ay, int az, int bx, int by, int bz) {
        int x0 = Math.min(ax, bx), y0 = Math.min(ay, by), z0 = Math.min(az, bz);
        int x1 = Math.max(ax, bx), y1 = Math.max(ay, by), z1 = Math.max(az, bz);
        x0--;
        y0--;
        z0--;
        BlockInfo[][][] blocks = new BlockInfo[x1 - x0][y1 - y0][z1 - z0];
        for (BlockPos pos : BlockPos.getAllInBox(x0, y0, z0, x1, y1, z1)) {
            blocks[pos.getX() - x0][pos.getY() - y0][pos.getZ() - z0] = BlockInfo.of(world, pos);
        }
        return new ArraySchema(blocks);
    }

    private final World world;
    private final BlockInfo[][][] blocks;
    private BiPredicate<BlockPos, BlockInfo> renderFilter = (__, ___) -> true;
    private final Vector3d center;

    public ArraySchema(BlockInfo[][][] blocks) {
        this.blocks = blocks;
        this.world = new DummyWorld();
        BlockPos current = new BlockPos();
        BlockPos max = new BlockPos(BlockPosUtil.MIN.x, BlockPosUtil.MIN.y, BlockPosUtil.MAX.z);
        for (int x = 0; x < blocks.length; x++) {
            for (int y = 0; y < blocks[x].length; y++) {
                for (int z = 0; z < blocks[x][y].length; z++) {
                    BlockInfo block = blocks[x][y][z];
                    if (block == null) continue;
                    current.set(x, y, z);
                    BlockPosUtil.setMax(max, current);
                    block.apply(this.world, current);
                }
            }
        }
        this.center = BlockPosUtil.getCenterD(ORIGIN, BlockPosUtil.add(max, 1, 1, 1));
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public Vector3d getFocus() {
        return center;
    }

    @Override
    public BlockPos getOrigin() {
        return ORIGIN;
    }

    @Override
    public void setRenderFilter(@Nullable BiPredicate<BlockPos, BlockInfo> renderFilter) {
        this.renderFilter = renderFilter;
    }

    @Override
    public @Nullable BiPredicate<BlockPos, BlockInfo> getRenderFilter() {
        return renderFilter;
    }

    @NotNull
    @Override
    public Iterator<Map.Entry<BlockPos, BlockInfo>> iterator() {
        return new AbstractIterator<>() {

            private final BlockPos pos = new BlockPos();
            private final MutablePair<BlockPos, BlockInfo> pair = new MutablePair<>(pos, null);
            private int x = 0, y = 0, z = -1;

            @Override
            protected Map.Entry<BlockPos, BlockInfo> computeNext() {
                BlockInfo info;
                while (true) {
                    if (++z >= blocks[x][y].length) {
                        z = 0;
                        if (++y >= blocks[x].length) {
                            y = 0;
                            if (++x >= blocks.length) {
                                return endOfData();
                            }
                        }
                    }
                    pos.set(x, y, z);
                    info = blocks[x][y][z];
                    if (info != null && renderFilter.test(pos, info)) {
                        pair.setRight(info);
                        return pair;
                    }
                }
            }
        };
    }

    public static class Builder {

        private final List<String[]> tensor = new ArrayList<>();
        private final Char2ObjectMap<BlockInfo> blockMap = new Char2ObjectOpenHashMap<>();

        public Builder() {
            blockMap.put(' ', BlockInfo.EMPTY);
            blockMap.put('#', BlockInfo.EMPTY);
        }

        public Builder layer(String... layer) {
            this.tensor.add(layer);
            return this;
        }

        public Builder where(char c, BlockInfo info) {
            this.blockMap.put(c, info);
            return this;
        }

        public Builder whereAir(char c) {
            return where(c, BlockInfo.EMPTY);
        }

        public Builder where(char c, Block block) {
            return where(c, new BlockInfo(block));
        }

        public Builder where(char c, Block block, int blockMeta) {
            return where(c, new BlockInfo(block, blockMeta));
        }

        public Builder where(char c, Block block, TileEntity tile) {
            return where(c, new BlockInfo(block, tile));
        }

        public Builder where(char c, Block block, int blockMeta, TileEntity tile) {
            return where(c, new BlockInfo(block, blockMeta, tile));
        }

        public Builder where(char c, ResourceLocation registryName, int stateMeta) {
            Block block = GameRegistry.findBlock(registryName.getResourceDomain(), registryName.getResourcePath());
            if (block == null) throw new IllegalArgumentException("Block with name " + registryName + " doesn't exist!");
            return where(c, new BlockInfo(block, stateMeta));
        }

        public Builder where(char c, ResourceLocation registryName) {
            return where(c, registryName, 0);
        }

        public Builder where(char c, String registryName, int stateMeta) {
            return where(c, new ResourceLocation(registryName), stateMeta);
        }

        public Builder where(char c, String registryName) {
            return where(c, new ResourceLocation(registryName), 0);
        }

        private void validate() {
            if (this.tensor.isEmpty()) {
                throw new IllegalArgumentException("no block matrix defined");
            }
            List<String> errors = new ArrayList<>();
            CharSet checkedChars = new CharArraySet();
            int layerSize = this.tensor.get(0).length;
            for (int x = 0; x < this.tensor.size(); x++) {
                String[] xLayer = this.tensor.get(x);
                if (xLayer.length == 0) {
                    errors.add(String.format("Layer %s is empty. This is not right", x + 1));
                } else if (xLayer.length != layerSize) {
                    errors.add(String.format("Invalid x-layer size. Expected %s, but got %s at layer %s", layerSize, xLayer.length, x + 1));
                }
                int rowSize = xLayer[0].length();
                for (int y = 0; y < xLayer.length; y++) {
                    String yRow = xLayer[y];
                    if (yRow.isEmpty()) {
                        errors.add(String.format("Row %s in layer %s is empty. This is not right", y + 1, x + 1));
                    } else if (yRow.length() != rowSize) {
                        errors.add(String.format("Invalid x-layer size. Expected %s, but got %s at row %s in layer %s", layerSize, xLayer.length, y + 1, x + 1));
                    }
                    for (int z = 0; z < yRow.length(); z++) {
                        char zChar = yRow.charAt(z);
                        if (!checkedChars.contains(zChar)) {
                            if (!this.blockMap.containsKey(zChar)) {
                                errors.add(String.format("Found char '%s' at char %s in row %s in layer %s, but character was not found in map!", zChar, z + 1, y + 1, x + 1));
                            }
                            checkedChars.add(zChar);
                        }
                    }
                }
            }
            if (!errors.isEmpty()) {
                ModularUI.LOGGER.error("Error validating ArrayScheme BlockArray:");
                for (String e : errors) ModularUI.LOGGER.error("  - {}", e);
                throw new IllegalArgumentException("The ArraySchema builder was misconfigured. See message above.");
            }
        }

        public ArraySchema build() {
            validate();
            BlockInfo[][][] blocks = new BlockInfo[this.tensor.size()][this.tensor.get(0).length][this.tensor.get(0)[0].length()];
            for (int x = 0; x < this.tensor.size(); x++) {
                String[] xLayer = this.tensor.get(x);
                for (int y = 0; y < xLayer.length; y++) {
                    String yRow = xLayer[y];
                    for (int z = 0; z < yRow.length(); z++) {
                        char zChar = yRow.charAt(z);
                        BlockInfo info = this.blockMap.get(zChar);
                        if (info == null || info == BlockInfo.EMPTY) continue; // null -> any allowed -> don't need to check
                        blocks[x][y][z] = info;
                    }
                }
            }
            return new ArraySchema(blocks);
        }
    }
}
