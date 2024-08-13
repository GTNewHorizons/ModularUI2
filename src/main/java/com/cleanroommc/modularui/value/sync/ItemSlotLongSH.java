package com.cleanroommc.modularui.value.sync;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.api.IItemStackLong;
import com.cleanroommc.modularui.utils.item.ItemHandlerHelper;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.utils.item.ItemStackLong;
import com.cleanroommc.modularui.widgets.slot.ModularSlotLong;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public class ItemSlotLongSH extends SyncHandler {

    private final ModularSlotLong slot;
    private IItemStackLong lastStoredItem;
    private IItemStackLong lastStoredPhantomItem = null;

    @ApiStatus.Internal
    public ItemSlotLongSH(ModularSlotLong slot) {
        this.slot = slot;
    }

    @Override
    public void init(String key, PanelSyncManager syncHandler) {
        super.init(key, syncHandler);
        syncHandler.getContainer().registerSlot(syncHandler.getPanelName(), this.slot);
        IItemStackLong currentStack = getSlot().getStackLong();
        this.lastStoredItem = currentStack != null ? currentStack.copy() : null;
        if (isPhantom() && currentStack != null) {
            this.lastStoredPhantomItem = currentStack.copy();
            this.lastStoredPhantomItem.setStackSize(1);
        }
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        IItemStackLong itemStack = getSlot().getStackLong();
        if (itemStack == null && this.lastStoredItem == null) return;
        boolean onlyAmountChanged = false;
        if (init ||
                !ItemHandlerHelper.canItemStacksStack(this.lastStoredItem, itemStack) ||
                (onlyAmountChanged = itemStack.getStackSize() != this.lastStoredItem.getStackSize())) {
            getSlot().onSlotChangedRealLong(itemStack, onlyAmountChanged, false, init);
            if (onlyAmountChanged) {
                this.lastStoredItem.setStackSize(itemStack.getStackSize());
            } else {
                this.lastStoredItem = itemStack == null ? null : itemStack.copy();
            }
            final boolean finalOnlyAmountChanged = onlyAmountChanged;
            syncToClient(1, buffer -> {
                buffer.writeBoolean(finalOnlyAmountChanged);
                NetworkUtils.writeItemStackLong(buffer, itemStack);
                buffer.writeBoolean(init);
            });
        }
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        if (id == 1) {
            boolean onlyAmountChanged = buf.readBoolean();
            this.lastStoredItem = NetworkUtils.readItemStackLong(buf);
            getSlot().onSlotChangedRealLong(this.lastStoredItem, onlyAmountChanged, true, buf.readBoolean());
        } else if (id == 4) {
            setEnabled(buf.readBoolean(), false);
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        if (id == 2) {
            phantomClick(MouseData.readPacket(buf));
        } else if (id == 3) {
            phantomScroll(MouseData.readPacket(buf));
        } else if (id == 4) {
            setEnabled(buf.readBoolean(), false);
        } else if (id == 5) {
            if (!isPhantom()) return;
            ItemStack stack = NetworkUtils.readItemStack(buf);
            this.slot.putStack(stack);
        }
    }

    protected void phantomClick(MouseData mouseData) {
        ItemStack cursorStackItem = getSyncManager().getCursorItem();
        IItemStackLong cursorStack = cursorStackItem == null ? null : new ItemStackLong(cursorStackItem);
        IItemStackLong slotStack = getSlot().getStackLong();
        IItemStackLong stackToPut;
        if (cursorStack != null && slotStack != null && !ItemHandlerHelper.canItemStacksStack((cursorStack), slotStack)) {
            stackToPut = (cursorStack.copy());
            if (mouseData.mouseButton == 1) {
                stackToPut.setStackSize(1);
            }
            getSlot().putStackLong(stackToPut);
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
                stackToPut.setStackSize(1);
            }
            getSlot().putStackLong(stackToPut);
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
        IItemStackLong currentItem = this.slot.getStackLong();
        long amount = mouseData.mouseButton;
        if (mouseData.shift) amount *= 4;
        if (mouseData.ctrl) amount *= 16;
        if (mouseData.alt) amount *= 64;
        if (amount > 0 && currentItem == null && this.lastStoredPhantomItem != null) {
            IItemStackLong stackToPut = this.lastStoredPhantomItem.copy();
            stackToPut.setStackSize(amount);
            this.slot.putStackLong(stackToPut);
        } else {
            incrementStackCount(amount);
        }
    }

    public void incrementStackCount(long amount) {
        IItemStackLong stack = getSlot().getStackLong();
        if (stack == null) {
            return;
        }
        long oldAmount = stack.getStackSize();
        if (amount < 0) {
            amount = Math.max(0, oldAmount + amount);
        } else {
            if (Integer.MAX_VALUE - amount < oldAmount) {
                amount = Integer.MAX_VALUE;
            } else {
                long maxSize = getSlot().getSlotStackLimitLong();
                if (!this.slot.isIgnoreMaxStackSize() && stack.getMaxStackSize() < maxSize) {
                    maxSize = stack.getMaxStackSize();
                }
                amount = Math.min(oldAmount + amount, maxSize);
            }
        }
        if (oldAmount != amount) {
            stack = stack.copy();
            stack.setStackSize(amount);
            getSlot().putStackLong(stack);
        }
    }

    public void setEnabled(boolean enabled, boolean sync) {
        this.slot.setEnabled(enabled);
        if (sync) {
            sync(4, buffer -> buffer.writeBoolean(enabled));
        }
    }

    public void updateFromClient(IItemStackLong stack) {
        syncToServer(5, buf -> NetworkUtils.writeItemStackLong(buf, stack));
    }

    public ModularSlotLong getSlot() {
        return this.slot;
    }

    public boolean isItemValid(IItemStackLong itemStack) {
        return getSlot().isItemValidLong(itemStack);
    }

    public boolean isPhantom() {
        return this.slot.isPhantom();
    }

    @Nullable
    public String getSlotGroup() {
        return this.slot.getSlotGroupName();
    }
}
