package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.widget.IDelegatingWidget;
import com.cleanroommc.modularui.api.widget.INotifyEnabled;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widget.sizer.StandardResizer;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Very basic implementation of {@link IWidget}.
 */
public abstract class AbstractWidget implements IWidget {

    // gui context
    private boolean valid = false;
    private IWidget parent = null;
    private ModularPanel panel = null;
    private ModularGuiContext context = null;

    @Nullable private String name;
    private boolean enabled = true;
    private int timeHovered = -1;
    private int timeBelowMouse = -1;

    private final Area area = new Area();
    private StandardResizer resizer;

    /**
     * Returns the screen of the panel of this widget is being opened in.
     *
     * @return the screen of this widget
     * @throws IllegalStateException if {@link #isValid()} returns false
     */
    @Override
    public ModularScreen getScreen() {
        return getPanel().getScreen();
    }

    @Override
    public void scheduleResize() {
        this.resizer.markDirty();
    }

    @Override
    public boolean requiresResize() {
        return this.resizer.requiresResize();
    }

    /**
     * Called when a panel is opened. Use {@link #onInit()} and {@link #afterInit()} for custom logic.
     *
     * @param parent the parent this element belongs to
     * @param late   true if this is called some time after the widget tree of the parent has been initialised
     */
    @ApiStatus.Internal
    @Override
    public final void initialise(@NotNull IWidget parent, boolean late) {
        this.timeHovered = -1;
        this.timeBelowMouse = -1;
        if (this.resizer == null) {
            throw new IllegalStateException("Resizer must be set before the widget initializes! Affected widget: " + this);
        }
        if (!(this instanceof ModularPanel)) {
            this.parent = parent;
            this.panel = parent.getPanel();
            this.context = parent.getContext();
            getArea().z(parent.getArea().z() + 1);
            if (parent instanceof AbstractWidget aw) {
                this.resizer.initialize(aw.resizer, parent.getScreen().getResizeNode());
            } else {
                this.resizer.initialize(parent.resizer(), parent.getScreen().getResizeNode());
            }
        }
        this.valid = true;
        onInitInternal(late);
        onInit();
        if (hasChildren()) {
            for (IWidget child : getChildren()) {
                child.initialise(this, false);
            }
        }
        afterInit();
        onResized();
    }

    /**
     * Internal hook called during {@link #initialise(IWidget, boolean)} before {@link #onInit()}.
     *
     * @param late true if this is called some time after the widget tree of the parent has been initialised
     */
    void onInitInternal(boolean late) {}

    /**
     * Called after this widget is initialised and before the children are initialised.
     */
    @ApiStatus.OverrideOnly
    public void onInit() {}

    /**
     * Called after this widget is initialised and after the children are initialised.
     */
    @ApiStatus.OverrideOnly
    public void afterInit() {}

    /**
     * Called when this widget is removed from the widget tree or after the panel is closed.
     * Overriding this is fine, but super must be called.
     */
    @MustBeInvokedByOverriders
    @Override
    public void dispose() {
        if (hasChildren()) {
            for (IWidget child : getChildren()) {
                child.dispose();
            }
        }
        if (!(this instanceof ModularPanel)) {
            this.panel = null;
            this.parent = null;
            this.context = null;
        }
        resizer().dispose();
        this.timeHovered = -1;
        this.timeBelowMouse = -1;
        this.valid = false;
    }

    // -------------------
    // === Gui context ===
    // -------------------

    /**
     * Returns if this widget is currently part of an open panel. Only if this is true information about parent, panel and gui context can
     * be obtained.
     *
     * @return true if this widget is part of an open panel
     */
    @Override
    public boolean isValid() {
        return valid;
    }

    /**
     * Increments the hover and below-mouse timers while this widget is hovered or below the mouse.
     */
    @Override
    public void onUpdate() {
        if (isHovering()) this.timeHovered++;
        if (isBelowMouse()) this.timeBelowMouse++;
    }

    /**
     * Resets the hover timer to zero when the mouse starts hovering this widget.
     * Overriding this is fine, but super must be called.
     */
    @MustBeInvokedByOverriders
    @Override
    public void onMouseStartHover() {
        this.timeHovered = 0;
    }

    /**
     * Resets the hover timer when the mouse stops hovering this widget.
     * Overriding this is fine, but super must be called.
     */
    @MustBeInvokedByOverriders
    @Override
    public void onMouseEndHover() {
        this.timeHovered = -1;
    }

    /**
     * Resets the below-mouse timer to zero when the mouse enters the area of this widget.
     * Overriding this is fine, but super must be called.
     */
    @MustBeInvokedByOverriders
    @Override
    public void onMouseEnterArea() {
        this.timeBelowMouse = 0;
    }

