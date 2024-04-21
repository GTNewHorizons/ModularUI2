package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.utils.FluidTankHandler;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.utils.fluid.FluidInteractions;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
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

    @Nullable
    private FluidStack cache;
    private final IFluidTank fluidTank;
    private boolean canFillSlot = true, canDrainSlot = true, controlsAmount = true, phantom = false;
    @Nullable
    private FluidStack lastStoredPhantomFluid;

    public FluidSlotSyncHandler(IFluidTank fluidTank) {
        this.fluidTank = fluidTank;
    }

    @Nullable
    @Override
    public FluidStack getValue() {
        return this.cache;
    }

    @Override
    public void setValue(@Nullable FluidStack value, boolean setSource, boolean sync) {
        this.cache = copyFluid(value);
        if (setSource && !NetworkUtils.isClient()) {
            this.fluidTank.drain(Integer.MAX_VALUE, true);
            if (!isFluidEmpty(value)) {
                this.fluidTank.fill(value.copy(), true);
            }
        }
        if (sync) {
            if (NetworkUtils.isClient()) {
                syncToServer(0, this::write);
            } else {
                syncToClient(0, this::write);
            }
        }
        onValueChanged();
    }

    public boolean needsSync() {
        FluidStack current = this.fluidTank.getFluid();
        if (current == this.cache)
            return false;
        if (current == null || this.cache == null)
            return true;
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
    public void write(PacketBuffer buffer) {
        NetworkUtils.writeFluidStack(buffer, this.cache);
    }

    @Override
    public void read(PacketBuffer buffer) {
        setValue(NetworkUtils.readFluidStack(buffer), true, false);
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        if (id == 0) {
            read(buf);
        } else if (id == 3) {
            this.controlsAmount = buf.readBoolean();
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        if (id == 0) {
            if (this.phantom) {
                read(buf);
            }
        } else if (id == 1) {
            if (this.phantom) {
                tryClickPhantom(MouseData.readPacket(buf));
            } else {
                tryClickContainer(MouseData.readPacket(buf));
            }
        } else if (id == 2) {
            if (this.phantom) {
                tryScrollPhantom(MouseData.readPacket(buf));
            }
        } else if (id == 3) {
            this.controlsAmount = buf.readBoolean();
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
        FluidStack heldFluid = FluidInteractions.getFluidForRealItem(heldItemSizedOne);
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
            drainFluid(processFullStack);
            return;
        } else {
            if (!canDrainSlot) {
                return;
            }
            drainFluid(processFullStack);
            return;
        }
    }

    private void tryClickPhantom(MouseData mouseData) {
        FluidStack currentFluid = fluidTank.getFluid();
        ItemStack cursorStack = getSyncManager().getCursorItem();

        if (mouseData.mouseButton == 0) {
            if (cursorStack == null) {
                if (canDrainSlot) {
                    fluidTank.drain(mouseData.shift ? Integer.MAX_VALUE : 1000, true);
                }
            } else {
                ItemStack heldItemSizedOne = cursorStack.copy();
                heldItemSizedOne.stackSize = 1;
                FluidStack heldFluid = FluidInteractions.getFluidForPhantomItem(heldItemSizedOne);
                if ((controlsAmount || currentFluid == null) && heldFluid != null) {
                    if (canFillSlot) {
                        if (!controlsAmount) {
                            heldFluid.amount = 1;
                        }
                        if (fluidTank.fill(heldFluid, false) > 0) {
                            lastStoredPhantomFluid = heldFluid.copy();
                        }
                    }
                } else {
                    if (canDrainSlot) {
                        fluidTank.drain(mouseData.shift ? Integer.MAX_VALUE : 1000, true);
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

    protected void drainFluid(boolean processFullStack) {
        ItemStack heldItem = getSyncManager().getCursorItem();
        if (heldItem == null || heldItem.stackSize == 0) {
            return;
        }

        ItemStack heldItemSizedOne = heldItem.copy();
        heldItemSizedOne.stackSize = 1;
        FluidStack currentFluid = fluidTank.getFluid();
        if (currentFluid == null) {
            return;
        }
        currentFluid = currentFluid.copy();

        int originalFluidAmount = fluidTank.getFluidAmount();
        ItemStack filledContainer = FluidInteractions.fillFluidContainer(currentFluid, heldItemSizedOne);
        if (filledContainer != null) {
            int filledAmount = originalFluidAmount - currentFluid.amount;
            if (filledAmount < 1) {
                return;
            }
            fluidTank.drain(filledAmount, true);
            if (processFullStack) {
                int additionalParallel = Math.min(heldItem.stackSize - 1, currentFluid.amount / filledAmount);
                fluidTank.drain(filledAmount * additionalParallel, true);
                filledContainer.stackSize += additionalParallel;
            }
            replaceCursorItemStack(filledContainer);
            playSound(currentFluid, false);
        }
    }

    protected void fillFluid(@NotNull FluidStack heldFluid, boolean processFullStack) {
        ItemStack heldItem = getSyncManager().getCursorItem();
        if (heldItem == null || heldItem.stackSize == 0) {
            return;
        }

        ItemStack heldItemSizedOne = heldItem.copy();
        heldItemSizedOne.stackSize = 1;
        FluidStack currentFluid = fluidTank.getFluid();
        if (currentFluid != null && !currentFluid.isFluidEqual(heldFluid)) {
            return;
        }

        int freeSpace = fluidTank.getCapacity() - fluidTank.getFluidAmount();
        if (freeSpace <= 0) {
            return;
        }

        ItemStack itemStackEmptied = null;
        int fluidAmountTaken = 0;
        if (freeSpace >= heldFluid.amount) {
            itemStackEmptied = FluidInteractions.getContainerForFilledItem(heldItemSizedOne);
            fluidAmountTaken = heldFluid.amount;
        }

        if (itemStackEmptied == null) {
            return;
        }

        int parallel = processFullStack ? Math.min(freeSpace / fluidAmountTaken, heldItem.stackSize) : 1;
        FluidStack copiedFluidStack = heldFluid.copy();
        copiedFluidStack.amount = fluidAmountTaken * parallel;
        fluidTank.fill(copiedFluidStack, true);

        itemStackEmptied.stackSize = parallel;
        replaceCursorItemStack(itemStackEmptied);
        playSound(heldFluid, true);
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

    protected void replaceCursorItemStack(ItemStack resultStack) {
        EntityPlayer player = getSyncManager().getPlayer();
        int resultStackMaxStackSize = resultStack.getMaxStackSize();
        while (resultStack.stackSize > resultStackMaxStackSize) {
            player.inventory.getItemStack().stackSize -= resultStackMaxStackSize;
            addItemToPlayerInventory(player, resultStack.splitStack(resultStackMaxStackSize));
        }
        if (getSyncManager().getCursorItem().stackSize == resultStack.stackSize) {
            getSyncManager().setCursorItem(resultStack);
        } else {
            ItemStack heldItem = getSyncManager().getCursorItem();
            heldItem.stackSize -= resultStack.stackSize;
            addItemToPlayerInventory(player, resultStack);
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
            sync(3, buffer -> buffer.writeBoolean(controlsAmount));
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
