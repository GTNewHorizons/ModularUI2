package com.cleanroommc.modularui.widget.sizer;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.ITreeNode;
import com.cleanroommc.modularui.api.layout.IResizeable;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Base node used by the widget resize tree.
 * <p>
 * A resize node tracks parent-child relationships, resize validation states.
 * Nodes may have a default parent and, temporarily, an override parent.
 */
public abstract class ResizeNode implements IResizeable, ITreeNode<ResizeNode> {

    private ResizeNode defaultParent;
    private ResizeNode parentOverride;
    private final List<ResizeNode> children = new ArrayList<>();
    private boolean defaultParentIsDelegating = false;
    private boolean requiresResize = true;

    /**
     * Returns the mutable list of child resize nodes.
     *
     * @return child resize nodes
     */
    @ApiStatus.Internal
    @Override
    public List<ResizeNode> getChildren() {
        return children;
    }

    /**
     * Returns the effective parent of this node.
     * <p>
     * If a parent override is present, it takes precedence over the default parent.
     *
     * @return the effective parent, or {@code null} if this node has no parent
     */
    @Override
    public ResizeNode getParent() {
        return parentOverride != null ? parentOverride : defaultParent;
    }

    /**
     * Replaces another node in the resize tree with this node.
     * <p>
     * Parent references and child references are transferred from the replaced node to this node. The replaced node is
     * disposed after replacement.
     *
     * @param node the node to replace
     */
    @ApiStatus.Internal
    public void replacementOf(ResizeNode node) {
        if (this == node) return;
        //ModularUI.LOGGER.info("Replacing resizer node {} with node {}", node, this);
        // remove this node from the tree by removing itself from the parents and removing the children
        if (this.defaultParent != null) this.defaultParent.children.remove(this);
        if (this.parentOverride != null) this.parentOverride.children.remove(this);
        for (ResizeNode n : this.children) {
            if (n.parentOverride == this) {
                n.setParentOverride(null);
            }
            if (n.defaultParent == this) {
                n.setDefaultParent(null);
            }
        }
        this.children.clear();

        // remove the node to replace from its tree and remember the exact position
        int defI = -1;
        int ovrI = -1;
        if (node.defaultParent != null) {
            defI = node.defaultParent.children.indexOf(node);
            if (defI >= 0) node.defaultParent.children.remove(defI);
        }
        if (node.parentOverride != null) {
            ovrI = node.parentOverride.children.indexOf(node);
            if (ovrI >= 0) node.parentOverride.children.remove(ovrI);
        }
        // take over the parent and ourselves to the new parents with the remembered position
        this.defaultParent = node.defaultParent;
        this.parentOverride = node.parentOverride;
        if (this.parentOverride != null) {
            if (ovrI < 0) throw new IllegalStateException();
            this.parentOverride.children.add(ovrI, this);
        }
        if (this.defaultParent != null) {
            if (defI < 0) throw new IllegalStateException();
            this.defaultParent.children.add(ovrI, this);
        }
        // take all children and update their parent to ourselves
        this.children.addAll(node.children);
        for (ResizeNode n : this.children) {
            if (n.parentOverride == node) n.parentOverride = this;
            if (n.defaultParent == node) n.defaultParent = this;
        }
        // finally invalidate replaced node
        node.dispose();
    }

    /**
     * Detaches this node from its parent and clears all parent and child references.
     */
    public void dispose() {
        if (getParent() != null) getParent().children.remove(this);
        this.defaultParent = null;
        this.parentOverride = null;
        this.children.clear();
    }

    private boolean removeFromParent(ResizeNode parent, ResizeNode parent2, ResizeNode replacement) {
        if (parent != null) {
            if (parent == replacement) return true;
            parent.children.remove(this);
        } else if (parent2 != null) {
            if (parent2 == replacement) return true;
            parent2.children.remove(this);
        }
        return false;
    }

    /**
     * Initializes this node before it is used by the resize tree.
     *
     * @param defaultParent the default parent node
     * @param root          the root resize node
     */
    @ApiStatus.Internal
    public void initialize(ResizeNode defaultParent, ResizeNode root) {
        setDefaultParent(defaultParent);
    }

    /**
     * Sets the default parent for this node.
     *
     * @param resizeNode the default parent, or {@code null} to detach from the default parent
     */
    @ApiStatus.Internal
    public void setDefaultParent(ResizeNode resizeNode) {
        //ModularUI.LOGGER.info("Set default parent of {} to {}. Current: default: {}, override: {}", this, resizeNode, this.defaultParent, this.parentOverride);
        if (resizeNode == this) throw new IllegalArgumentException("Tried to set itself as default parent in " + this);
        if (removeFromParent(this.defaultParent, null, resizeNode)) return;
        this.defaultParent = resizeNode;
        if (this.parentOverride == null && resizeNode != null) {
            resizeNode.children.add(this);
        }
    }