    /**
     * Resets the below-mouse timer when the mouse leaves the area of this widget.
     * Overriding this is fine, but super must be called.
     */
    @MustBeInvokedByOverriders
    @Override
    public void onMouseLeaveArea() {
        this.timeBelowMouse = -1;
    }

    /**
     * Returns whether this widget has been hovered for at least the given number of ticks.
     *
     * @param ticks number of ticks to check against
     * @return true if the hover time is at least {@code ticks}
     */
    @Override
    public boolean isHoveringFor(int ticks) {
        return timeHovered >= ticks;
    }

    /**
     * Returns whether the mouse has been below this widget for at least the given number of ticks.
     *
     * @param ticks number of ticks to check against
     * @return true if the below-mouse time is at least {@code ticks}
     */
    @Override
    public boolean isBelowMouseFor(int ticks) {
        return timeBelowMouse >= ticks;
    }

    public int getTicksHovered() {
        return timeHovered;
    }

    public int getTicksBelowMouse() {
        return timeBelowMouse;
    }

    /**
     * Returns the area of this widget. This contains information such as position, size, relative position to parent, padding and margin.
     * Even tho this is a mutable object, you should refrain from modifying the values.
     *
     * @return area of this widget
     */
    @Override
    public Area getArea() {
        return area;
    }

    /**
     * Shortcut to get the area of the parent. Delegating widgets are skipped, so the area of the first non-delegating ancestor is returned.
     *
     * @return parent area
     */
    public Area getParentArea() {
        IWidget parent = getParent();
        while (parent instanceof IDelegatingWidget dw) {
            parent = dw.getParent();
        }
        return parent.getArea();
    }

    /**
     * Returns if this widget is currently enabled. Disabled widgets (and all its children) are not rendered and can't be interacted with.
     *
     * @return true if this widget is enabled.
     */
    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Sets enabled state. Disabled widgets (and all its children) are not rendered and can't be interacted with.
     *
     * @param enabled enabled state
     */
    @Override
    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (isValid() && getParent() instanceof INotifyEnabled notifyEnabled) {
                notifyEnabled.onChildChangeEnabled(this, enabled);
            }
        }
    }

    /**
     * Returns the parent of this widget. If this is a {@link ModularPanel} this will always return null contrary to the annotation.
     *
     * @return the parent of this widget
     * @throws IllegalStateException if {@link #isValid()} returns false
     */
    @Override
    public @NotNull IWidget getParent() {
        if (!isValid()) {
            throw new IllegalStateException(this + " is not in a valid state!");
        }
        return parent;
    }

    /**
     * Returns the gui context of the screen this widget is part of.
     *
     * @return the gui context of this widget
     * @throws IllegalStateException if {@link #isValid()} returns false
     */
    @Override
    public ModularGuiContext getContext() {
        if (!isValid()) {
            throw new IllegalStateException(this + " is not in a valid state!");
        }
        return context;
    }

    /**
     * Used to set the gui context on panels internally.
     */
    @ApiStatus.Internal
    protected final void setContext(ModularGuiContext context) {
        this.context = context;
    }

    /**
     * Returns the panel of this widget is being opened in.
     *
     * @return the panel of this widget
     * @throws IllegalStateException if {@link #isValid()} returns false
     */
    @Override
    public @NotNull ModularPanel getPanel() {
        if (!isValid()) {
            throw new IllegalStateException(this + " is not in a valid state!");
        }
        return panel;
    }

    @Override
    public @NotNull StandardResizer resizer() {
        return this.resizer;
    }

    protected final StandardResizer rawResizer() {
        return this.resizer;
    }

    /**
     * Replaces the resizer of this widget. If this widget is currently valid and already has a resizer, the new resizer is
     * notified that it replaces the old one.
     *
     * @param resizer the new resizer, must not be null
     * @throws NullPointerException if resizer is null
     */
    protected void resizer(StandardResizer resizer) {
        Objects.requireNonNull(resizer);
        if (this.resizer == resizer) return;
        if (isValid() && this.resizer != null) {
            resizer.replacementOf(this.resizer);
        }
        this.resizer = resizer;
    }

    @Override
    public @Nullable String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public boolean isName(String name) {
        return Objects.equals(name, this.name);
    }

    public boolean nameContains(String part) {
        return this.name != null && this.name.contains(part);
    }

    /**
     * This is only used in {@link #toString()}.
     *
     * @return the simple class name or other fitting name
     */
    protected String getTypeName() {
        return getClass().getSimpleName();
    }

    /**
     * @return the simple class plus the debug name if set
     */
    @Override
    public String toString() {
        if (getName() != null) {
            return getTypeName() + "#" + getName();
        }
        return getTypeName();
    }
}
