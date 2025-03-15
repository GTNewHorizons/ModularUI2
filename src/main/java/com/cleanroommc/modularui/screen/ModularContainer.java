package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.inventory.ClickType;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.mixins.early.minecraft.ContainerAccessor;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.utils.item.ItemHandlerHelper;
import com.cleanroommc.modularui.value.sync.ModularSyncManager;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
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

    private static final int DROP_TO_WORLD = -999;
    private static final int LEFT_MOUSE = 0;
    private static final int RIGHT_MOUSE = 1;

    private EntityPlayer player;
    private ModularSyncManager syncManager;
    private boolean init = true;
    // all phantom slots (inventory don't contain phantom slots)
    private final List<ModularSlot> phantomSlots = new ArrayList<>();
    private final List<ModularSlot> shiftClickSlots = new ArrayList<>();
    private GuiData guiData;
    private UISettings settings;

    @SideOnly(Side.CLIENT)
    private ModularScreen optionalScreen;

    public ModularContainer() {}

    @ApiStatus.Internal
    public void construct(EntityPlayer player, PanelSyncManager panelSyncManager, UISettings settings, String mainPanelName, GuiData guiData) {
        this.player = player;
        this.syncManager = new ModularSyncManager(this);
        this.syncManager.construct(mainPanelName, panelSyncManager);
        this.settings = settings;
        this.guiData = guiData;
        sortShiftClickSlots();
    }

    @SideOnly(Side.CLIENT)
    void initializeClient(ModularScreen screen) {
        this.optionalScreen = screen;
    }

    @ApiStatus.Internal
    @SideOnly(Side.CLIENT)
    public void constructClientOnly() {
        this.player = Minecraft.getMinecraft().thePlayer;
        this.syncManager = null;
    }

    public boolean isInitialized() {
        return this.player != null;
    }

    @SideOnly(Side.CLIENT)
    public ModularScreen getScreen() {
        if (this.optionalScreen == null) throw new NullPointerException("ModularScreen is not yet initialised!");
        return optionalScreen;
    }

    public ContainerAccessor acc() {
        return (ContainerAccessor) this;
    }

    @Override
    public void addCraftingToCrafters(ICrafting player) {
        super.addCraftingToCrafters(player);
        if (this.syncManager != null) {
            this.syncManager.onOpen();
        }
    }

    @Override
    public void onContainerClosed(@NotNull EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        if (this.syncManager != null) {
            this.syncManager.onClose();
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (this.syncManager != null) {
            this.syncManager.detectAndSendChanges(this.init);
        }
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
    public void registerSlot(String panelName, ModularSlot slot) {
        if (this.inventorySlots.contains(slot)) {
            throw new IllegalArgumentException("Tried to register slot which already exists!");
        }
        if (slot.isPhantom()) {
            this.phantomSlots.add(slot);
        } else {
            addSlotToContainer(slot);
        }
        if (slot.getSlotGroupName() != null) {
            SlotGroup slotGroup = getSyncManager().getSlotGroup(panelName, slot.getSlotGroupName());
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

    @Contract("_, null, null -> fail")
    @NotNull
    @ApiStatus.Internal
    public SlotGroup validateSlotGroup(String panelName, @Nullable String slotGroupName, @Nullable SlotGroup slotGroup) {
        if (slotGroup != null) {
            if (getSyncManager().getSlotGroup(panelName, slotGroup.getName()) == null) {
                throw new IllegalArgumentException("Slot group is not registered in the GUI.");
            }
            return slotGroup;
        }
        if (slotGroupName != null) {
            slotGroup = getSyncManager().getSlotGroup(panelName, slotGroupName);
            if (slotGroup == null) {
                throw new IllegalArgumentException("Can't find slot group for name " + slotGroupName);
            }
            return slotGroup;
        }
        throw new IllegalArgumentException("Either the slot group or the name must not be null!");
    }

    public ModularSyncManager getSyncManager() {
        if (this.syncManager == null) {
            throw new IllegalStateException("GuiSyncManager is not available for client only GUI's.");
        }
        return this.syncManager;
    }

    public boolean isClient() {
        return this.syncManager == null || NetworkUtils.isClient(this.player);
    }

    public boolean isClientOnly() {
        return this.syncManager == null;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public GuiData getGuiData() {
        return guiData;
    }

    public ModularSlot getModularSlot(int index) {
        Slot slot = this.inventorySlots.get(index);
        if (slot instanceof ModularSlot modularSlot) {
            return modularSlot;
        }
        throw new IllegalStateException("A non-ModularSlot was found, but all slots in a ModularContainer must extend ModularSlot.");
    }

    public List<ModularSlot> getShiftClickSlots() {
        return Collections.unmodifiableList(this.shiftClickSlots);
    }

    public void onSlotChanged(ModularSlot slot, ItemStack stack, boolean onlyAmountChanged) {}

    @Override
    public boolean canInteractWith(@NotNull EntityPlayer playerIn) {
        return this.settings.canPlayerInteractWithUI(playerIn);
    }

    @Override
    public ItemStack slotClick(int slotId, int mouseButton, int mode, EntityPlayer player) {
        ClickType clickTypeIn = ClickType.fromNumber(mode);
        ItemStack returnable = null;
        InventoryPlayer inventoryplayer = player.inventory;

        if (clickTypeIn == ClickType.QUICK_CRAFT || acc().getDragEvent() != 0) {
            return super.slotClick(slotId, mouseButton, mode, player);
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
            }
            if (clickTypeIn == ClickType.QUICK_MOVE) {
                if (slotId < 0) {
                    return null;
                }

                Slot fromSlot = getSlot(slotId);
                if (!fromSlot.canTakeStack(player)) {
                    return Platform.EMPTY_STACK;
                }
                // looping so that crafting works properly
                ItemStack remainder;
                do {
                    remainder = transferStackInSlot(player, slotId);
                    returnable = Platform.copyStack(remainder);
                } while (!Platform.isStackEmpty(remainder) && ItemHandlerHelper.canItemStacksStack(fromSlot.getStack(), remainder));
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
            detectAndSendChanges(); // Added
            return returnable; // Added
        } else if (clickTypeIn == ClickType.PICKUP_ALL && slotId >= 0) {
            Slot slot = inventorySlots.get(slotId);
            ItemStack itemstack1 = inventoryplayer.getItemStack();

            if (itemstack1 != null && (slot == null || !slot.getHasStack() || !slot.canTakeStack(player))) {
                int i = mouseButton == 0 ? 0 : inventorySlots.size() - 1;
                int j = mouseButton == 0 ? 1 : -1;

                for (int k = 0; k < 2; ++k) {
                    for (int l = i; l >= 0 && l < inventorySlots.size() && itemstack1.stackSize < itemstack1.getMaxStackSize(); l += j) {
                        Slot slot1 = inventorySlots.get(l);
                        if (slot1 instanceof ModularSlot modularSlot && modularSlot.isPhantom()) continue; // Added

                        // func_94527_a: canAddItemToSlot
                        if (slot1.getHasStack() && Container.func_94527_a(slot1, itemstack1, true) && slot1.canTakeStack(player) && func_94530_a(itemstack1, slot1)) { // Replaced: canMergeSlot
                            ItemStack itemstack2 = slot1.getStack();

                            if (k != 0 || itemstack2.stackSize != itemstack2.getMaxStackSize()) { // Moved condition from previous if
                                int i1 = Math.min(itemstack1.getMaxStackSize() - itemstack1.stackSize, itemstack2.stackSize);
                                ItemStack itemstack3 = slot1.decrStackSize(i1);
                                itemstack1.stackSize += i1;

                                if (itemstack3 == null || itemstack3.stackSize <= 0) { // Added null check
                                    slot1.putStack(null);
                                }

                                slot1.onPickupFromSlot(player, itemstack3);
                            }
                        }
                    }
                }
            }

            detectAndSendChanges();
            return returnable; // Added
        } else if (clickTypeIn == ClickType.SWAP && mouseButton >= 0 && mouseButton < 9) {
            ModularSlot phantom = getModularSlot(slotId);
            ItemStack hotbarStack = inventoryplayer.getStackInSlot(mouseButton);
            if (phantom.isPhantom()) {
                // insert stack from hotbar slot into phantom slot
                phantom.putStack(hotbarStack == null ? null : hotbarStack.copy());
                detectAndSendChanges();
                return returnable;
            }
        }
        return super.slotClick(slotId, mouseButton, mode, player);
    }

    @Override
    public @Nullable ItemStack transferStackInSlot(@NotNull EntityPlayer playerIn, int index) {
        ModularSlot slot = getModularSlot(index);
        if (!slot.isPhantom()) {
            ItemStack stack = slot.getStack();
            if (stack != null) {
                ItemStack copy = stack.copy();
                stack = stack.copy();
                int base = 0;
                if (stack.stackSize > stack.getMaxStackSize()) {
                    base = stack.stackSize - stack.getMaxStackSize();
                    stack.stackSize = stack.getMaxStackSize();
                }
                ItemStack remainder = transferItem(slot, stack.copy());
                if (base == 0 && (remainder == null || remainder.stackSize < 1)) stack = null;
                else stack.stackSize = base + remainder.stackSize;
                slot.putStack(stack);
                slot.onSlotChange(remainder, copy);
                slot.onPickupFromSlot(playerIn, remainder);
                slot.onCraftShiftClick(playerIn, remainder);
                return copy; // return a non-empty stack if insertion was successful, this causes this function to be called again, important for crafting
            }
        }
        return null;
    }

    protected ItemStack transferItem(ModularSlot fromSlot, ItemStack fromStack) {
        @Nullable SlotGroup fromSlotGroup = fromSlot.getSlotGroup();
        for (ModularSlot toSlot : getShiftClickSlots()) {
            SlotGroup slotGroup = Objects.requireNonNull(toSlot.getSlotGroup());
            // func_111238_b: isEnabled
            if (slotGroup != fromSlotGroup && toSlot.func_111238_b() && toSlot.isItemValid(fromStack)) {
                ItemStack toStack = toSlot.getStack();
                if (!fromSlot.isPhantom() && ItemHandlerHelper.canItemStacksStack(fromStack, toStack)) {
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
        boolean hasNonEmptyPhantom = false;
        // now insert into first empty slot (phantom or not) and check if we have any non-empty phantom slots
        for (ModularSlot toSlot : getShiftClickSlots()) {
            ItemStack itemstack = toSlot.getStack();
            SlotGroup slotGroup = Objects.requireNonNull(toSlot.getSlotGroup());
            // func_111238_b: isEnabled
            if (slotGroup != fromSlotGroup && toSlot.func_111238_b() && toSlot.isItemValid(fromStack)) {
                if (toSlot.isPhantom()) {
                    if (!Platform.isStackEmpty(itemstack)) {
                        // skip non-empty phantom for now
                        hasNonEmptyPhantom = true;
                    } else {
                        toSlot.putStack(fromStack.copy());
                        return fromStack;
                    }
                } else if (Platform.isStackEmpty(itemstack)) {
                    if (fromStack.stackSize > toSlot.getItemStackLimit(fromStack)) {
                        toSlot.putStack(fromStack.splitStack(toSlot.getItemStackLimit(fromStack)));
                    } else {
                        toSlot.putStack(fromStack.splitStack(fromStack.stackSize));
                    }
                    if (fromStack.stackSize < 1) {
                        break;
                    }
                }
            }
        }
        if (!hasNonEmptyPhantom) return fromStack;

        // now insert into the first phantom slot we can find (will be non-empty)
        // unfortunately, when all phantom slots are used it will always overwrite the first one
        for (ModularSlot toSlot : getShiftClickSlots()) {
            SlotGroup slotGroup = Objects.requireNonNull(toSlot.getSlotGroup());
            // func_111238_b: isEnabled
            if (slotGroup != fromSlotGroup && toSlot.isPhantom() && toSlot.func_111238_b() && toSlot.isItemValid(fromStack)) {
                // don't check for stackable, just overwrite
                toSlot.putStack(fromStack.copy());
                return fromStack;
            }
        }
        return fromStack;
    }
}
