package com.cleanroommc.modularui.widgets.slot;

import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.utils.item.IItemHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.stats.AchievementList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;

import cpw.mods.fml.common.FMLCommonHandler;

/**
 * Basically a copy of {@link net.minecraft.inventory.SlotCrafting} for {@link ModularSlot}.
 */
public class ModularCraftingSlot extends ModularSlot {

    private InventoryCraftingWrapper craftMatrix;
    private int amountCrafted;

    public ModularCraftingSlot(IItemHandler itemHandler, int index) {
        super(itemHandler, index);
    }

    /**
     * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
     */
    @Override
    public boolean isItemValid(ItemStack stack) {
        return false;
    }

    /**
     * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg. Returns the new
     * stack.
     */
    @Override
    public ItemStack decrStackSize(int amount) {
        if (this.getHasStack()) {
            this.amountCrafted += Math.min(amount, this.getStack().stackSize);
        }

        return super.decrStackSize(amount);
    }

    /**
     * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood. Typically increases an
     * internal count then calls onCrafting(item).
     */
    @Override
    protected void onCrafting(ItemStack stack, int amount) {
        this.amountCrafted += amount;
        this.onCrafting(stack);
    }

    /**
     * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood.
     */
    @Override
    protected void onCrafting(ItemStack p_75208_1_) {
        p_75208_1_.onCrafting(getPlayer().worldObj, getPlayer(), this.amountCrafted);
        this.amountCrafted = 0;

        if (p_75208_1_.getItem() == Item.getItemFromBlock(Blocks.crafting_table)) {
            getPlayer().addStat(AchievementList.buildWorkBench, 1);
        }

        if (p_75208_1_.getItem() instanceof ItemPickaxe) {
            getPlayer().addStat(AchievementList.buildPickaxe, 1);
        }

        if (p_75208_1_.getItem() == Item.getItemFromBlock(Blocks.furnace)) {
            getPlayer().addStat(AchievementList.buildFurnace, 1);
        }

        if (p_75208_1_.getItem() instanceof ItemHoe) {
            getPlayer().addStat(AchievementList.buildHoe, 1);
        }

        if (p_75208_1_.getItem() == Items.bread) {
            getPlayer().addStat(AchievementList.makeBread, 1);
        }

        if (p_75208_1_.getItem() == Items.cake) {
            getPlayer().addStat(AchievementList.bakeCake, 1);
        }

        if (p_75208_1_.getItem() instanceof ItemPickaxe && ((ItemPickaxe) p_75208_1_.getItem()).func_150913_i() != Item.ToolMaterial.WOOD) {
            getPlayer().addStat(AchievementList.buildBetterPickaxe, 1);
        }

        if (p_75208_1_.getItem() instanceof ItemSword) {
            getPlayer().addStat(AchievementList.buildSword, 1);
        }

        if (p_75208_1_.getItem() == Item.getItemFromBlock(Blocks.enchanting_table)) {
            getPlayer().addStat(AchievementList.enchantments, 1);
        }

        if (p_75208_1_.getItem() == Item.getItemFromBlock(Blocks.bookshelf)) {
            getPlayer().addStat(AchievementList.bookcase, 1);
        }
    }

    @Override
    public void onCraftShiftClick(EntityPlayer player, ItemStack stack) {
        if (!Platform.isStackEmpty(stack)) {
            player.dropPlayerItemWithRandomChoice(stack, false);
        }
    }

    @Override
    public void onPickupFromSlot(EntityPlayer player, ItemStack stack) {
        FMLCommonHandler.instance()
                .firePlayerCraftingEvent(player, stack, craftMatrix);
        onCrafting(stack);

        for (int i = 0; i < this.craftMatrix.getSizeInventory(); ++i) {
            ItemStack itemstack1 = this.craftMatrix.getStackInSlot(i);

            if (itemstack1 != null) {
                this.craftMatrix.decrStackSize(i, 1);

                if (itemstack1.getItem()
                        .hasContainerItem(itemstack1)) {
                    ItemStack itemstack2 = itemstack1.getItem()
                            .getContainerItem(itemstack1);

                    if (itemstack2 != null && itemstack2.isItemStackDamageable()
                            && itemstack2.getItemDamage() > itemstack2.getMaxDamage()) {
                        MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, itemstack2));
                        continue;
                    }

                    if (!itemstack1.getItem()
                            .doesContainerItemLeaveCraftingGrid(itemstack1)
                            || !player.inventory.addItemStackToInventory(itemstack2)) {
                        if (this.craftMatrix.getStackInSlot(i) == null) {
                            this.craftMatrix.setInventorySlotContents(i, itemstack2);
                        } else {
                            player.dropPlayerItemWithRandomChoice(itemstack2, false);
                        }
                    }
                }
            }
        }

        this.craftMatrix.notifyContainer();
    }

    public void updateResult(ItemStack stack) {
        putStack(stack);
        getSyncHandler().forceSyncItem();
    }

    public void setCraftMatrix(InventoryCraftingWrapper craftMatrix) {
        this.craftMatrix = craftMatrix;
    }
}
