package com.cleanroommc.modularui.utils.fakeworld;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

@SuppressWarnings("all")
public class DummySaveHandler implements ISaveHandler, IPlayerFileData, IChunkLoader {

    @Nullable
    @Override
    public WorldInfo loadWorldInfo() {
        return null;
    }

    @Override
    public void checkSessionLock() {}

    @NotNull
    @Override
    public IChunkLoader getChunkLoader(@NotNull WorldProvider provider) {
        return this;
    }

    @NotNull
    @Override
    public IPlayerFileData getSaveHandler() {
        return this;
    }

    @Override
    public void saveWorldInfoWithPlayer(@NotNull WorldInfo worldInformation, @NotNull NBTTagCompound tagCompound) {}

    @Override
    public void saveWorldInfo(@NotNull WorldInfo worldInformation) {}

    @NotNull
    @Override
    public File getWorldDirectory() {
        return null;
    }

    @NotNull
    @Override
    public File getMapFileFromName(@NotNull String mapName) {
        return null;
    }

    @Nullable
    @Override
    public Chunk loadChunk(@NotNull World worldIn, int x, int z) {
        return null;
    }

    @Override
    public void saveChunk(@NotNull World worldIn, @NotNull Chunk chunkIn) {}

    @Override
    public void saveExtraChunkData(@NotNull World worldIn, @NotNull Chunk chunkIn) {}

    @Override
    public void chunkTick() {}

    @Override
    public void flush() {}

    @Override
    public void writePlayerData(@NotNull EntityPlayer player) {}

    @Nullable
    @Override
    public NBTTagCompound readPlayerData(@NotNull EntityPlayer player) {
        return null;
    }

    @NotNull
    @Override
    public String[] getAvailablePlayerDat() {
        return new String[0];
    }

    @Override
    public void saveExtraData() {

    }

    @Override
    public String getWorldDirectoryName() {
        return "";
    }
}
