package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.layout.ILayoutWidget;
import com.cleanroommc.modularui.api.layout.IResizeable;
import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.api.widget.IGuiElement;
import com.cleanroommc.modularui.api.widget.ISynced;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.ObjectList;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 * Helper class to apply actions to each widget in a tree.
 */
public class WidgetTree {

    private WidgetTree() {
    }

    public static List<IWidget> getAllChildrenByLayer(IWidget parent) {
        return getAllChildrenByLayer(parent, false);
    }

    public static List<IWidget> getAllChildrenByLayer(IWidget parent, boolean includeSelf) {
        List<IWidget> children = new ArrayList<>();
        if (includeSelf) children.add(parent);
        ObjectList<IWidget> parents = ObjectList.create();
        parents.add(parent);
        while (!parents.isEmpty()) {
            for (IWidget child : parents.removeFirst().getChildren()) {
                if (!child.getChildren().isEmpty()) {
                    parents.add(child);
                }
                children.add(child);
            }
        }
        return children;
    }

    public static boolean foreachChildByLayer(IWidget parent, Predicate<IWidget> consumer) {
        return foreachChildByLayer(parent, consumer, false);
    }

    public static boolean foreachChildByLayer(IWidget parent, Predicate<IWidget> consumer, boolean includeSelf) {
        if (includeSelf && !consumer.test(parent)) return false;
        ObjectList<IWidget> parents = ObjectList.create();
        parents.add(parent);
        while (!parents.isEmpty()) {
            for (IWidget child : parents.removeFirst().getChildren()) {
                if (child.hasChildren()) {
                    parents.addLast(child);
                }
                if (!consumer.test(child)) return false;
            }
        }
        return true;
    }

    public static boolean foreachChildByLayer2(IWidget parent, Predicate<IWidget> consumer, boolean includeSelf) {
        if (includeSelf && !consumer.test(parent)) return false;
        ObjectList<IWidget> parents = ObjectList.create();
        parents.add(parent);
        while (!parents.isEmpty()) {
            for (IWidget child : parents.removeFirst().getChildren()) {
                if (!consumer.test(child)) return false;

                if (child.hasChildren()) {
                    parents.addLast(child);
                }
            }
        }
        return true;
    }

    public static boolean foreachChild(IWidget parent, Predicate<IWidget> consumer, boolean includeSelf) {
        if (includeSelf && !consumer.test(parent)) return false;
        if (parent.getChildren().isEmpty()) return true;
        for (IWidget widget : parent.getChildren()) {
            if (!consumer.test(widget)) return false;
            if (!widget.getChildren().isEmpty() && foreachChild(widget, consumer, false)) {
                return false;
            }
        }
        return true;
    }

    public static boolean foreachChildReverse(IWidget parent, Predicate<IWidget> consumer, boolean includeSelf) {
        if (parent.getChildren().isEmpty()) {
            return !includeSelf || consumer.test(parent);
        }
        for (IWidget widget : parent.getChildren()) {
            if (!widget.getChildren().isEmpty() && foreachChildReverse(widget, consumer, false)) {
                return false;
            }
            if (!consumer.test(widget)) return false;
        }
        return !includeSelf || consumer.test(parent);
    }

    public static void drawTree(IWidget parent, GuiContext context) {
        drawTree(parent, context, false);
    }

    public static void drawTree(IWidget parent, GuiContext context, boolean ignoreEnabled) {
        if (!parent.isEnabled() && !ignoreEnabled) return;

        float alpha = parent.getPanel().getAlpha();
        IViewport viewport = parent instanceof IViewport ? (IViewport) parent : null;

        // transform stack according to the widget
        context.pushMatrix();
        parent.transform(context);

        boolean canBeSeen = parent.canBeSeen(context);

        // apply transformations to opengl
        GL11.glPushMatrix();
        context.applyToOpenGl();

        if (canBeSeen) {
            // draw widget
            GL11.glColorMask(true, true, true, true);
            GL11.glColor4f(1f, 1f, 1f, alpha);
            GL11.glEnable(GL11.GL_BLEND);
            WidgetTheme widgetTheme = parent.getWidgetTheme(context.getTheme());
            parent.drawBackground(context, widgetTheme);
            parent.draw(context, widgetTheme);
            parent.drawOverlay(context, widgetTheme);
        }

        if (viewport != null) {
            if (canBeSeen) {
                // draw viewport without children transformation
                GL11.glColor4f(1f, 1f, 1f, alpha);
                GL11.glEnable(GL11.GL_BLEND);
                viewport.preDraw(context, false);
                GL11.glPopMatrix();
                // apply children transformation of the viewport
                context.pushViewport(viewport, parent.getArea());
                viewport.transformChildren(context);
                // apply to opengl and draw with transformation
                GL11.glPushMatrix();
                context.applyToOpenGl();
                viewport.preDraw(context, true);
            } else {
                // only transform stack
                context.pushViewport(viewport, parent.getArea());
                viewport.transformChildren(context);
            }
        }
        // remove all opengl transformations
        GL11.glPopMatrix();

        // render all children if there are any
        List<IWidget> children = parent.getChildren();
        if (!children.isEmpty()) {
            children.forEach(widget -> drawTree(widget, context, false));
        }

        if (viewport != null) {
            if (canBeSeen) {
                // apply opengl transformations again and draw
                GL11.glColor4f(1f, 1f, 1f, alpha);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glPushMatrix();
                context.applyToOpenGl();
                viewport.postDraw(context, true);
                // remove children transformation of this viewport
                context.popViewport(viewport);
                GL11.glPopMatrix();
                // apply transformation again to opengl and draw
                GL11.glPushMatrix();
                context.applyToOpenGl();
                viewport.postDraw(context, false);
                GL11.glPopMatrix();
            } else {
                // only remove transformation
                context.popViewport(viewport);
            }
        }
        // remove all widget transformations
        context.popMatrix();
    }

