package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.utils.item.ItemHandlerHelper;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Wraps a slot and handles interactions for phantom slots.
 * Use {@link ModularSlot} directly.
 */
// Changes made here probably should also be made to ItemSlotLongSH
public class ItemSlotSH extends SyncHandler {

    private final ModularSlot slot;
    private ItemStack lastStoredItem;
    private ItemStack lastStoredPhantomItem = null;
    private boolean registered = false;

    @ApiStatus.Internal
    public ItemSlotSH(ModularSlot slot) {
        this.slot = slot;
    }

    @Override
    public void init(String key, PanelSyncManager syncHandler) {
        super.init(key, syncHandler);
        if (!registered) {
            getSyncManager().getContainer().registerSlot(getSyncManager().getPanelName(), this.slot);
            this.registered = true;
        }
        ItemStack currentStack = getSlot().getStack();
        this.lastStoredItem = currentStack != null ? currentStack.copy() : null;
        if (isPhantom() && currentStack != null) {
            this.lastStoredPhantomItem = currentStack.copy();
            this.lastStoredPhantomItem.stackSize = 1;
        }
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        ItemStack itemStack = getSlot().getStack();
        if (itemStack == null && this.lastStoredItem == null) return;
        boolean onlyAmountChanged = false;
        if (init ||
                !ItemHandlerHelper.canItemStacksStack(this.lastStoredItem, itemStack) ||
                (onlyAmountChanged = itemStack.stackSize != this.lastStoredItem.stackSize)) {
            getSlot().onSlotChangedReal(itemStack, onlyAmountChanged, false, init);
            if (onlyAmountChanged) {
                this.lastStoredItem.stackSize = itemStack.stackSize;
            } else {
                this.lastStoredItem = itemStack == null ? null : itemStack.copy();
            }
            final boolean finalOnlyAmountChanged = onlyAmountChanged;
            syncToClient(1, buffer -> {
                buffer.writeBoolean(finalOnlyAmountChanged);
                NetworkUtils.writeItemStack(buffer, itemStack);
                buffer.writeBoolean(init);
            });
        }
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        if (id == 1) {
            boolean onlyAmountChanged = buf.readBoolean();
            this.lastStoredItem = NetworkUtils.readItemStack(buf);
            getSlot().onSlotChangedReal(this.lastStoredItem, onlyAmountChanged, true, buf.readBoolean());
        } else if (id == 4) {
            setEnabled(buf.readBoolean(), false);
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        if (id == 2) {
            phantomClick(MouseData.readPacket(buf));
        } else if (id == 3) {
            phantomScroll(MouseData.readPacket(buf));
        } else if (id == 4) {
            setEnabled(buf.readBoolean(), false);
        } else if (id == 5) {
            if (!isPhantom()) return;
            phantomClick(MouseData.readPacket(buf), NetworkUtils.readItemStack(buf));
        }
    }

    protected void phantomClick(MouseData mouseData) {
        phantomClick(mouseData, getSyncManager().getCursorItem());
    }

    protected void phantomClick(MouseData mouseData, ItemStack cursorStack) {
        ItemStack slotStack = getSlot().getStack();
        ItemStack stackToPut;
        if (cursorStack != null && slotStack != null && !ItemHandlerHelper.canItemStacksStack(cursorStack, slotStack)) {
            stackToPut = cursorStack.copy();
            if (mouseData.mouseButton == 1) {
                stackToPut.stackSize = 1;
            }
            stackToPut.stackSize = Math.min(stackToPut.stackSize, slot.getItemStackLimit(stackToPut));
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
                stackToPut = cursorStack.copy();
            }
            if (mouseData.mouseButton == 1) {
                stackToPut.stackSize = 1;
            }
            stackToPut.stackSize = Math.min(stackToPut.stackSize, slot.getItemStackLimit(stackToPut));
            getSlot().putStack(stackToPut);
            this.lastStoredPhantomItem = stackToPut.copy();
        } else {
            if (mouseData.mouseButton == 0) {
                if (mouseData.shift) {
                    this.slot.putStack(null);
                } else {
                    incrementStackCount(-1);
                }
            } else if (mouseData.mouseButton == 1) {
                incrementStackCount(1);
            }
        }
    }

    protected void phantomScroll(MouseData mouseData) {
        ItemStack currentItem = this.slot.getStack();
        int amount = mouseData.mouseButton;
        if (mouseData.shift) amount *= 4;
        if (mouseData.ctrl) amount *= 16;
        if (mouseData.alt) amount *= 64;
        if (amount > 0 && currentItem == null && this.lastStoredPhantomItem != null) {
            ItemStack stackToPut = this.lastStoredPhantomItem.copy();
            stackToPut.stackSize = amount;
            this.slot.putStack(stackToPut);
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
                if (!this.slot.isIgnoreMaxStackSize() && stack.getMaxStackSize() < maxSize) {
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

    public void setEnabled(boolean enabled, boolean sync) {
        this.slot.setEnabled(enabled);
        if (sync) {
            sync(4, buffer -> buffer.writeBoolean(enabled));
        }
    }

    public void updateFromClient(ItemStack stack, int button) {
        syncToServer(5, buf -> {
            MouseData mouseData = MouseData.create(button);
            mouseData.writeToPacket(buf);
            NetworkUtils.writeItemStack(buf, stack);
        });
    }

    public ModularSlot getSlot() {
        return this.slot;
    }

    public boolean isItemValid(ItemStack itemStack) {
        return getSlot().isItemValid(itemStack);
    }

    public boolean isPhantom() {
        return this.slot.isPhantom();
    }

    @Nullable
    public String getSlotGroup() {
        return this.slot.getSlotGroupName();
    }
}

