package com.cleanroommc.modularui.value.sync;

import static com.google.common.primitives.Ints.saturatedCast;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.IFluidTankLong;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.utils.fluid.FluidInteractions;
import com.cleanroommc.modularui.utils.fluid.FluidTanksHandler;
import com.cleanroommc.modularui.utils.fluid.IFluidTanksHandler;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

public class FluidSlotLongSyncHandler extends ValueSyncHandler<IFluidTankLong> {

    @NotNull
    private final IFluidTanksHandler handler;
    private final int index;
    @NotNull
    private IFluidTankLong cache;
    private boolean canFillSlot = true, canDrainSlot = true, controlsAmount = true, phantom = false;
    @Nullable
    private Fluid lastStoredPhantomFluid;

    public FluidSlotLongSyncHandler(IFluidTankLong tank) {
        this(new FluidTanksHandler(tank), 0);
    }

    public FluidSlotLongSyncHandler(IFluidTanksHandler handler, int index) {
        this.handler = handler;
        this.index = index;
        this.cache = handler.getTank(index).copy();
    }

    @Override
    public void setValue(IFluidTankLong value, boolean setSource, boolean sync) {
        cache = value.copy();
        if (setSource && !NetworkUtils.isClient()) {
            handler.drain(index, Long.MAX_VALUE, true);
            if (value.getFluidAmountLong() == 0) {
                handler.fill(index, value.getRealFluid(), value.getFluidAmountLong(), true);
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
        IFluidTankLong current = handler.getTank(index);
        if (current == this.cache)
            return false;
        if (this.cache == null)
            return true;
        if (current.getRealFluid() == null || cache.getRealFluid() == null)
            return true;
        if (current.getFluidAmountLong() != cache.getFluidAmountLong())
            return true;
        return current.getRealFluid() != cache.getRealFluid();
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (!isFirstSync && !needsSync()) {
            return false;
        }

        setValue(handler.getTank(index), false, false);
        return true;
    }

    @Override
    public void write(PacketBuffer buffer) {
        NetworkUtils.writeFluidTank(buffer, getValue());
    }

    @Override
    public void read(PacketBuffer buffer) {
        setValue(NetworkUtils.readFluidTank(buffer), true, false);
    }

    @Override
    public IFluidTankLong getValue() {
        return cache;
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
        boolean processFullStack = mouseData.mouseButton == 0;
        ItemStack heldItem = getSyncManager().getCursorItem();
        if (heldItem == null || heldItem.stackSize == 0)
            return;

        ItemStack heldItemSizedOne = heldItem.copy();
        heldItemSizedOne.stackSize = 1;
        FluidStack currentFluid = handler.getTank(index).getFluid();
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

        if (heldFluid != null && handler.getTankAmount(index) < handler.getTankCapacity(index)) {
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
        FluidStack currentFluid = handler.getTank(index).getFluid();
        ItemStack heldItem = getSyncManager().getCursorItem();

        if (mouseData.mouseButton == 0) {
            if (heldItem == null) {
                if (canDrainSlot) {
                    handler.drain(index, mouseData.shift ? Long.MAX_VALUE : 1000, true);
                }
            } else {
                ItemStack heldItemSizedOne = heldItem.copy();
                heldItemSizedOne.stackSize = 1;
                FluidStack heldFluid = FluidInteractions.getFluidForPhantomItem(heldItemSizedOne);
                if ((controlsAmount || currentFluid == null) && heldFluid != null) {
                    if (canFillSlot) {
                        if (!controlsAmount) {
                            heldFluid.amount = 1;
                        }
                        if (handler.fill(index, heldFluid.getFluid(), heldFluid.amount, false) > 0) {
                            lastStoredPhantomFluid = heldFluid.getFluid();
                        }
                    }
                } else {
                    if (canDrainSlot) {
                        handler.drain(index, mouseData.shift ? Long.MAX_VALUE : 1000, true);
                    }
                }
            }
        } else if (mouseData.mouseButton == 1) {
            if (canFillSlot) {
                if (currentFluid != null) {
                    if (controlsAmount) {
                        FluidStack toFill = currentFluid.copy();
                        toFill.amount = 1000;
                        handler.fill(index, toFill.getFluid(), toFill.amount, true);
                    }
                } else if (lastStoredPhantomFluid != null) {
                    handler.fill(index, lastStoredPhantomFluid, controlsAmount ? 1000 : 1, true);
                }
            }
        } else if (mouseData.mouseButton == 2 && currentFluid != null && canDrainSlot) {
            handler.drain(index, mouseData.shift ? Long.MAX_VALUE : 1000, true);
        }
    }

    protected void drainFluid(boolean processFullStack) {
        ItemStack heldItem = getSyncManager().getCursorItem();
        if (heldItem == null || heldItem.stackSize == 0) {
            return;
        }

        ItemStack heldItemSizedOne = heldItem.copy();
        heldItemSizedOne.stackSize = 1;
        FluidStack currentFluid = handler.getTank(index).getFluid();
        if (currentFluid == null) {
            return;
        }
        currentFluid = currentFluid.copy();

        long originalFluidAmount = handler.getTankAmount(index);
        ItemStack filledContainer = FluidInteractions.fillFluidContainer(currentFluid, heldItemSizedOne);
        if (filledContainer != null) {
            long filledAmount = originalFluidAmount - currentFluid.amount;
            if (filledAmount < 1) {
                return;
            }
            handler.drain(index, filledAmount, true);
            if (processFullStack) {
                long additionalParallel = Math.min(heldItem.stackSize - 1, currentFluid.amount / filledAmount);
                handler.drain(index, filledAmount * additionalParallel, true);
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
        FluidStack currentFluid = handler.getTank(index).getFluid();
        if (currentFluid != null && !currentFluid.isFluidEqual(heldFluid)) {
            return;
        }

        long freeSpace = handler.getTankCapacity(index) - handler.getTankAmount(index);
        if (freeSpace <= 0) {
            return;
        }

        ItemStack itemStackEmptied = null;
        long fluidAmountTaken = 0;
        if (freeSpace >= heldFluid.amount) {
            itemStackEmptied = FluidInteractions.getContainerForFilledItem(heldItemSizedOne);
            fluidAmountTaken = heldFluid.amount;
        }

        if (itemStackEmptied == null) {
            return;
        }

        long parallel = processFullStack ? Math.min(freeSpace / fluidAmountTaken, heldItem.stackSize) : 1;
        FluidStack copiedFluidStack = heldFluid.copy();
        handler.fill(index, copiedFluidStack.getFluid(), fluidAmountTaken * parallel, true);

        itemStackEmptied.stackSize = saturatedCast(parallel);
        replaceCursorItemStack(itemStackEmptied);
        playSound(heldFluid, true);
    }

    private void playSound(FluidStack fluid, boolean fill) {
    }

    protected void replaceCursorItemStack(ItemStack resultStack) {
        EntityPlayer player = getSyncManager().getPlayer();
        int resultStackMaxStackSize = resultStack.getMaxStackSize();
        while (resultStack.stackSize > resultStackMaxStackSize) {
            player.inventory.getItemStack().stackSize -= resultStackMaxStackSize;
            addItemToPlayerInventory(player, resultStack.splitStack(resultStackMaxStackSize));
        }
        if (player.inventory.getItemStack().stackSize == resultStack.stackSize) {
            player.inventory.setItemStack(resultStack);
        } else {
            ItemStack tStackHeld = player.inventory.getItemStack();
            tStackHeld.stackSize -= resultStack.stackSize;
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

    public void tryScrollPhantom(MouseData mouseData) {
        long amount = mouseData.mouseButton;
        if (mouseData.shift) {
            amount *= 10;
        }
        if (mouseData.ctrl) {
            amount *= 100;
        }
        if (mouseData.alt) {
            amount *= 1000;
        }
        if (handler.getFluidInTank(index) == null) {
            if (amount > 0 && this.lastStoredPhantomFluid != null) {
                handler.fill(index, lastStoredPhantomFluid, amount, true);
            }
            return;
        }
        if (amount > 0 && this.controlsAmount) {
            handler.fill(index, handler.getFluidInTank(index), amount, true);
        } else if (amount < 0) {
            handler.drain(index, amount, true);
        }
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

    public FluidSlotLongSyncHandler phantom(boolean phantom) {
        this.phantom = phantom;
        return this;
    }

    public FluidSlotLongSyncHandler controlsAmount(boolean controlsAmount) {
        this.controlsAmount = controlsAmount;
        if (isValid()) {
            sync(3, buffer -> buffer.writeBoolean(controlsAmount));
        }
        return this;
    }

    public FluidSlotLongSyncHandler canDrainSlot(boolean canDrainSlot) {
        this.canDrainSlot = canDrainSlot;
        return this;
    }

    public FluidSlotLongSyncHandler canFillSlot(boolean canFillSlot) {
        this.canFillSlot = canFillSlot;
        return this;
    }
}