    public static void drawTreeForeground(IWidget parent, GuiContext context) {
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glEnable(GL11.GL_BLEND);
        parent.drawForeground(context);

        List<IWidget> children = parent.getChildren();
        if (!children.isEmpty()) {
            children.forEach(widget -> drawTreeForeground(widget, context));
        }
    }

    @ApiStatus.Internal
    public static void onUpdate(IWidget parent) {
        foreachChildByLayer(parent, widget -> {
            widget.onUpdate();
            return true;
        }, true);
    }

    public static void resize(IWidget parent) {
        // TODO check if widget has a parent which depends on its children
        // resize each widget and calculate their relative pos
        if (!resizeWidget(parent, true) && !resizeWidget(parent, false)) {
            throw new IllegalStateException("Failed to resize widgets");
        }
        // now apply the calculated pos
        applyPos(parent);
        WidgetTree.foreachChildByLayer(parent, child -> {
            child.postResize();
            return true;
        }, true);
    }

    private static boolean resizeWidget(IWidget widget, boolean init) {
        boolean result = false;
        // first try to resize this widget
        IResizeable resizer = widget.resizer();
        if (resizer != null) {
            if (init) {
                widget.beforeResize();
                resizer.initResizing();
            }
            result = resizer.resize(widget);
        }

        // now resize all children and collect children which could not be fully calculated
        List<IWidget> anotherResize = new ArrayList<>();
        if (widget.hasChildren()) {
            widget.getChildren().forEach(iWidget -> {
                if (!resizeWidget(iWidget, init)) {
                    anotherResize.add(iWidget);
                }
            });
        }

        if (widget instanceof ILayoutWidget) {
            ((ILayoutWidget) widget).layoutWidgets();
        }

        // post resize this widget if possible
        if (resizer != null && !result) {
            result = resizer.postResize(widget);
        }

        if (widget instanceof ILayoutWidget) {
            ((ILayoutWidget) widget).postLayoutWidgets();
        }

        // now fully resize all children which needs it
        if (!anotherResize.isEmpty()) {
            anotherResize.forEach(iWidget -> {
                if (!iWidget.resizer().isFullyCalculated()) {
                    resizeWidget(iWidget, false);
                }
            });
        }

        if (result) widget.onResized();

        return result;
    }

    public static void applyPos(IWidget parent) {
        WidgetTree.foreachChildByLayer(parent, child -> {
            IResizeable resizer = child.resizer();
            if (resizer != null) {
                resizer.applyPos(child);
            }
            return true;
        }, true);
    }

    public static IGuiElement findParent(IGuiElement parent, Predicate<IGuiElement> filter) {
        if (parent == null) return null;
        while (!(parent instanceof ModularPanel)) {
            if (filter.test(parent)) {
                return parent;
            }
            parent = parent.getParent();
        }
        return filter.test(parent) ? parent : null;
    }

    public static IWidget findParent(IWidget parent, Predicate<IWidget> filter) {
        if (parent == null) return null;
        while (!(parent instanceof ModularPanel)) {
            if (filter.test(parent)) {
                return parent;
            }
            parent = parent.getParent();
        }
        return filter.test(parent) ? parent : null;
    }

    public static <T extends IWidget> T findParent(IWidget parent, Class<T> type) {
        if (parent == null) return null;
        while (!(parent instanceof ModularPanel)) {
            if (type.isAssignableFrom(parent.getClass())) {
                return (T) parent;
            }
            parent = parent.getParent();
        }
        return type.isAssignableFrom(parent.getClass()) ? (T) parent : null;
    }

    public static void collectSyncValues(GuiSyncManager syncHandler, ModularPanel panel) {
        AtomicInteger id = new AtomicInteger(0);
        String syncKey = GuiSyncManager.AUTO_SYNC_PREFIX + panel.getName();
        foreachChildByLayer(panel, widget -> {
            if (widget instanceof ISynced) {
                ISynced<?> synced = (ISynced<?>) widget;
                if (synced.isSynced()) {
                    syncHandler.syncValue(syncKey, id.getAndIncrement(), synced.getSyncHandler());
                }
            }
            return true;
        }, true);
    }
}
