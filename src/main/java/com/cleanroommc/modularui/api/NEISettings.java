package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularScreen;
import org.jetbrains.annotations.ApiStatus;

import java.awt.Rectangle;


/**
 * Keeps track of everything related to NEI in a Modular GUI.
 * By default, NEI is disabled in client only GUIs.
 * This class can be safely interacted with even when NEI is not installed.
 */
@ApiStatus.NonExtendable
public interface NEISettings {

    /**
     * Force NEI to be enabled
     */
    void enableNEI();

    /**
     * Force NEI to be disabled
     */
    void disableNEI();

    /**
     * Only enabled NEI in synced GUIs
     */
    void defaultNEI();

    /**
     * Checks if NEI is enabled for a given screen
     *
     * @param screen modular screen
     * @return true if NEI is enabled
     */
    boolean isNEIEnabled(ModularScreen screen);

    /**
     * Adds an exclusion zone. NEI will always try to avoid exclusion zones. <br>
     * <b>If a widgets wishes to have an exclusion zone it should use {@link #addNEIExclusionArea(IWidget)}!</b>
     *
     * @param area exclusion area
     */
    void addNEIExclusionArea(Rectangle area);

    /**
     * Removes an exclusion zone.
     *
     * @param area exclusion area to remove (must be the same instance)
     */
    void removeNEIExclusionArea(Rectangle area);

    /**
     * Adds an exclusion zone of a widget. NEI will always try to avoid exclusion zones. <br>
     * Useful when a widget is outside its panel.
     *
     * @param area widget
     */
    void addNEIExclusionArea(IWidget area);

    /**
     * Removes a widget exclusion area.
     *
     * @param area widget
     */
    void removeNEIExclusionArea(IWidget area);

    NEISettings DUMMY = new NEISettings() {
        @Override
        public void enableNEI() {}

        @Override
        public void disableNEI() {}

        @Override
        public void defaultNEI() {}

        @Override
        public boolean isNEIEnabled(ModularScreen screen) {
            return false;
        }

        @Override
        public void addNEIExclusionArea(Rectangle area) {}

        @Override
        public void removeNEIExclusionArea(Rectangle area) {}

        @Override
        public void addNEIExclusionArea(IWidget area) {}

        @Override
        public void removeNEIExclusionArea(IWidget area) {}
    };
}
