package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.api.inventory.ClickType;
import com.cleanroommc.modularui.future.ItemHandlerHelper;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Sometimes you need special behaviour. This class allows you to override common methods from {@link net.minecraft.inventory.Container Container}.
 * <br>
 * <b>NOTE: Only use this if you know what you are doing. You can do a lot wrong here and cause stupid bugs.</b>
 */
public class ContainerCustomizer {

    private static final int DROP_TO_WORLD = -999;
    private static final int LEFT_MOUSE = 0;
    private static final int RIGHT_MOUSE = 1;

    private ModularContainer container;

    void initialize(ModularContainer container) {
        this.container = container;
    }

    public ModularContainer getContainer() {
        if (container == null) {
            throw new NullPointerException("ContainerCustomizer is not registered!");
        }
        return container;
    }

    public void onContainerClosed() {}

    public @Nullable ItemStack slotClick(int slotId, int mouseButton, @NotNull ClickType clickTypeIn, EntityPlayer player) {
        ItemStack returnable = null;
        InventoryPlayer inventoryplayer = player.inventory;

        if (clickTypeIn == ClickType.QUICK_CRAFT || container.acc().getDragEvent() != 0) {
            return container.superSlotClick(slotId, mouseButton, clickTypeIn, player);
        }

        if ((clickTypeIn == ClickType.PICKUP || clickTypeIn == ClickType.QUICK_MOVE) &&
                (mouseButton == LEFT_MOUSE || mouseButton == RIGHT_MOUSE)) {
            if (slotId == DROP_TO_WORLD) {
                if (inventoryplayer.getItemStack() != null) {
                    if (mouseButton == LEFT_MOUSE) {
                        player.dropPlayerItemWithRandomChoice(inventoryplayer.getItemStack(), true);
                        inventoryplayer.setItemStack(null);
                    }

                    if (mouseButton == RIGHT_MOUSE) {
                        player.dropPlayerItemWithRandomChoice(inventoryplayer.getItemStack().splitStack(1), true);

                        if (inventoryplayer.getItemStack().stackSize == 0) {
                            inventoryplayer.setItemStack(null);
                        }
                    }
                }
                return inventoryplayer.getItemStack(); // Added
            } if (clickTypeIn == ClickType.QUICK_MOVE) {
                if (slotId < 0) {
                    return null;
                }

                Slot fromSlot = container.getSlot(slotId);

                if (fromSlot != null && fromSlot.canTakeStack(player)) {
                    ItemStack transferredStack = this.transferStackInSlot(player, slotId);

                    if (transferredStack != null) {
                        Item item = transferredStack.getItem();
                        returnable = transferredStack.copy();

                        if (fromSlot.getStack() != null && fromSlot.getStack().getItem() == item) {
                            this.slotClick(slotId, mouseButton, ClickType.QUICK_MOVE, player); // retrySlotClick
                        }
                    }
                }
            } else {
                if (slotId < 0) {
                    return null;
                }

                Slot clickedSlot = container.getSlot(slotId);

                if (clickedSlot != null) {
                    ItemStack slotStack = clickedSlot.getStack();
                    ItemStack heldStack = inventoryplayer.getItemStack();

                    // if (slotStack != null) {
                    //     returnable = slotStack.copy();
                    // } // Removed

                    if (slotStack == null) {
                        if (heldStack != null && clickedSlot.isItemValid(heldStack)) {
                            int stackCount = mouseButton == LEFT_MOUSE ? heldStack.stackSize : 1;

                            if (stackCount > clickedSlot.getSlotStackLimit()) {
                                stackCount = clickedSlot.getSlotStackLimit();
                            }

                            clickedSlot.putStack(heldStack.splitStack(stackCount));

                            if (heldStack.stackSize == 0) {
                                inventoryplayer.setItemStack(null);
                            }
                        }
                    } else if (clickedSlot.canTakeStack(player)) {
                        if (heldStack == null) {
                            // int toRemove = mouseButton == LEFT_MOUSE ? slotStack.stackSize : (slotStack.stackSize + 1) / 2; // Removed
                            int s = Math.min(slotStack.stackSize, slotStack.getMaxStackSize());
                            int toRemove = mouseButton == LEFT_MOUSE ? s : (s + 1) / 2; // Added
                            // inventoryplayer.setItemStack(clickedSlot.decrStackSize(toRemove)); // Removed
                            inventoryplayer.setItemStack(slotStack.splitStack(toRemove)); // Added

                            if (slotStack.stackSize == 0) {
                                // clickedSlot.putStack(null); // Removed
                                slotStack = null; // Added
                            }
                            clickedSlot.putStack(slotStack); // Added

                            clickedSlot.onPickupFromSlot(player, inventoryplayer.getItemStack());
                        } else if (clickedSlot.isItemValid(heldStack)) {
                            if (slotStack.getItem() == heldStack.getItem() &&
                                    slotStack.getItemDamage() == heldStack.getItemDamage() &&
                                    ItemStack.areItemStackTagsEqual(slotStack, heldStack)) {
                                int stackCount = mouseButton == 0 ? heldStack.stackSize : 1;

                                if (stackCount > clickedSlot.getSlotStackLimit() - slotStack.stackSize) {
                                    stackCount = clickedSlot.getSlotStackLimit() - slotStack.stackSize;
                                }

                                // if (stackCount > heldStack.getMaxStackSize() - slotStack.stackSize) {
                                //     stackCount = heldStack.getMaxStackSize() - slotStack.stackSize;
                                // } // Removed

                                heldStack.splitStack(stackCount);

                                if (heldStack.stackSize == 0) {
                                    inventoryplayer.setItemStack(null);
                                }

                                slotStack.stackSize += stackCount;
                                clickedSlot.putStack(slotStack); // Added
                            } else if (heldStack.stackSize <= clickedSlot.getSlotStackLimit()) {
                                clickedSlot.putStack(heldStack);
                                inventoryplayer.setItemStack(slotStack);
                            }
                        } else if (slotStack.getItem() == heldStack.getItem() &&
                                heldStack.getMaxStackSize() > 1 &&
                                (!slotStack.getHasSubtypes() || slotStack.getItemDamage() == heldStack.getItemDamage()) &&
                                ItemStack.areItemStackTagsEqual(slotStack, heldStack)) {
                            int stackCount = slotStack.stackSize;

                            if (stackCount > 0 && stackCount + heldStack.stackSize <= heldStack.getMaxStackSize()) {
                                heldStack.stackSize += stackCount;
                                slotStack = clickedSlot.decrStackSize(stackCount);

                                if (slotStack.stackSize == 0) {
                                    clickedSlot.putStack(null);
                                }

                                clickedSlot.onPickupFromSlot(player, inventoryplayer.getItemStack());
                            }
                        }
                    }

                    clickedSlot.onSlotChanged();
                }
            }
            container.detectAndSendChanges(); // Added
            return returnable; // Added
        }
        return container.superSlotClick(slotId, mouseButton, clickTypeIn, player);
    }

