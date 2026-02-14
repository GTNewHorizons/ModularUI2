package com.cleanroommc.modularui.widgets.menu;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.ParentWidget;

public class Menu<W extends Menu<W>> extends ParentWidget<W> implements IMenuPart {

    private AbstractMenuButton<?> menuSource;

    void setMenuSource(AbstractMenuButton<?> source) {
        this.menuSource = source;
    }

    @Override
    public void onMouseLeaveArea() {
        super.onMouseLeaveArea();
        checkClose(true, true);
    }

    public void checkClose(boolean soft, boolean requireNoHover) {
        if (this.menuSource == null) return;
        if (soft || requireNoHover) {
            if (this.menuSource.isBelowMouse() || isSelfOrChildHovered()) return;
        }
        this.menuSource.closeMenu(soft);
        this.menuSource.checkClose(soft, requireNoHover);
    }

    @Override
    protected void onChildAdd(IWidget child) {
        super.onChildAdd(child);
        if (!child.resizer().hasHeight()) {
            child.resizer().height(12);
        }
        if (!child.resizer().hasWidth()) {
            child.resizer().widthRel(1f);
        }
    }

    @Override
    protected WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getWidgetTheme(IThemeApi.PANEL);
    }

    public AbstractMenuButton<?> getMenuSource() {
        return menuSource;
    }
}
