package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.ScrollWidget;
import com.cleanroommc.modularui.widget.SingleChildWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.WidgetTree;
import com.cleanroommc.modularui.widget.sizer.Area;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class DropDownMenu extends SingleChildWidget<DropDownMenu> implements Interactable {

    private static final IKey NONE = IKey.str("None");
    private final DropDownWrapper menu = new DropDownWrapper();
    private IDrawable arrowClosed;
    private IDrawable arrowOpened;
    private static int offsetArrow = 5;

    public DropDownMenu() {
        menu.setEnabled(false);
        menu.background(GuiTextures.BUTTON_CLEAN);
        child(menu);
        setArrows(GuiTextures.ARROW_UP, GuiTextures.ARROW_DOWN);
        background();
    }

    public int getSelectedIndex() {
        return menu.getCurrentIndex();
    }

    public DropDownMenu setSelectedIndex(int index) {
        menu.setCurrentIndex(index);
        return getThis();
    }

    public DropDownMenu addChoice(Function<Integer, DropDownItem> itemGetter) {
        menu.addChoice(itemGetter);
        return getThis();
    }

    public DropDownMenu setArrows(IDrawable arrowClosed, IDrawable arrowOpened) {
        this.arrowClosed = arrowClosed;
        this.arrowOpened = arrowOpened;
        return getThis();
    }

    public DropDownMenu setMaxItemsToDisplay(int maxItems) {
        menu.setMaxItemsToDisplay(maxItems);
        return getThis();
    }

    public DropDownMenu addChoice(ItemSelected onSelect, IDrawable... drawable) {
        DropDownItem item = new DropDownItem();
        return addChoice(index ->
                item.onMouseReleased(m -> {
                            menu.setOpened(false);
                            menu.setCurrentIndex(index);
                            onSelect.selected(this);
                            return true;
                        })
                        .overlay(drawable));
    }

    public DropDownMenu addChoice(ItemSelected onSelect, String text) {
        return addChoice(onSelect, IKey.str(text));
    }

    public DropDownMenu setDropDownDirection(DropDownDirection direction) {
        menu.setDropDownDirection(direction);
        return getThis();
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (!menu.isOpen()) {
            menu.setOpened(true);
            menu.setEnabled(true);
            return Result.SUCCESS;
        }
        menu.setOpened(false);
        menu.setEnabled(false);
        return Result.SUCCESS;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.draw(context, widgetTheme);
        Area area = getArea();
        int smallerSide = Math.min(area.width, area.height);
        WidgetTheme wt = getActiveWidgetTheme(widgetTheme, isHovering());
        if (menu.getSelectedItem() != null) {
            menu.getSelectedItem().setEnabled(true);
            menu.getSelectedItem().drawBackground(context, widgetTheme);
            menu.getSelectedItem().draw(context, widgetTheme);
            menu.getSelectedItem().drawForeground(context);
            menu.getSelectedItem().drawOverlay(context, widgetTheme);
        } else {
            NONE.draw(context, 0, 0, area.width, area.height, wt);
        }

        int arrowSize = smallerSide / 2;
        if (menu.isOpen()) {
            arrowOpened.draw(context, area.width - arrowSize - offsetArrow, arrowSize / 2, arrowSize, arrowSize, wt);
        } else {
            arrowClosed.draw(context, area.width - arrowSize - offsetArrow, arrowSize / 2, arrowSize, arrowSize, wt);
        }
    }

    @Override
    public DropDownMenu background(IDrawable... background) {
        menu.background(background);
        return super.background(background);
    }

    public enum DropDownDirection {
        UP(0, -1),
        DOWN(0, 1);

        private final int xOffset;
        private final int yOffset;

        DropDownDirection(int xOffset, int yOffset) {
            this.xOffset = xOffset;
            this.yOffset = yOffset;
        }

        public int getXOffset() {
            return xOffset;
        }

        public int getYOffset() {
            return yOffset;
        }
    }

    private static class DropDownWrapper extends ScrollWidget<DropDownWrapper> {

        private DropDownDirection direction = DropDownDirection.DOWN;
        private int maxItemsOnDisplay = 10;
        private final List<IWidget> children = new ArrayList<>();
        private boolean open;
        private int count = 0;
        private int currentIndex = -1;

        public void setDropDownDirection(DropDownDirection direction) {
            this.direction = direction;
        }

        @Override
        public void onUpdate() {
            if (!open) {
                setEnabled(false);
            }
        }

        public void setOpened(boolean open) {
            this.open = open;
            rebuild();
        }

        public boolean isOpen() {
            return open;
        }

        public void addChoice(Function<Integer, DropDownItem> itemGetter) {
            children.add(itemGetter.apply(count));
            count++;
        }

        public int getCurrentIndex() {
            return currentIndex;
        }

        public void setCurrentIndex(int currentIndex) {
            this.currentIndex = currentIndex;
        }

        @Override
        public @NotNull List<IWidget> getChildren() {
            return Collections.unmodifiableList(children);
        }

        public IWidget getSelectedItem() {
            if (currentIndex < 0 || currentIndex >= count) {
                return null;
            }
            return getChildren().get(currentIndex);
        }

        @Override
        public DropDownWrapper background(IDrawable... background) {
            for (IWidget child : getChildren()) {
                if (!(child instanceof Widget<?> childAsWidget)) continue;
                childAsWidget.background(background);
            }
            return super.background(background);
        }

        public void setMaxItemsToDisplay(int maxItems) {
            maxItemsOnDisplay = maxItems;
        }

        @Override
        public void onResized() {
            super.onResized();
            if (!isValid()) return;
            Area parentArea = getParent().getArea();
            int maxItems = Math.min(maxItemsOnDisplay, children.size());
            size(parentArea.width, parentArea.height * maxItems);
            pos(0, direction == DropDownDirection.UP ? -parentArea.height * (maxItems + 1) : parentArea.height);

            List<IWidget> children = getChildren();
            for (int i = 0; i < children.size(); i++) {
                IWidget child = children.get(i);
                child.getArea().setSize(parentArea.width, parentArea.height);
                child.getArea().setPos(0, parentArea.height * i);
                child.getFlex().left(0).top(parentArea.height * i);
                child.setEnabled(open);
            }
        }

        private void rebuild() {
            WidgetTree.resize(this);
        }
    }

    public static class DropDownItem extends ButtonWidget<DropDownItem> {

        @Override
        public boolean canClickThrough() {
            return false;
        }

        @Override
        public WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
            return theme.getFallback();
        }
    }

    @FunctionalInterface
    public interface ItemSelected {

        void selected(DropDownMenu menu);
    }
}
