package com.cleanroommc.modularui.widgets.slot;

import static com.google.common.primitives.Ints.saturatedCast;

import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.api.IItemStackLong;
import com.cleanroommc.modularui.future.IItemHandlerLong;
import com.cleanroommc.modularui.utils.item.ItemStackLong;

import net.minecraft.item.ItemStack;

public class ModularSlotLong extends ModularSlot {

    private final IItemHandlerLong itemHandler;
    private final int index;
    private IOnSlotChangedLong changeListener = IOnSlotChangedLong.DEFAULT;
    private Predicate<IItemStackLong> filter = (item) -> true;

    public ModularSlotLong(IItemHandlerLong handler, int index) {
        this(handler, index, false);
    }

    public ModularSlotLong(IItemHandlerLong itemHandler, int index, boolean phantom) {
        super(itemHandler, index, phantom);
        this.itemHandler = itemHandler;
        this.index = index;
    }

    public boolean isItemValidLong(IItemStackLong stack) {
        return stack != null && filter.test(stack);
    }

    @Override
    public boolean isItemValid(@Nullable ItemStack stack) {
        return isItemValidLong(stack == null ? null : new ItemStackLong(stack)) && super.isItemValid(stack);
    }

    public IItemStackLong getStackLong() {
        return itemHandler.getStackInSlotLong(index);
    }

    public void putStackLong(IItemStackLong stack) {
        if (ItemStackLong.areItemStacksEqual(stack, getStackLong())) return;
        itemHandler.setStackInSlotLong(index, stack);
    }

    public long getSlotStackLimitLong() {
        return itemHandler.getSlotLimitLong(index);
    }

    @Override
    public int getSlotStackLimit() {
        return saturatedCast(getSlotStackLimitLong());
    }

    public long getItemStackLimitLong(@NotNull IItemStackLong stack) {
        return getSlotStackLimitLong();
    }

    @Override
    public int getItemStackLimit(@NotNull ItemStack stack) {
        return saturatedCast(getItemStackLimitLong(new ItemStackLong(stack)));
    }

    public void onSlotChangedRealLong(IItemStackLong stack, boolean onlyChangedAmount, boolean client, boolean init) {
        this.changeListener.onChange(stack, onlyChangedAmount, client, init);
    }

    @Override
    public void onSlotChangedReal(ItemStack itemStack, boolean onlyChangedAmount, boolean client, boolean init) {
        onSlotChangedRealLong(new ItemStackLong(itemStack), onlyChangedAmount,  client, init);
        super.onSlotChangedReal(itemStack, onlyChangedAmount, client, init);
    }

    public ModularSlotLong changeListener(IOnSlotChangedLong changeListener) {
        if (changeListener == null) {
            this.changeListener = IOnSlotChangedLong.DEFAULT;
            return this;
        }
        this.changeListener = changeListener;
        return this;
    }

    public ModularSlotLong filterLong(Predicate<IItemStackLong> filter) {
        this.filter = filter;
        if (this.filter == null) {
            this.filter = (item) -> true;
        }
        return this;
    }

}
