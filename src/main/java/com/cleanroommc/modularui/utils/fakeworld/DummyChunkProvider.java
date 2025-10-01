package com.cleanroommc.modularui.utils.fakeworld;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

import com.gtnewhorizon.gtnhlib.util.CoordinatePacker;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class DummyChunkProvider implements IChunkProvider {

    private final World world;
    private final Long2ObjectMap<Chunk> loadedChunks = new Long2ObjectOpenHashMap<>();

    public DummyChunkProvider(World world) {
        this.world = world;
    }

    @Override
    public Chunk loadChunk(int x, int z) {
        return loadedChunks.get(CoordinatePacker.pack(x, 0, z));
    }

    @NotNull
    @Override
    public Chunk provideChunk(int x, int z) {
        long chunkKey = CoordinatePacker.pack(x, 0, z);
        if (loadedChunks.containsKey(chunkKey))
            return loadedChunks.get(chunkKey);
        Chunk chunk = new Chunk(world, x, z);
        loadedChunks.put(chunkKey, chunk);
        return chunk;
    }

    @NotNull
    @Override
    public String makeString() {
        return "Dummy";
    }

    @Override
    public boolean chunkExists(int x, int z) {
        return true;
    }

    @Override
    public void populate(IChunkProvider p_73153_1_, int p_73153_2_, int p_73153_3_) {}

    @Override
    public boolean saveChunks(boolean p_73151_1_, IProgressUpdate p_73151_2_) {
        return false;
    }

    @Override
    public boolean unloadQueuedChunks() {
        return false;
    }

    @Override
    public boolean canSave() {
        return false;
    }

    @Override
    public List<BiomeGenBase.SpawnListEntry> getPossibleCreatures(EnumCreatureType p_73155_1_, int p_73155_2_, int p_73155_3_, int p_73155_4_) {
        return Collections.emptyList();
    }

    @Override
    public ChunkPosition func_147416_a(World p_147416_1_, String p_147416_2_, int p_147416_3_, int p_147416_4_, int p_147416_5_) {
        return null;
    }

    @Override
    public int getLoadedChunkCount() {
        return 0;
    }

    @Override
    public void recreateStructures(int p_82695_1_, int p_82695_2_) {}

    @Override
    public void saveExtraData() {}
}
