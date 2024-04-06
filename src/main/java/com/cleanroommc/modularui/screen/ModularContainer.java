package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.future.IItemHandler;
import com.cleanroommc.modularui.future.ItemHandlerHelper;
import com.cleanroommc.modularui.future.PlayerMainInvWrapper;
import com.cleanroommc.modularui.future.SlotItemHandler;
import com.cleanroommc.modularui.mixins.early.minecraft.ContainerAccessor;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.utils.item.ItemStackLongDelegate;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ModularContainer extends Container {

    public static ModularContainer getCurrent(EntityPlayer player) {
        if (player.openContainer instanceof ModularContainer container) {
            return container;
        }
        return null;
    }

    private final GuiSyncManager guiSyncManager;
    private boolean init = true;
    private final List<ModularSlot> slots = new ArrayList<>();
    private final List<ModularSlot> shiftClickSlots = new ArrayList<>();

    private static final int DROP_TO_WORLD = -999;
    private static final int LEFT_MOUSE = 0;
    private static final int RIGHT_MOUSE = 1;

    private static final int CLICK_TYPE_PICKUP = 0;
    private static final int CLICK_TYPE_QUICK_MOVE = 1;
    private static final int CLICK_TYPE_QUICK_CRAFT = 5;

    public ModularContainer(GuiSyncManager guiSyncManager) {
        this.guiSyncManager = Objects.requireNonNull(guiSyncManager);
        this.guiSyncManager.construct(this);
        sortShiftClickSlots();
    }

    @SideOnly(Side.CLIENT)
    public ModularContainer() {
        this.guiSyncManager = null;
    }

    public ContainerAccessor acc() {
        return (ContainerAccessor) this;
    }

    @Override
    public void onContainerClosed(@NotNull EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        if (this.guiSyncManager != null) {
            this.guiSyncManager.onClose();
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        this.guiSyncManager.detectAndSendChanges(this.init);
        this.init = false;
    }

    private void sortShiftClickSlots() {
        this.shiftClickSlots.sort(Comparator.comparingInt(slot -> Objects.requireNonNull(slot.getSlotGroup()).getShiftClickPriority()));
    }

    @Override
    public void putStacksInSlots(ItemStack[] items) {
        if (this.inventorySlots.size() != items.length) {
            ModularUI.LOGGER.error("Here are {} slots, but expected {}", this.inventorySlots.size(), items.length);
        }
        for (int i = 0; i < Math.min(this.inventorySlots.size(), items.length); ++i) {
            this.getSlot(i).putStack(items[i]);
        }
    }

    @ApiStatus.Internal
    public void registerSlot(ModularSlot slot) {
        if (this.inventorySlots.contains(slot)) {
            throw new IllegalArgumentException("Tried to register slot which already exists!");
        }
        addSlotToContainer(slot);
        this.slots.add(slot);
        if (slot.getSlotGroupName() != null) {
            SlotGroup slotGroup = getSyncManager().getSlotGroup(slot.getSlotGroupName());
            if (slotGroup == null) {
                ModularUI.LOGGER.throwing(new IllegalArgumentException("SlotGroup '" + slot.getSlotGroupName() + "' is not registered!"));
                return;
            }
            slot.slotGroup(slotGroup);
        }
        if (slot.getSlotGroup() != null) {
            SlotGroup slotGroup = slot.getSlotGroup();
            if (slotGroup.allowShiftTransfer()) {
                this.shiftClickSlots.add(slot);
                if (!this.init) {
                    sortShiftClickSlots();
                }
            }
        }
    }

    @Contract("null, null -> fail")
    @NotNull
    @ApiStatus.Internal
    public SlotGroup validateSlotGroup(@Nullable String slotGroupName, @Nullable SlotGroup slotGroup) {
        if (slotGroup != null) {
            if (getSyncManager().getSlotGroup(slotGroup.getName()) == null) {
                throw new IllegalArgumentException("Slot group is not registered in the GUI.");
            }
            return slotGroup;
        }
        if (slotGroupName != null) {
            slotGroup = getSyncManager().getSlotGroup(slotGroupName);
            if (slotGroup == null) {
                throw new IllegalArgumentException("Can't find slot group for name " + slotGroupName);
            }
            return slotGroup;
        }
        throw new IllegalArgumentException("Either the slot group or the name must not be null!");
    }


    public GuiSyncManager getSyncManager() {
        if (this.guiSyncManager == null) {
            throw new IllegalStateException("GuiSyncManager is not available for client only GUI's.");
        }
        return this.guiSyncManager;
    }

    public boolean isClient() {
        return this.guiSyncManager == null || NetworkUtils.isClient(this.guiSyncManager.getPlayer());
    }

    public boolean isClientOnly() {
        return this.guiSyncManager == null;
    }

    @Override
    public boolean canInteractWith(@NotNull EntityPlayer playerIn) {
        return true;
    }

    @Override
    public ItemStack slotClick(int slotId, int mouseButton, int clickTypeIn, EntityPlayer player) {
        ItemStack returnable = null;
        InventoryPlayer inventoryplayer = player.inventory;

        if (clickTypeIn == CLICK_TYPE_QUICK_CRAFT || acc().getDragEvent() != 0) {
            return super.slotClick(slotId, mouseButton, clickTypeIn, player);
        }

        if ((clickTypeIn == CLICK_TYPE_PICKUP || clickTypeIn == CLICK_TYPE_QUICK_MOVE) &&
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
            } if (clickTypeIn == CLICK_TYPE_QUICK_MOVE) {
                if (slotId < 0) {
                    return null;
                }

                Slot fromSlot = getSlot(slotId);

                if (fromSlot != null && fromSlot.canTakeStack(player)) {
                    ItemStack transferredStack = this.transferStackInSlot(player, slotId);

                    if (transferredStack != null) {
                        Item item = transferredStack.getItem();
                        returnable = transferredStack.copy();

                        if (fromSlot.getStack() != null && fromSlot.getStack().getItem() == item) {
                            this.retrySlotClick(slotId, mouseButton, true, player);
                        }
                    }
                }
            } else {
                if (slotId < 0) {
                    return null;
                }

                Slot clickedSlot = getSlot(slotId);

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
                            int toRemove = mouseButton == LEFT_MOUSE ? slotStack.stackSize : (slotStack.stackSize + 1) / 2;
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

                                if (stackCount > heldStack.getMaxStackSize() - slotStack.stackSize) {
                                    stackCount = heldStack.getMaxStackSize() - slotStack.stackSize;
                                }

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
            this.detectAndSendChanges(); // Added
            return returnable; // Added
        }
        return super.slotClick(slotId, mouseButton, clickTypeIn, player);
    }


    @Override
    @Nullable
    public ItemStack transferStackInSlot(@NotNull EntityPlayer playerIn, int index) {
        ModularSlot slot = this.slots.get(index);
        if (!slot.isPhantom()) {
            ItemStack stack = slot.getStack();
            if (stack != null) {
                ItemStack remainder = transferItem(slot, stack.copy());
                if (remainder == null || remainder.stackSize < 1) stack = null;
                else stack.stackSize = remainder.stackSize;
                slot.putStack(stack);
                return null;
            }
        }
        return null;
    }

    protected ItemStack transferItem(ModularSlot fromSlot, ItemStack fromStack) {
        @Nullable SlotGroup fromSlotGroup = fromSlot.getSlotGroup();
        for (ModularSlot toSlot : this.shiftClickSlots) {
            SlotGroup slotGroup = Objects.requireNonNull(toSlot.getSlotGroup());
            // func_111238_b: isEnabled
            if (slotGroup != fromSlotGroup && toSlot.func_111238_b() && toSlot.isItemValid(fromStack)) {
                ItemStack toStack = toSlot.getStack();
                if (toSlot.isPhantom()) {
                    if (toStack == null || (ItemHandlerHelper.canItemStacksStack(fromStack, toStack) && toStack.stackSize < toSlot.getSlotStackLimit())) {
                        toSlot.putStack(fromStack.copy());
                        return fromStack;
                    }
                } else if (ItemHandlerHelper.canItemStacksStack(fromStack, toStack)) {
                    int j = toStack.stackSize + fromStack.stackSize;
                    int maxSize = Math.min(toSlot.getSlotStackLimit(), fromStack.getMaxStackSize());

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
        for (ModularSlot emptySlot : this.shiftClickSlots) {
            ItemStack itemstack = emptySlot.getStack();
            SlotGroup slotGroup = Objects.requireNonNull(emptySlot.getSlotGroup());
            // func_111238_b: isEnabled
            if (slotGroup != fromSlotGroup && emptySlot.func_111238_b() && itemstack == null && emptySlot.isItemValid(fromStack)) {
                if (fromStack.stackSize > emptySlot.getSlotStackLimit()) {
                    emptySlot.putStack(fromStack.splitStack(emptySlot.getSlotStackLimit()));
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

    private static boolean isPlayerSlot(Slot slot) {
        if (slot == null) return false;
        if (slot.inventory instanceof InventoryPlayer) {
            return slot.getSlotIndex() >= 0 && slot.getSlotIndex() < 36;
        }
        if (slot instanceof SlotItemHandler slotItemHandler) {
            IItemHandler iItemHandler = slotItemHandler.getItemHandler();
            if (iItemHandler instanceof PlayerMainInvWrapper) {
                return slot.getSlotIndex() >= 0 && slot.getSlotIndex() < 36;
            }
        }
        return false;
    }
}