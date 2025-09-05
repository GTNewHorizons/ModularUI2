package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.JsonHelper;
import com.google.gson.JsonObject;

public class WidgetSlotTheme extends WidgetTheme {

    private final int slotHoverColor;
    private final boolean useCustomSlotTextures;
    private final IDrawable inventorySlotBackground;
    private final IDrawable hotbarSlotBackground;

    public WidgetSlotTheme(IDrawable background, int slotHoverColor) {
        this(background, slotHoverColor, false, null, null);
    }

    public WidgetSlotTheme(IDrawable background, int slotHoverColor, boolean useCustomSlotTextures, IDrawable inventory, IDrawable hotbar) {
        super(background, null, Color.WHITE.main, 0xFF404040, false);
        this.slotHoverColor = slotHoverColor;
        this.useCustomSlotTextures=useCustomSlotTextures;
        this.inventorySlotBackground=inventory;
        this.hotbarSlotBackground=hotbar;
    }

    public WidgetSlotTheme(WidgetTheme parent, JsonObject json, JsonObject fallback) {
        super(parent, json, fallback);
        this.slotHoverColor = JsonHelper.getColorWithFallback(json, fallback, ((WidgetSlotTheme) parent).getSlotHoverColor(), IThemeApi.SLOT_HOVER_COLOR);
        this.useCustomSlotTextures=JsonHelper.getBoolWithFallback(json, fallback, ((WidgetSlotTheme) parent).getUseCustomSlotTextures(), IThemeApi.SLOT_CUSTOM_TEXTURES);
        this.inventorySlotBackground = JsonHelper.deserializeWithFallback(json, fallback, IDrawable.class, ((WidgetSlotTheme) parent).getInventorySlotBackground(), IThemeApi.SLOT_INVENTORY_BACKGROUND);
        this.hotbarSlotBackground = JsonHelper.deserializeWithFallback(json, fallback, IDrawable.class, ((WidgetSlotTheme) parent).getHotbarSlotBackground(), IThemeApi.SLOT_HOTBAR_BACKGROUND);
    }

    public int getSlotHoverColor() {
        return this.slotHoverColor;
    }
    public boolean getUseCustomSlotTextures() {return this.useCustomSlotTextures;}
    public IDrawable getInventorySlotBackground() {return this.inventorySlotBackground;}
    public IDrawable getHotbarSlotBackground() {return this.hotbarSlotBackground;}
}
