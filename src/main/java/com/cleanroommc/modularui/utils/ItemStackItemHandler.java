package com.cleanroommc.modularui.utils;

import com.cleanroommc.modularui.factory.PlayerInventoryGuiData;
import com.cleanroommc.modularui.utils.item.IItemHandlerModifiable;
import com.cleanroommc.modularui.utils.item.ItemHandlerHelper;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

// nh todo: this doesn't work due to `ItemStackItemHandler#container` being different object from actual item stored in inv
/**
 * An item handler whose slots are stored as an NBT list inside a single {@link ItemStack} container,
 * for example the item the player is currently holding while a GUI is open.
 */
public class ItemStackItemHandler implements IItemHandlerModifiable {

    private static final String KEY_ITEMS = "Items";

    private final Supplier<ItemStack> container;
    private final Consumer<ItemStack> containerUdater;
    private final int slots;

    /**
     * Creates a handler backed by the item the player used to open the GUI.
     *
     * @param data  GUI data holding the used item stack
     * @param slots number of slots
     */
    public ItemStackItemHandler(PlayerInventoryGuiData data, int slots) {
        this(data::getUsedItemStack, data::setUsedItemStack, slots);
    }

    /**
     * Creates a handler backed by a custom container stack.
     *
     * @param container      supplies the current container stack
     * @param containerUdater called with the container stack after its NBT is changed, so it can be written back
     * @param slots          number of slots
     */
    public ItemStackItemHandler(Supplier<ItemStack> container, Consumer<ItemStack> containerUdater, int slots) {
        this.container = container;
        this.containerUdater = containerUdater;
        this.slots = slots;
    }

    @Override
    public int getSlots() {
        return this.slots;
    }

    @Nullable
    @Override
    public ItemStack getStackInSlot(int slot) {
        validateSlotIndex(slot);
        NBTTagCompound item = getItemsNbt().getCompoundTagAt(slot);
        return item == null ? null : ItemStack.loadItemStackFromNBT(item);
    }

    @Override
    public void setStackInSlot(int slot, @Nullable ItemStack stack) {
        validateSlotIndex(slot);
        NBTTagList list = getItemsNbt();
        list.func_150304_a(slot, stack == null ? new NBTTagCompound() : stack.writeToNBT(new NBTTagCompound()));
        this.containerUdater.accept(this.container.get());
    }

    /**
     * Inserts as much of the stack as fits into the slot, merging with an existing stack if the items match.
     *
     * @param slot     slot index
     * @param stack    the stack to insert, may be null
     * @param simulate if true, nothing is changed
     * @return the remainder that did not fit, or null if everything was inserted
     */
    @Nullable
    @Override
    public ItemStack insertItem(int slot, @Nullable ItemStack stack, boolean simulate) {
        if (stack == null) return null;
        ItemStack existing = getStackInSlot(slot);

        int limit = getStackLimit(slot, stack);

        if (existing != null) {
            if (!ItemHandlerHelper.canItemStacksStack(stack, existing))
                return stack;

            limit -= existing.stackSize;
        }

        if (limit <= 0) return stack;

        boolean reachedLimit = stack.stackSize > limit;

        if (!simulate) {
            if (existing == null) {
                setStackInSlot(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
            } else {
                existing.stackSize += reachedLimit ? limit : stack.stackSize;
            }
            onContentsChanged(slot);
        }

        return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.stackSize - limit) : null;
    }

    /**
     * Extracts up to {@code amount} items from the slot, limited by the item's max stack size.
     *
     * @param slot     slot index
     * @param amount   maximum number of items to extract
     * @param simulate if true, nothing is changed
     * @return the extracted stack, or null if nothing was extracted
     */
    @Nullable
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) return null;

        ItemStack existing = getStackInSlot(slot);
        if (existing == null) return null;

        int toExtract = Math.min(amount, existing.getMaxStackSize());

        if (existing.stackSize <= toExtract) {
            if (!simulate) {
                setStackInSlot(slot, null);
                onContentsChanged(slot);
            }
            return existing;
        } else {
            if (!simulate) {
                setStackInSlot(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.stackSize - toExtract));
                onContentsChanged(slot);
            }

            return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    /**
     * Returns the maximum number of items of this stack type that can be placed in the slot.
     *
     * @param slot  slot index
     * @param stack the stack to be placed
     * @return the smaller of the slot limit and the item's max stack size
     */
    protected int getStackLimit(int slot, @NotNull ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }

    /**
     * Called after the contents of a slot change. Writes the container back via the updater.
     *
     * @param slot the slot that changed
     */
    protected void onContentsChanged(int slot) {
        this.containerUdater.accept(this.container.get());
    }

    /**
     * Returns the NBT item list of the container. Creates the tag compound and the list, filled with empty
     * entries for each slot, if they are missing. The container is written back when something is created.
     *
     * @return the list of slot NBT compounds
     */
    public NBTTagList getItemsNbt() {
        ItemStack stack = this.container.get();
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) {
            nbt = new NBTTagCompound();
            this.container.get().setTagCompound(nbt);
            this.containerUdater.accept(stack);
        }
        if (!nbt.hasKey(KEY_ITEMS)) {
            NBTTagList list = new NBTTagList();
            for (int i = 0; i < getSlots(); i++) {
                list.appendTag(new NBTTagCompound());
            }
            nbt.setTag(KEY_ITEMS, list);
            this.containerUdater.accept(stack);
        }
        return nbt.getTagList(KEY_ITEMS, Constants.NBT.TAG_COMPOUND);
    }

    /**
     * Checks that the slot index is within range.
     *
     * @param slot slot index to check
     * @throws RuntimeException if the slot is outside {@code [0, slots)}
     */
    protected void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= this.slots) {
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + this.slots + ")");
        }
    }
}