    /**
     * Sets an override parent for this node.
     * <p>
     * While present, the override parent is used as the effective parent instead of the default parent.
     *
     * @param resizeNode the override parent, or {@code null} to restore the default parent
     */
    protected void setParentOverride(ResizeNode resizeNode) {
        //ModularUI.LOGGER.info("Set override parent of {} to {}. Current: default: {}, override: {}", this, resizeNode, this.defaultParent, this.parentOverride);
        if (resizeNode == this) throw new IllegalArgumentException("Tried to set itself as parent override in " + this);
        if (removeFromParent(this.parentOverride, this.defaultParent, resizeNode)) return;
        this.parentOverride = resizeNode;
        if (this.parentOverride != null) {
            this.parentOverride.children.add(this);
        } else if (this.defaultParent != null) {
            this.defaultParent.children.add(this);
        }
    }

    /**
     * Sets whether calls should be delegated to the default parent when this node has an override parent.
     *
     * @param defaultParentIsDelegating {@code true} to delegate resize lifecycle calls to the default parent
     */
    @ApiStatus.Internal
    public void setDefaultParentIsDelegating(boolean defaultParentIsDelegating) {
        this.defaultParentIsDelegating = defaultParentIsDelegating;
    }

    /**
     * @return {@code true} if this node currently has an override parent
     */
    public boolean hasParentOverride() {
        return this.parentOverride != null;
    }

    @Override
    public void initResizing(boolean onOpen) {
        if (this.defaultParentIsDelegating && this.parentOverride != null) {
            this.defaultParent.initResizing(onOpen);
        }
    }

    /**
     * Resets implementation-specific resize state.
     */
    public void reset() {}

    /**
     * Marks this node as requiring another resize pass.
     */
    public void markDirty() {
        this.requiresResize = true;
    }

    /**
     * Called when this node has completed resizing.
     */
    public void onResized() {
        this.requiresResize = false;
        if (this.defaultParentIsDelegating && this.parentOverride != null) {
            this.defaultParent.onResized();
        }
    }

    /**
     * Called after the full resize tree has completed resizing.
     */
    public void postFullResize() {
        if (this.defaultParentIsDelegating && this.parentOverride != null) {
            this.defaultParent.postFullResize();
        }
    }

    /**
     * @return {@code true} if this node requires a resize pass
     */
    public boolean requiresResize() {
        return this.requiresResize;
    }

    public boolean widthDependsOnParent() {
        return false;
    }

    public boolean heightDependsOnParent() {
        return false;
    }

    public boolean xDependsOnParent() {
        return false;
    }

    public boolean yDependsOnParent() {
        return false;
    }

    public boolean dependsOnParentX() {
        return widthDependsOnParent() || xDependsOnParent();
    }

    public boolean dependsOnParentY() {
        return heightDependsOnParent() || yDependsOnParent();
    }

    public boolean dependsOnParent() {
        return dependsOnParentX() || dependsOnParentY();
    }

    public boolean dependsOnParent(GuiAxis axis) {
        return axis.isHorizontal() ? dependsOnParentX() : dependsOnParentY();
    }

    public boolean dependsOnChildrenX() {
        return false;
    }

    public boolean dependsOnChildrenY() {
        return false;
    }

    public boolean dependsOnChildren() {
        return dependsOnChildrenX() || dependsOnChildrenY();
    }

    public boolean dependsOnChildren(GuiAxis axis) {
        return axis.isHorizontal() ? dependsOnChildrenX() : dependsOnChildrenY();
    }

    public boolean isSameResizer(ResizeNode node) {
        return node == this;
    }

    public boolean isLayout() {
        return false;
    }

    /**
     * Lays out child nodes.
     *
     * @return {@code true} if child layout completed
     */
    public boolean layoutChildren() {
        return true;
    }

    /**
     * Performs post-layout processing for child nodes.
     *
     * @return {@code true} if post-layout processing completed
     */
    public boolean postLayoutChildren() {
        return true;
    }

    /**
     * Validates or expands this node on the requested axis.
     *
     * @param axis the axis to check
     */
    @ApiStatus.Internal
    public void checkExpanded(@Nullable GuiAxis axis) {}

    public boolean isDecoration() {
        return false;
    }

    public abstract boolean hasYPos();

    public abstract boolean hasXPos();

    public abstract boolean hasHeight();

    public abstract boolean hasWidth();

    public abstract boolean hasStartPos(GuiAxis axis);

    public abstract boolean hasEndPos(GuiAxis axis);

    public boolean hasPos(GuiAxis axis) {
        return axis.isHorizontal() ? hasXPos() : hasYPos();
    }

    public boolean hasSize(GuiAxis axis) {
        return axis.isHorizontal() ? hasWidth() : hasHeight();
    }

    public boolean isExpanded() {
        return false;
    }

    /**
     * @return {@code true} if this node occupies the full available size
     */
    public abstract boolean isFullSize();

    /**
     * @return {@code true} if this node has a fixed size
     */
    public abstract boolean hasFixedSize();

    /**
     * @return human-readable information for resize debugging
     */
    public abstract String getDebugDisplayName();

    @Override
    public abstract String toString();
}
