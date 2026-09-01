package com.cleanroommc.modularui.hud;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

/**
 * A single HUD element rendered on top of the game world.
 * <p>
 * A HUD element is <b>display-only</b>: it cannot capture mouse or keyboard input, has no
 * hover events, no focus, and no drag support. The context's mouse/keyboard state is still
 * updated each frame so widgets can read absolute mouse position in their {@code draw()} methods.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * HudElement hud = new HudElement("mymod", ctx -> {
 *     ModularPanel panel = new ModularPanel("status");
 *     panel.size(100, 20).pos(5, 5);
 *     panel.child(new TextWidget()
 *         .text(IKey.dynamic(() -> "Current time: " + System.currentTimeMillis())));
 *     return panel;
 * });
 * HudManager.register(hud);
 * }</pre>
 */
@SideOnly(Side.CLIENT)
public class HudElement {

    @Getter private final ModularScreen screen;
    private final HudWrapper wrapper;

    @Getter private boolean enabled = true;
    private boolean visibleInWorld = true;
    private boolean visibleInGui = true;
    private boolean overGui = false;
    @Getter private int renderPriority = 0;

    /**
     * Creates a new HUD element with the given owner and main panel.
     *
     * @param owner     owner of this element (usually a mod id). Used for theme lookup.
     * @param mainPanel main panel of this element.
     */
    public HudElement(@NotNull String owner, @NotNull ModularPanel mainPanel) {
        this(owner, ctx -> mainPanel);
    }

    /**
     * Creates a new HUD element with the given owner and panel creator.
     *
     * @param owner         owner of this element (usually a mod id). Used for theme lookup.
     * @param panelCreator  function which creates the main panel. Receives the (read-only)
     *                      {@link ModularGuiContext} of this element.
     */
    public HudElement(@NotNull String owner, @NotNull Function<ModularGuiContext, ModularPanel> panelCreator) {
        Objects.requireNonNull(owner, "The owner must not be null!");
        Objects.requireNonNull(panelCreator, "The panel creator must not be null!");
        this.screen = new HudScreen(owner, panelCreator);
        this.wrapper = new HudWrapper(this.screen);
    }

    /**
     * Sets whether this element is rendered when no screen is open (in-game).
     *
     * @param visibleInWorld true to render in-world
     * @return this
     */
    public HudElement visibleInWorld(boolean visibleInWorld) {
        this.visibleInWorld = visibleInWorld;
        return this;
    }

    /**
     * Sets whether and where this element is drawn while a {@code GuiScreen} (inventory, etc.) is
     * open. Visible underneath open screens by default, not over them: most HUD elements (persistent
     * overlays, ambient status) should not cover an open screen's own content.
     *
     * @param visibleInGui true to render at all while a screen is open
     * @param isOverGui    if visible, true to draw on top of the screen, false to draw underneath it
     * @return this
     */
    public HudElement visibleInGui(boolean visibleInGui, boolean isOverGui) {
        this.visibleInGui = visibleInGui;
        this.overGui = isOverGui;
        return this;
    }

    /**
     * Sets the render priority. Higher values are drawn on top of lower values.
     *
     * @param renderPriority priority value
     * @return this
     */
    public HudElement renderPriority(int renderPriority) {
        this.renderPriority = renderPriority;
        return this;
    }

    /**
     * Enables or disables this element. Disabled elements are not rendered or ticked.
     *
     * @param enabled true to enable
     * @return this
     */
    public HudElement enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public ModularPanel getPanel() {
        return screen.getMainPanel();
    }

    /**
     * @return true if this element should be rendered in-world (no screen open)
     */
    public boolean isVisibleInWorld() {
        return enabled && visibleInWorld;
    }

    /**
     * @return true if this element should be rendered on top of an open {@code GuiScreen}
     */
    public boolean isVisibleOverGui() {
        return enabled && visibleInGui && overGui;
    }

    /**
     * @return true if this element should be rendered underneath an open {@code GuiScreen}
     */
    public boolean isVisibleUnderGui() {
        return enabled && visibleInGui && !overGui;
    }

    /**
     * @return true if this element can be ticked (enabled)
     */
    boolean canDraw() {
        return enabled;
    }

    @Nullable
    public String getOwner() {
        return screen.getOwner();
    }

    @Override
    public String toString() {
        return "HudElement[" + screen.getOwner() + ":" + screen.getName() + "]";
    }
}
