package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.utils.IMultiFluidTankHandler;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.utils.fluid.FluidInteractions;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidTank;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluidSlotSyncHandler extends ValueSyncHandler<FluidStack> {

    public static boolean isFluidEmpty(@Nullable FluidStack fluidStack) {
        return fluidStack == null || fluidStack.amount <= 0;
    }

    @Nullable
    public static FluidStack copyFluid(@Nullable FluidStack fluidStack) {
        return isFluidEmpty(fluidStack) ? null : fluidStack.copy();
    }

    public static final int SYNC_CLICK = 1;
    public static final int SYNC_SCROLL = 2;
    public static final int SYNC_CONTROLS_AMOUNT = 3;

    @Nullable
    private FluidStack cache;
    private final IFluidTank fluidTank;
    private boolean canFillSlot = true, canDrainSlot = true, controlsAmount = true, phantom = false;
    @Nullable
    private FluidStack lastStoredPhantomFluid;

    public FluidSlotSyncHandler(IFluidTank fluidTank) {
        this.fluidTank = fluidTank;
    }

    public FluidSlotSyncHandler(IMultiFluidTankHandler fluidHandler, int index) {
        this(fluidHandler.getFluidTank(index));
    }

    @Nullable
    @Override
    public FluidStack getValue() {
        return this.cache;
    }

    @Override
    public void setValue(@Nullable FluidStack value, boolean setSource, boolean sync) {
        this.cache = copyFluid(value);
        if (setSource) {
            this.fluidTank.drain(Integer.MAX_VALUE, true);
            if (!isFluidEmpty(value)) {
                this.fluidTank.fill(value.copy(), true);
            }
        }
        onValueChanged();
        if (sync) sync();
    }

    public boolean needsSync() {
        FluidStack current = this.fluidTank.getFluid();
        if (current == this.cache) return false;
        if (current == null || this.cache == null) return true;
        return current.amount != this.cache.amount || !current.isFluidEqual(this.cache);
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (isFirstSync || needsSync()) {
            setValue(this.fluidTank.getFluid(), false, false);
            return true;
        }
        return false;
    }

    @Override
    public Class<FluidStack> getValueType() {
        return FluidStack.class;
    }

    @Override
    public void notifyUpdate() {
        setValue(this.fluidTank.getFluid(), false, true);
    }

    @Override
    public void write(PacketBuffer buffer) {
        NetworkUtils.writeFluidStack(buffer, this.cache);
    }

    @Override
    public void read(PacketBuffer buffer) {
        setValue(NetworkUtils.readFluidStack(buffer), true, false);
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        if (id == SYNC_VALUE) {
            read(buf);
        } else if (id == SYNC_CONTROLS_AMOUNT) {
            this.controlsAmount = buf.readBoolean();
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        if (id == SYNC_VALUE) {
            if (this.phantom) {
                read(buf);
            }
        } else if (id == SYNC_CLICK) {
            if (this.phantom) {
                tryClickPhantom(MouseData.readPacket(buf));
            } else {
                tryClickContainer(MouseData.readPacket(buf));
            }
        } else if (id == SYNC_SCROLL) {
            if (this.phantom) {
                tryScrollPhantom(MouseData.readPacket(buf));
            }
        } else if (id == SYNC_CONTROLS_AMOUNT) {
            this.controlsAmount = buf.readBoolean();
        } else if (id == 4) {
            MouseData mouseData = MouseData.readPacket(buf);
            ItemStack draggedStack = NetworkUtils.readItemStack(buf);
            if (this.phantom && draggedStack != null) {
                tryClickPhantom(mouseData, draggedStack);
            }
        }
    }

    private void tryClickContainer(MouseData mouseData) {
        boolean processFullStack = mouseData.shift;
        ItemStack heldItem = getSyncManager().getCursorItem();
        if (heldItem == null || heldItem.stackSize == 0) {
            return;
        }

        ItemStack heldItemSizedOne = heldItem.copy();
        heldItemSizedOne.stackSize = 1;
        FluidStack currentFluid = fluidTank.getFluid();
        FluidStack heldFluid = FluidInteractions.getFluidForItem(heldItemSizedOne);
        if (heldFluid != null && heldFluid.amount <= 0) {
            heldFluid = null;
        }

        if (currentFluid == null) {
            if (!canFillSlot) {
                return;
            }
            if (heldFluid == null) {
                return;
            }
            fillFluid(heldFluid, processFullStack);
            return;
        }

        if (heldFluid != null && fluidTank.getFluidAmount() < fluidTank.getCapacity()) {
            if (canFillSlot) {
                fillFluid(heldFluid, processFullStack);
                return;
            }
            if (!canDrainSlot) {
                return;
            }
            drainFluid(heldFluid, processFullStack);
        } else {
            if (!canDrainSlot) {
                return;
            }
            drainFluid(heldFluid, processFullStack);
        }
    }

    private void tryClickPhantom(MouseData mouseData) {
        ItemStack cursorStack = getSyncManager().getCursorItem();
        tryClickPhantom(mouseData, cursorStack);
    }

    private void tryClickPhantom(MouseData mouseData, ItemStack cursorStack) {
        FluidStack currentFluid = fluidTank.getFluid();

        if (mouseData.mouseButton == 0) {
            if (cursorStack == null) {
                if (canDrainSlot) {
                    fluidTank.drain(mouseData.shift ? Integer.MAX_VALUE : 1000, true);
                }
            } else {
                ItemStack heldItemSizedOne = cursorStack.copy();
                heldItemSizedOne.stackSize = 1;
                FluidStack heldFluid = FluidInteractions.getFluidForItem(heldItemSizedOne);
                if ((controlsAmount || currentFluid == null) && heldFluid != null) {
                    fillPhantom(heldFluid);
                } else {
                    if (canDrainSlot) {
                        fluidTank.drain(mouseData.shift ? Integer.MAX_VALUE : 1000, true);
                        // "Swap" fluid
                        if (!controlsAmount && heldFluid != null && fluidTank.getFluidAmount() <= 0) {
                            fillPhantom(heldFluid);
                        }
                    }
                }
            }
        } else if (mouseData.mouseButton == 1) {
            if (canFillSlot) {
                if (currentFluid != null) {
                    if (controlsAmount) {
                        FluidStack toFill = currentFluid.copy();
                        toFill.amount = 1000;
                        fluidTank.fill(toFill, true);
                    }
                } else if (lastStoredPhantomFluid != null) {
                    FluidStack toFill = lastStoredPhantomFluid.copy();
                    toFill.amount = controlsAmount ? 1000 : 1;
                    fluidTank.fill(lastStoredPhantomFluid, true);
                }
            }
        } else if (mouseData.mouseButton == 2 && currentFluid != null && canDrainSlot) {
            fluidTank.drain(mouseData.shift ? Integer.MAX_VALUE : 1000, true);
        }
    }

    private void fillPhantom(@NotNull FluidStack heldFluid) {
        if (canFillSlot) {
            if (!controlsAmount) {
                heldFluid.amount = 1;
            }
            if (fluidTank.fill(heldFluid, true) > 0) {
                lastStoredPhantomFluid = heldFluid.copy();
            }
        }
    }

    protected void drainFluid(@Nullable FluidStack heldFluid, boolean processFullStack) {
        FluidStack initialFluid = fluidTank.getFluid();
        if (initialFluid == null) {
            return;
        }

        ItemStack heldItem = getSyncManager().getCursorItem();
        if (heldItem == null || heldItem.stackSize == 0) {
            return;
        }

        ItemStack heldItemSizedOne = heldItem.copy();
        heldItemSizedOne.stackSize = 1;

        if (heldItem.getItem() instanceof IFluidContainerItem container) {
            int containerCapacity = container.getCapacity(heldItemSizedOne);
            int containerAmount = heldFluid != null ? heldFluid.amount : 0;
            boolean soundPlayed = false;

            // 1. Try to fill some containers completely
            if (heldItem.stackSize > 1) {
                int drained = batchDrainFluidToContainers(heldItem, heldFluid, processFullStack);

                if (drained > 0) {
                    playSound(initialFluid, false);
                    soundPlayed = true;
                }

                // Return if we already filled one container and we don't want to fill others
                if (drained > 0 && !processFullStack) {
                    return;
                }

                // Return if all the containers are filled.
                // Note: stackSize = 0 means that the stack got replaced as per replaceCursorItemStack implementation
                if (heldItem.stackSize == 0) {
                    return;
                }
            }

            // 2. Try to fill one container in a stack
            int amountToFill = Math.min(containerCapacity - containerAmount, fluidTank.getFluidAmount());
            if (amountToFill <= 0) {
                return;
            }

            ItemStack itemToFill = heldItemSizedOne.copy();
            int filled = container.fill(itemToFill, new FluidStack(initialFluid, amountToFill), true);

            if (filled > 0) {
                fluidTank.drain(filled, true);
                replaceCursorItemStack(itemToFill);

                if (!soundPlayed) {
                    playSound(initialFluid, false);
                }
            }
        } else {
            int filled = this.batchDrainFluidToContainers(heldItem, heldFluid, processFullStack);
            if (filled > 0) {
                playSound(initialFluid, false);
            }
        }
    }

    private int batchDrainFluidToContainers(ItemStack heldItem, @Nullable FluidStack heldFluid, boolean processFullStack) {
        ItemStack heldItemSizedOne = heldItem.copy();
        heldItemSizedOne.stackSize = 1;

        FluidStack tankFluid = fluidTank.getFluid();
        ItemStack fullContainer = FluidInteractions.getFullFluidContainer(heldItemSizedOne, tankFluid);
        if (fullContainer == null) {
            return 0;
        }

        FluidStack fullContainerFluid = FluidInteractions.getFluidForItem(fullContainer);
        if (fullContainerFluid == null) {
            return 0;
        }

        int amountToFill = fullContainerFluid.amount - (heldFluid != null ? heldFluid.amount : 0);
        if (amountToFill <= 0) {
            return 0;
        }

        int containersToFill = Math.min(tankFluid.amount / amountToFill, heldItem.stackSize);

        if (!processFullStack && containersToFill > 1) {
            containersToFill = 1;
        }

        if (containersToFill == 0) {
            return 0;
        }

        fullContainer.stackSize = containersToFill;
        replaceCursorItemStack(fullContainer);

        int amountToDrain = containersToFill * amountToFill;
        fluidTank.drain(amountToDrain, true);
        return amountToDrain;
    }

    protected void fillFluid(@NotNull FluidStack heldFluid, boolean processFullStack) {
        FluidStack currentFluid = fluidTank.getFluid();
        if (currentFluid != null && !currentFluid.isFluidEqual(heldFluid)) {
            return;
        }

        ItemStack heldItem = getSyncManager().getCursorItem();
        if (heldItem == null || heldItem.stackSize == 0) {
            return;
        }

        ItemStack heldItemSizedOne = heldItem.copy();
        heldItemSizedOne.stackSize = 1;

        if (heldItem.getItem() instanceof IFluidContainerItem container) {
            boolean soundPlayed = false;

            // 1. Try to empty some filled containers completely
            if (heldItem.stackSize > 1) {
                int filled = batchFillFluidFromContainers(heldItem, heldFluid, processFullStack);

                if (filled > 0) {
                    playSound(heldFluid, true);
                    soundPlayed = true;
                }

                // Return if we already drained one container and we don't want to drain others
                if (filled > 0 && !processFullStack) {
                    return;
                }

                // Return if there are no filled containers left.
                // Note: stackSize = 0 means that the stack got replaced as per replaceCursorItemStack implementation
                if (heldItem.stackSize == 0) {
                    return;
                }
            }

            // 2. Try to drain one container in a stack
            int freeSpace = fluidTank.getCapacity() - fluidTank.getFluidAmount();
            int amountToDrain = Math.min(heldFluid.amount, freeSpace);
            if (amountToDrain <= 0) {
                return;
            }

            ItemStack itemToDrain = heldItemSizedOne.copy();
            FluidStack drained = container.drain(itemToDrain, amountToDrain, true);

            if (drained != null && drained.amount > 0) {
                fluidTank.fill(drained, true);
                replaceCursorItemStack(itemToDrain);

                if (!soundPlayed) {
                    playSound(heldFluid, true);
                }
            }
        } else {
            int filled = this.batchFillFluidFromContainers(heldItem, heldFluid, processFullStack);
            if (filled > 0) {
                playSound(heldFluid, true);
            }
        }
    }

    private int batchFillFluidFromContainers(ItemStack heldItem, FluidStack heldFluid, boolean processFullStack) {
        ItemStack heldItemSizedOne = heldItem.copy();
        heldItemSizedOne.stackSize = 1;

        ItemStack emptyContainer = FluidInteractions.getEmptyFluidContainer(heldItemSizedOne);
        if (emptyContainer == null) {
            return 0;
        }

        int freeSpace = fluidTank.getCapacity() - fluidTank.getFluidAmount();
        int containersToEmpty = Math.min(freeSpace / heldFluid.amount, heldItem.stackSize);

        if (!processFullStack && containersToEmpty > 1) {
            containersToEmpty = 1;
        }

        if (containersToEmpty == 0) {
            return 0;
        }

        emptyContainer.stackSize = containersToEmpty;
        replaceCursorItemStack(emptyContainer);

        int amountToFill = heldFluid.amount * containersToEmpty;
        fluidTank.fill(new FluidStack(heldFluid.getFluid(), amountToFill), true);
        return amountToFill;
    }

    public void tryScrollPhantom(MouseData mouseData) {
        FluidStack currentFluid = this.fluidTank.getFluid();
        int amount = mouseData.mouseButton;
        if (mouseData.shift) {
            amount *= 10;
        }
        if (mouseData.ctrl) {
            amount *= 100;
        }
        if (mouseData.alt) {
            amount *= 1000;
        }
        if (currentFluid == null) {
            if (amount > 0 && this.lastStoredPhantomFluid != null) {
                FluidStack toFill = this.lastStoredPhantomFluid.copy();
                toFill.amount = this.controlsAmount ? amount : 1;
                this.fluidTank.fill(toFill, true);
            }
            return;
        }
        if (amount > 0 && this.controlsAmount) {
            FluidStack toFill = currentFluid.copy();
            toFill.amount = amount;
            this.fluidTank.fill(toFill, true);
        } else if (amount < 0) {
            this.fluidTank.drain(-amount, true);
        }
    }

    /**
     * In 1.7.10 placing water or lava does not play sound, so we do nothing here.
     * Override if you want to play something.
     */
    private void playSound(FluidStack fluid, boolean fill) {
    }

    /**
     * Replaces heldStack with resultStack.
     * If player held more items in stack, remaining amount will be kept in its hand.
     * Guarantees mutating original heldStack stackSize even if it has to be 0.
     * Requires heldStack.stackSize >= resultStack.stackSize
     */
    protected void replaceCursorItemStack(ItemStack resultStack) {
        EntityPlayer player = getSyncManager().getPlayer();
        ItemStack heldStack = getSyncManager().getCursorItem();
        int resultStackMaxStackSize = resultStack.getMaxStackSize();

        assert heldStack.stackSize >= resultStack.stackSize;

        while (resultStack.stackSize > resultStackMaxStackSize) {
            heldStack.stackSize -= resultStackMaxStackSize;
            addItemToPlayerInventory(player, resultStack.splitStack(resultStackMaxStackSize));
        }

        heldStack.stackSize -= resultStack.stackSize;

        if (heldStack.stackSize == 0) {
            getSyncManager().setCursorItem(resultStack);
        } else {
            addItemToPlayerInventory(player, resultStack);
            // it's the same held item stack, but we need to sync changed stack size
            getSyncManager().setCursorItem(heldStack);
        }
    }

    protected static void addItemToPlayerInventory(EntityPlayer player, ItemStack stack) {
        if (stack == null)
            return;
        if (!player.inventory.addItemStackToInventory(stack) && !player.worldObj.isRemote) {
            EntityItem dropItem = player.entityDropItem(stack, 0);
            dropItem.delayBeforeCanPickup = 0;
        }
    }

    public IFluidTank getFluidTank() {
        return this.fluidTank;
    }

    public boolean canDrainSlot() {
        return this.canDrainSlot;
    }

    public boolean canFillSlot() {
        return this.canFillSlot;
    }

    public boolean controlsAmount() {
        return this.controlsAmount;
    }

    public boolean isPhantom() {
        return this.phantom;
    }

    public FluidSlotSyncHandler phantom(boolean phantom) {
        this.phantom = phantom;
        return this;
    }

    public FluidSlotSyncHandler controlsAmount(boolean controlsAmount) {
        this.controlsAmount = controlsAmount;
        if (isValid()) {
            sync(SYNC_CONTROLS_AMOUNT, buffer -> buffer.writeBoolean(controlsAmount));
        }
        return this;
    }

    public FluidSlotSyncHandler canDrainSlot(boolean canDrainSlot) {
        this.canDrainSlot = canDrainSlot;
        return this;
    }

    public FluidSlotSyncHandler canFillSlot(boolean canFillSlot) {
        this.canFillSlot = canFillSlot;
        return this;
    }
}
