package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.factory.GuiFactories;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.registry.GameRegistry;

import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TestBlock extends Block implements ITileEntityProvider {

    public static final Block testBlock = new TestBlock(TestTile::new);
    public static final Block testBlock2 = new TestBlock(TestTile2::new);
    public static final ItemBlock testItemBlock = new ItemBlock(testBlock);
    public static final ItemBlock testItemBlock2 = new ItemBlock(testBlock2);

    public static void preInit() {
        testBlock.setBlockName("test_block").setBlockTextureName("stone");
        GameRegistry.registerBlock(testBlock, "test_block");
        GameRegistry.registerTileEntity(TestTile.class, "test_block");
        testBlock2.setBlockName("test_block_2").setBlockTextureName("dirt");
        GameRegistry.registerBlock(testBlock2, "test_block_2");
        GameRegistry.registerTileEntity(TestTile2.class, "test_block_2");
        TestItem.testItem.setUnlocalizedName("test_item").setTextureName("diamond");
        GameRegistry.registerItem(TestItem.testItem, "test_item");
    }

    private final Supplier<TileEntity> tileEntitySupplier;

    public TestBlock(Supplier<TileEntity> tileEntitySupplier) {
        super(Material.rock);
        this.tileEntitySupplier = tileEntitySupplier;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
        return this.tileEntitySupplier.get();
    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer playerIn, int side, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            GuiFactories.tileEntity().open(playerIn, x, y, z);
        }
        return true;
    }
}
