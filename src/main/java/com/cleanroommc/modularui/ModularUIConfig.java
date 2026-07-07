package com.cleanroommc.modularui;

import com.cleanroommc.modularui.screen.RichTooltip;

import com.gtnewhorizon.gtnhlib.config.Config;

@Config(modid = ModularUI.ID)
public class ModularUIConfig {

    @Config.LangKey("modularui2.config.default_scroll_speed")
    @Config.RangeInt(min = 1, max = 100)
    public static int defaultScrollSpeed = 30;

    @Config.LangKey("modularui2.config.smooth_progress_bar")
    public static boolean smoothProgressBar = false;

    // Default direction
    @Config.LangKey("modularui2.config.tooltip_pos")
    public static RichTooltip.Pos tooltipPos = RichTooltip.Pos.NEXT_TO_MOUSE;

    @Config.LangKey("modularui2.config.esc_restore_last_text")
    public static boolean escRestoreLastText = false;

    @Config.LangKey("modularui2.config.show_slot_overlay")
    public static boolean showSlotOverlay = true;

    @Config.LangKey("modularui2.config.gui_debug_mode")
    public static boolean guiDebugMode = ModularUI.isDevEnv;

    @Config.LangKey("modularui2.config.use_dark_theme_by_default")
    public static boolean useDarkThemeByDefault = false;

    @Config.LangKey("modularui2.config.debug_text_color")
    public static String debugTextColor = "#FFAAAAAA";

    @Config.LangKey("modularui2.config.debug_outline_color")
    public static String debugOutlineColor = "#DCB42873";

    @Config.LangKey("modularui2.config.enable_test_guis")
    @Config.RequiresMcRestart
    public static boolean enableTestGuis = ModularUI.isDevEnv;

    @Config.LangKey("modularui2.config.enable_test_overlays")
    @Config.RequiresMcRestart
    public static boolean enableTestOverlays = false;

    //@Config.LangKey("modularui2.config.use_rich_tooltips")
    //public static boolean replaceVanillaTooltips = false;
}