    public @Nullable ItemStack transferStackInSlot(@NotNull EntityPlayer playerIn, int index) {
        ModularSlot slot = this.container.getModularSlot(index);
        if (!slot.isPhantom()) {
            ItemStack stack = slot.getStack();
            if (stack != null) {
                stack = stack.copy();
                int base = 0;
                if (stack.stackSize > stack.getMaxStackSize()) {
                    base = stack.stackSize - stack.getMaxStackSize();
                    stack.stackSize = stack.getMaxStackSize();
                }
                ItemStack remainder = transferItem(slot, stack.copy());
                if (base == 0 && remainder == null || remainder.stackSize < 1) stack = null;
                else stack.stackSize = base + remainder.stackSize;
                slot.putStack(stack);
                return null;
            }
        }
        return null;
    }

    protected ItemStack transferItem(ModularSlot fromSlot, ItemStack fromStack) {
        @Nullable SlotGroup fromSlotGroup = fromSlot.getSlotGroup();
        for (ModularSlot toSlot : this.container.getShiftClickSlots()) {
            SlotGroup slotGroup = Objects.requireNonNull(toSlot.getSlotGroup());
            // func_111238_b: isEnabled
            if (slotGroup != fromSlotGroup && toSlot.func_111238_b() && toSlot.isItemValid(fromStack)) {
                ItemStack toStack = toSlot.getStack();
                if (toSlot.isPhantom()) {
                    if (toStack == null || (ItemHandlerHelper.canItemStacksStack(fromStack, toStack) && toStack.stackSize < toSlot.getItemStackLimit(toStack))) {
                        toSlot.putStack(fromStack.copy());
                        return fromStack;
                    }
                } else if (ItemHandlerHelper.canItemStacksStack(fromStack, toStack)) {
                    int j = toStack.stackSize + fromStack.stackSize;
                    int maxSize = toSlot.getItemStackLimit(fromStack);

                    if (j <= maxSize) {
                        fromStack.stackSize = 0;
                        toStack.stackSize = j;
                        toSlot.onSlotChanged();
                    } else if (toStack.stackSize < maxSize) {
                        fromStack.stackSize -= maxSize - toStack.stackSize;
                        toStack.stackSize = maxSize;
                        toSlot.onSlotChanged();
                    }

                    if (fromStack.stackSize < 1) {
                        return fromStack;
                    }
                }
            }
        }
        for (ModularSlot emptySlot : this.container.getShiftClickSlots()) {
            ItemStack itemstack = emptySlot.getStack();
            SlotGroup slotGroup = Objects.requireNonNull(emptySlot.getSlotGroup());
            // func_111238_b: isEnabled
            if (slotGroup != fromSlotGroup && emptySlot.func_111238_b() && itemstack == null && emptySlot.isItemValid(fromStack)) {
                if (fromStack.stackSize > emptySlot.getItemStackLimit(fromStack)) {
                    emptySlot.putStack(fromStack.splitStack(emptySlot.getItemStackLimit(fromStack)));
                } else {
                    emptySlot.putStack(fromStack.splitStack(fromStack.stackSize));
                }
                if (fromStack.stackSize < 1) {
                    break;
                }
            }
        }
        return fromStack;
    }

    public boolean canMergeSlot(ItemStack stack, Slot slotIn) {
        return this.container.superCanMergeSlot(stack, slotIn);
    }

    protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        return this.container.superMergeItemStack(stack, startIndex, endIndex, reverseDirection);
    }
}
