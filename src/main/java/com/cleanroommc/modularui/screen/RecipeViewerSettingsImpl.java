package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.api.RecipeViewerSettings;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerState;

import com.google.common.collect.AbstractIterator;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnmodifiableView;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Keeps track of everything related to recipe viewer in a Modular GUI. Recipe viewer is a mod like JEI, NEI and EMI.
 * By default, recipe viewer is disabled in client only GUIs.
 * This class can be safely interacted with even when recipe viewer is not installed.
 */
@SideOnly(Side.CLIENT)
public class RecipeViewerSettingsImpl implements RecipeViewerSettings {

    private RecipeViewerState recipeViewerState = RecipeViewerState.DEFAULT;
    private final List<IWidget> recipeViewerExclusionWidgets = new ArrayList<>();
    private final List<Rectangle> recipeViewerExclusionAreas = new ArrayList<>();

    /**
     * Force recipe viewer to be enabled
     */
    @Override
    public void enable() {
        this.recipeViewerState = RecipeViewerState.ENABLED;
    }

    /**
     * Force recipe viewer to be disabled
     */
    @Override
    public void disable() {
        this.recipeViewerState = RecipeViewerState.DISABLED;
    }

    /**
     * Only enabled recipe viewer in synced GUIs
     */
    @Override
    public void defaultState() {
        this.recipeViewerState = RecipeViewerState.DEFAULT;
    }

    /**
     * Checks if recipe viewer is enabled for a given screen
     *
     * @param screen modular screen
     * @return true if recipe viewer is enabled
     */
    @Override
    public boolean isEnabled(ModularScreen screen) {
        return this.recipeViewerState.test(screen);
    }

    /**
     * Adds an exclusion zone. Recipe viewer will always try to avoid exclusion zones. <br>
     * <b>If a widgets wishes to have an exclusion zone it should use {@link #addExclusionArea(IWidget)}!</b>
     *
     * @param area exclusion area
     */
    @Override
    public void addExclusionArea(Rectangle area) {
        if (!this.recipeViewerExclusionAreas.contains(area)) {
            this.recipeViewerExclusionAreas.add(area);
        }
    }

    /**
     * Removes an exclusion zone.
     *
     * @param area exclusion area to remove (must be the same instance)
     */
    @Override
    public void removeExclusionArea(Rectangle area) {
        this.recipeViewerExclusionAreas.remove(area);
    }

    /**
     * Adds an exclusion zone of a widget. Recipe viewer will always try to avoid exclusion zones. <br>
     * Useful when a widget is outside its panel.
     *
     * @param area widget
     */
    @Override
    public void addExclusionArea(IWidget area) {
        if (!this.recipeViewerExclusionWidgets.contains(area)) {
            this.recipeViewerExclusionWidgets.add(area);
        }
    }

    /**
     * Removes a widget exclusion area.
     *
     * @param area widget
     */
    @Override
    public void removeExclusionArea(IWidget area) {
        this.recipeViewerExclusionWidgets.remove(area);
    }

    @UnmodifiableView
    public List<Rectangle> getRecipeViewerExclusionAreas() {
        return Collections.unmodifiableList(this.recipeViewerExclusionAreas);
    }

    @UnmodifiableView
    public List<IWidget> getRecipeViewerExclusionWidgets() {
        return Collections.unmodifiableList(this.recipeViewerExclusionWidgets);
    }

    private final Iterable<Rectangle> allExclusionAreas = () -> new AbstractIterator<>() {

        private Iterator<Rectangle> rectIt = RecipeViewerSettingsImpl.this.recipeViewerExclusionAreas.iterator();
        private Iterator<IWidget> widgetIt = null;

        @Override
        protected Rectangle computeNext() {
            if (this.rectIt != null) {
                if (this.rectIt.hasNext()) return this.rectIt.next();
                this.rectIt = null;
                this.widgetIt = RecipeViewerSettingsImpl.this.recipeViewerExclusionWidgets.iterator();
            }
            if (this.widgetIt != null) {
                while (this.widgetIt.hasNext()) {
                    IWidget widget = this.widgetIt.next();
                    if (!widget.isValid()) {
                        this.widgetIt.remove();
                        continue;
                    }
                    if (!widget.isEnabled()) continue;
                    return widget.getArea();
                }
                this.widgetIt = null;
            }
            return endOfData();
        }
    };

    @ApiStatus.Internal
    public Iterable<Rectangle> getAllRecipeViewerExclusionAreas() {
        return this.allExclusionAreas;
    }
}
