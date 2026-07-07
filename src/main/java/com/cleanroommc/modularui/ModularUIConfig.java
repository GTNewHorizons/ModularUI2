package com.cleanroommc.modularui;

import com.cleanroommc.modularui.screen.RichTooltip;

import com.gtnewhorizon.gtnhlib.config.Config;

@Config(modid = ModularUI.ID)
public class ModularUIConfig {

    @Config.LangKey("modularui2.config.default_scroll_speed")
    @Config.Comment("Amount of pixels scrolled")
    @Config.RangeInt(min = 1, max = 100)
    public static int defaultScrollSpeed = 30;

    @Config.LangKey("modularui2.config.smooth_progress_bar")
    @Config.Comment("If progress bar should step in texture pixels or screen pixels. (Screen pixels are way smaller and therefore smoother)")
    public static boolean smoothProgressBar = false;

    // Default direction
    @Config.LangKey("modularui2.config.tooltip_pos")
    @Config.Comment("Default tooltip position around the widget or its panel.")
    public static RichTooltip.Pos tooltipPos = RichTooltip.Pos.NEXT_TO_MOUSE;

    @Config.LangKey("modularui2.config.esc_restore_last_text")
    @Config.Comment("If true, pressing ESC key in the text field will restore the last text instead of confirming current one.")
    public static boolean escRestoreLastText = false;

    @Config.LangKey("modularui2.config.show_slot_overlay")
    @Config.Comment("If true, display the slot overlay when the slot is occupied.")
    public static boolean showSlotOverlay = true;

    @Config.LangKey("modularui2.config.gui_debug_mode")
    @Config.Comment("If true, widget outlines and widget information will be drawn.")
    public static boolean guiDebugMode = ModularUI.isDevEnv;

    @Config.LangKey("modularui2.config.use_dark_theme_by_default")
    @Config.Comment("If true and not specified otherwise, screens will try to use the 'vanilla_dark' theme.")
    public static boolean useDarkThemeByDefault = false;

    @Config.LangKey("modularui2.config.debug_text_color")
    @Config.Comment("Debug text color. Prefix Hex values with a #. Common colors can be referred by their name.")
    public static String debugTextColor = "#FFAAAAAA";

    @Config.LangKey("modularui2.config.debug_outline_color")
    @Config.Comment("Debug outline color. Prefix Hex values with a #. Common colors can be referred by their name.")
    public static String debugOutlineColor = "#DCB42873";

    @Config.LangKey("modularui2.config.enable_test_guis")
    @Config.RequiresMcRestart
    @Config.Comment("Enables a test block, test item with a test gui and opening a gui by right clicking a diamond.")
    public static boolean enableTestGuis = ModularUI.isDevEnv;

    @Config.LangKey("modularui2.config.enable_test_overlays")
    @Config.RequiresMcRestart
    @Config.Comment("Enables a test overlay shown on title screen and watermark shown on every GuiContainer.")
    public static boolean enableTestOverlays = false;

    //@Config.LangKey("modularui2.config.use_rich_tooltips")
    //public static boolean replaceVanillaTooltips = false;
}
