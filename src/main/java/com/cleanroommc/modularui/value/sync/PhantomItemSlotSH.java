package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.utils.item.ItemHandlerHelper;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import cpw.mods.fml.relauncher.Side;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;

/**
 * Wraps a slot and handles interactions for phantom slots.
 * Use {@link ModularSlot} directly.
 */
public class PhantomItemSlotSH extends ItemSlotSH {

    public static final int SYNC_CLICK = 100;
    public static final int SYNC_SCROLL = 101;
    public static final int SYNC_ITEM_SIMPLE = 102;

    private ItemStack lastStoredPhantomItem = null;

    @ApiStatus.Internal
    public PhantomItemSlotSH(ModularSlot slot) {
        super(slot);
    }

    @Override
    public void init(String key, PanelSyncManager syncHandler) {
        super.init(key, syncHandler);
        ItemStack currentStack = getSlot().getStack();
        if (isPhantom() && currentStack != null) {
            this.lastStoredPhantomItem = currentStack.copy();
            this.lastStoredPhantomItem.stackSize = 1;
        }
    }

    @Override
    protected void onSlotUpdate(ItemStack stack, boolean onlyAmountChanged, boolean client, boolean init) {
        getSlot().putStack(stack);
        if (!onlyAmountChanged && stack != null) {
            // store last non-empty stack for later
            this.lastStoredPhantomItem = stack.copy();
            this.lastStoredPhantomItem.stackSize = 1;
        }
        super.onSlotUpdate(stack, onlyAmountChanged, client, init);
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        super.readOnServer(id, buf);
        if (id == SYNC_CLICK) {
            phantomClick(MouseData.readPacket(buf));
        } else if (id == SYNC_SCROLL) {
            phantomScroll(MouseData.readPacket(buf));
        } else if (id == SYNC_ITEM_SIMPLE) {
            if (!isPhantom()) return;
            ItemStack itemStack = NetworkUtils.readItemStack(buf);
            int button = buf.readVarIntFromBuffer(); // TODO whats this 1.12
            phantomClick(new MouseData(Side.SERVER, button, false, false, false), itemStack);
        }
    }

    public void updateFromClient(ItemStack stack, int button) {
        syncToServer(SYNC_ITEM_SIMPLE, buf -> {
            NetworkUtils.writeItemStack(buf, stack);
            buf.writeVarIntToBuffer(button); // TODO whats this 1.12
        });
    }

    protected void phantomClick(MouseData mouseData) {
        phantomClick(mouseData, getSyncManager().getCursorItem());
    }

    protected void phantomClick(MouseData mouseData, ItemStack cursorStack) {
        ItemStack slotStack = getSlot().getStack();
        ItemStack stackToPut;
        if (cursorStack != null && slotStack != null && !ItemHandlerHelper.canItemStacksStack(cursorStack, slotStack)) {
            if (!isItemValid(cursorStack)) return;
            stackToPut = cursorStack.copy();
            if (mouseData.mouseButton == 1) {
                stackToPut.stackSize = 1;
            }
            stackToPut.stackSize = Math.min(stackToPut.stackSize, getSlot().getItemStackLimit(stackToPut));
            getSlot().putStack(stackToPut);
            this.lastStoredPhantomItem = stackToPut.copy();
        } else if (slotStack == null) {
            if (cursorStack == null) {
                if (mouseData.mouseButton == 1 && this.lastStoredPhantomItem != null) {
                    stackToPut = this.lastStoredPhantomItem.copy();
                } else {
                    return;
                }
            } else {
                if (!isItemValid(cursorStack)) return;
                stackToPut = cursorStack.copy();
            }
            if (mouseData.mouseButton == 1) {
                stackToPut.stackSize = 1;
            }
            stackToPut.stackSize = Math.min(stackToPut.stackSize, getSlot().getItemStackLimit(stackToPut));
            getSlot().putStack(stackToPut);
            this.lastStoredPhantomItem = stackToPut.copy();
        } else {
            if (mouseData.mouseButton == 0) {
                if (mouseData.shift) {
                    getSlot().putStack(null);
                } else {
                    incrementStackCount(-1);
                }
            } else if (mouseData.mouseButton == 1) {
                incrementStackCount(1);
            }
        }
    }

    protected void phantomScroll(MouseData mouseData) {
        ItemStack currentItem = getSlot().getStack();
        int amount = mouseData.mouseButton;
        if (mouseData.shift) amount *= 4;
        if (mouseData.ctrl) amount *= 16;
        if (mouseData.alt) amount *= 64;
        if (amount > 0 && currentItem == null && this.lastStoredPhantomItem != null) {
            ItemStack stackToPut = this.lastStoredPhantomItem.copy();
            stackToPut.stackSize = amount;
            getSlot().putStack(stackToPut);
        } else {
            incrementStackCount(amount);
        }
    }

    public void incrementStackCount(int amount) {
        ItemStack stack = getSlot().getStack();
        if (stack == null) {
            return;
        }
        int oldAmount = stack.stackSize;
        if (amount < 0) {
            amount = Math.max(0, oldAmount + amount);
        } else {
            if (Integer.MAX_VALUE - amount < oldAmount) {
                amount = Integer.MAX_VALUE;
            } else {
                int maxSize = getSlot().getSlotStackLimit();
                if (!getSlot().isIgnoreMaxStackSize() && stack.getMaxStackSize() < maxSize) {
                    maxSize = stack.getMaxStackSize();
                }
                amount = Math.min(oldAmount + amount, maxSize);
            }
        }
        if (oldAmount != amount) {
            stack = stack.copy();
            stack.stackSize = amount;
            if (amount < 1) {
                stack = null;
            }
            getSlot().putStack(stack);
        }
    }

    @Override
    public boolean isPhantom() {
        return true;
    }
}
