package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.value.ISyncOrValue;
import com.cleanroommc.modularui.api.value.IValue;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerIngredientProvider;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.value.ObjectValue;
import com.cleanroommc.modularui.value.sync.GenericSyncValue;
import com.cleanroommc.modularui.widget.Widget;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An item slot which only purpose is to display an item stack.
 * The displayed item stack can be supplied directly, by an {@link ObjectValue} dynamically or by a {@link GenericSyncValue} synced.
 * Players can not interact with this widget in any form.
 */
public class ItemDisplayWidget extends Widget<ItemDisplayWidget> implements RecipeViewerIngredientProvider {

    private IValue<ItemStack> value;
    private boolean displayAmount = false;

    public ItemDisplayWidget() {
        size(18);
    }

    @Override
    public boolean isValidSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        return syncOrValue.isValueOfType(ItemStack.class);
    }

    @Override
    protected void setSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        super.setSyncOrValue(syncOrValue);
        this.value = syncOrValue.castValueNullable(ItemStack.class);
    }

    @Override
    protected WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getItemSlotTheme();
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        ItemStack item = value.getValue();
        if (!Platform.isStackEmpty(item)) {
            int contentOffsetY = 1;
            int contentOffsetX = 1;
            GuiDraw.drawItem(
                    item,
                    contentOffsetX,
                    contentOffsetY,
                    getArea().width - 2 * contentOffsetX,
                    getArea().height - 2 * contentOffsetY,
                    context.getCurrentDrawingZ());
            if (this.displayAmount) {
                GuiDraw.drawScaledAmountText(item.stackSize, null, 1, 1, this.getArea().width-1,
                        this.getArea().height-1, Alignment.BottomRight, 0);
            }
        }
    }

    public ItemDisplayWidget item(IValue<ItemStack> itemSupplier) {
        setSyncOrValue(ISyncOrValue.orEmpty(itemSupplier));
        return this;
    }

    public ItemDisplayWidget item(ItemStack itemStack) {
        return item(new ObjectValue<>(ItemStack.class, itemStack));
    }

    public ItemDisplayWidget displayAmount(boolean displayAmount) {
        this.displayAmount = displayAmount;
        return this;
    }

    @Override
    public @Nullable ItemStack getStackForRecipeViewer() {
        return value.getValue();
    }
}
