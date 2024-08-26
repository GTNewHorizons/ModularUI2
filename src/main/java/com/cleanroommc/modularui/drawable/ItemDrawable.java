package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.GuiScreenWrapper;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.GameObjectHelper;
import com.cleanroommc.modularui.utils.JsonHelper;
import com.cleanroommc.modularui.widget.Widget;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.NoSuchElementException;

public class ItemDrawable implements IDrawable {

    private ItemStack item = null;

    public ItemDrawable() {
    }

    public ItemDrawable(@Nullable ItemStack item) {
        this.item = item;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        GuiDraw.drawItem(this.item, x, y, width, height);
    }

    @Override
    public Widget<?> asWidget() {
        return IDrawable.super.asWidget().size(16);
    }

    @Override
    public Icon asIcon() {
        return IDrawable.super.asIcon().size(16);
    }

    public ItemDrawable setItem(@Nullable ItemStack item) {
        this.item = item;
        return this;
    }

    public static ItemDrawable ofJson(JsonObject json) {
        String itemS = JsonHelper.getString(json, null, "item");
        if (itemS == null) throw new JsonParseException("Item property not found!");
        String[] parts = itemS.split(":");
        if (parts.length < 2)
            throw new JsonParseException("Item property must have be in the format 'mod:item_name:meta'");
        int meta = 0;
        if (parts.length > 2) {
            try {
                meta = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                throw new JsonParseException(e);
            }
        }
        ItemStack item;
        try {
            item = GameObjectHelper.getItemStack(parts[0], parts[1], meta);
        } catch (NoSuchElementException e) {
            throw new JsonParseException(e);
        }
        if (json.has("nbt")) {
            try {
                NBTTagCompound nbt = (NBTTagCompound) JsonToNBT.func_150315_a(JsonHelper.getObject(json, new JsonObject(), o -> o, "nbt").toString());
                item.setTagCompound(nbt);
            } catch (NBTException e) {
                throw new JsonParseException(e);
            }
        }
        return new ItemDrawable(item);
    }
}
