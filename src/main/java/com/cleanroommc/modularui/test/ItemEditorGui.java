package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.factory.SimpleGuiFactory;
import com.cleanroommc.modularui.future.ItemStackHandler;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;

public class ItemEditorGui implements IGuiHolder<GuiData> {

    private static final SimpleGuiFactory GUI = new SimpleGuiFactory("mui:item_editor", ItemEditorGui::new);

    private final ItemStackHandler stackHandler = new ItemStackHandler(1);

    private ItemStack getStack() {
        return this.stackHandler.getStackInSlot(0);
    }

    private void setStack(ItemStack stack) {
        this.stackHandler.setStackInSlot(0, stack);
    }

    @Override
    public ModularPanel buildUI(GuiData data, PanelSyncManager syncManager) {
        ItemStack itemStack = syncManager.getPlayer().getHeldItem();
        if (itemStack != null) {
            setStack(itemStack.copy());
            syncManager.getPlayer().setCurrentItemOrArmor(0, null);
            syncManager.addCloseListener(player -> {
                if (getStack() != null) {
                    if (syncManager.getPlayer().getHeldItem() == null) {
                        player.setCurrentItemOrArmor(0, getStack());
                    } else {
                        player.inventory.addItemStackToInventory(getStack());
                    }
                }
            });
        }
        ModularPanel panel = ModularPanel.defaultPanel("item_editor");
        return panel.bindPlayerInventory()
                .child(new Column()
                        .crossAxisAlignment(Alignment.CrossAxis.START)
                        .sizeRel(1f)
                        .margin(7)
                        .child(IKey.str("Item Editor").asWidget().marginTop(7).marginBottom(3))
                        .child(new ItemSlot().slot(new ModularSlot(this.stackHandler, 0)))
                        .child(new Row()
                                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                                .height(16)
                                .margin(0, 4)
                                .child(IKey.str("Meta: ").asWidget())
                                .child(new TextFieldWidget()
                                        .size(50, 16)
                                        .value(new IntSyncValue(() -> {
                                            ItemStack stack = getStack();
                                            return stack != null ? stack.getItemDamage() : 0;
                                        }, val -> {
                                            if (!syncManager.isClient())
                                                getStack().setItemDamage(val);
                                        }))
                                        .setNumbers(0, Short.MAX_VALUE - 1))
                                .child(IKey.str("  Amount: ").asWidget())
                                .child(new TextFieldWidget()
                                        .size(30, 16)
                                        .value(new IntSyncValue(() -> {
                                            ItemStack stack = getStack();
                                            return stack != null ? stack.stackSize : 0;
                                        }, value -> {
                                            if (!syncManager.isClient())
                                                getStack().stackSize = value;
                                        }))
                                        .setNumbers(1, 127)))
                        .child(new TextFieldWidget()
                                .height(20)
                                .widthRel(1f)
                                .value(new StringSyncValue(() -> {
                                    ItemStack stack = getStack();
                                    if (stack == null || !stack.hasTagCompound()) {
                                        return "";
                                    }
                                    return stack.getTagCompound().toString();
                                }, val -> {
                                    if (!syncManager.isClient()) {
                                        try {
                                            getStack().setTagCompound((NBTTagCompound) JsonToNBT.func_150315_a(val));
                                        } catch (NBTException ignored) {
                                        }
                                    }
                                }))
                                .setValidator(s -> s)));
    }

    public static class Command extends CommandBase {

        @Override
        public String getCommandName() {
            return "itemEditor";
        }

        @Override
        public String getCommandUsage(ICommandSender sender) {
            return "/itemEditor";
        }

        @Override
        public void processCommand(ICommandSender sender, String[] args) {
            if (sender instanceof EntityPlayerMP entityPlayerMP && entityPlayerMP.capabilities.isCreativeMode) {
                GUI.open((EntityPlayerMP) sender);
            } else {
                throw new CommandException("Player must be creative mode!");
            }
        }
    }
}
