package com.cleanroommc.modularui.utils.fakeworld;

import com.cleanroommc.modularui.ModularUI;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.*;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

import org.jetbrains.annotations.NotNull;

public class DummyWorld extends World {

    private static final WorldSettings DEFAULT_SETTINGS = new WorldSettings(
            1L, WorldSettings.GameType.SURVIVAL, true, false, WorldType.DEFAULT);

    public static final DummyWorld INSTANCE = new DummyWorld();

    public DummyWorld() {
        super(new DummySaveHandler(), "DummyServer", new WorldProviderSurface(), DEFAULT_SETTINGS, new Profiler());
        // Guarantee the dimension ID was not reset by the provider
        this.provider.setDimension(Integer.MAX_VALUE);
        int providerDim = this.provider.dimensionId;
        this.provider.registerWorld(this);
        this.provider.setDimension(providerDim);
        this.chunkProvider = this.createChunkProvider();
        this.calculateInitialSkylight();
        // this.calculateInitialWeather();
        // this.getWorldBorder().setSize(30000000);
        // De-allocate lightUpdateBlockList, checkLightFor uses this
        ObfuscationReflectionHelper.setPrivateValue(World.class, this, null,
                ModularUI.isDevEnv ? "lightUpdateBlockList" : "field_72994_J");
    }

    @Override
    public void notifyBlocksOfNeighborChange(int p_147441_1_, int p_147441_2_, int p_147441_3_, Block p_147441_4_, int p_147441_5_) {
        // NOOP - do not trigger forge events
    }

    @Override
    public void markAndNotifyBlock(int x, int y, int z, Chunk chunk, Block oldBlock, Block newBlock, int flag) {
        // NOOP - do not trigger forge events
    }

    @Override
    public void markBlockForUpdate(int p_147471_1_, int p_147471_2_, int p_147471_3_) {}

    @Override
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {}

    @Override
    public void notifyBlocksOfNeighborChange(int p_147459_1_, int p_147459_2_, int p_147459_3_, Block p_147459_4_) {}

    @NotNull
    @Override
    protected IChunkProvider createChunkProvider() {
        return new DummyChunkProvider(this);
    }

    @Override
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return chunkProvider.isChunkGeneratedAt(x, z);
    }

    @Override
    // De-allocated lightUpdateBlockList, default return
    public boolean updateLightByType(EnumSkyBlock p_147463_1_, int p_147463_2_, int p_147463_3_, int p_147463_4_) {
        return true;
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
