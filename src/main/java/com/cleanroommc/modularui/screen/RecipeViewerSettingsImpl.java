package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.api.RecipeViewerSettings;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerState;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import com.google.common.collect.AbstractIterator;
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

    @Override
    public void enable() {
        this.recipeViewerState = RecipeViewerState.ENABLED;
    }

    @Override
    public void disable() {
        this.recipeViewerState = RecipeViewerState.DISABLED;
    }

    @Override
    public void defaultState() {
        this.recipeViewerState = RecipeViewerState.DEFAULT;
    }

    @Override
    public boolean isEnabled(ModularScreen screen) {
        return this.recipeViewerState.test(screen);
    }

    @Override
    public void addExclusionArea(Rectangle area) {
        if (!this.recipeViewerExclusionAreas.contains(area)) {
            this.recipeViewerExclusionAreas.add(area);
        }
    }

    @Override
    public void removeExclusionArea(Rectangle area) {
        this.recipeViewerExclusionAreas.remove(area);
    }

    @Override
    public void addExclusionArea(IWidget area) {
        if (!this.recipeViewerExclusionWidgets.contains(area)) {
            this.recipeViewerExclusionWidgets.add(area);
        }
    }

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
                    if (!widget.isEnabled() || !widget.getPanel().isOpen()) continue;
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
