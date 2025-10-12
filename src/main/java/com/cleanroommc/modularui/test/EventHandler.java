package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IIcon;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.ClientGUI;
import com.cleanroommc.modularui.screen.RichTooltipEvent;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.ReloadThemeEvent;
import com.cleanroommc.modularui.theme.SelectableTheme;
import com.cleanroommc.modularui.theme.ThemeBuilder;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Color;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class EventHandler {

    public static boolean enabledRichTooltipEventTest = false;
    public static final String TEST_THEME = "mui:test_theme";
    private static final ThemeBuilder<?> testTheme = new ThemeBuilder<>(TEST_THEME)
            .defaultColor(Color.BLUE_ACCENT.brighter(0))
            .widgetTheme(IThemeApi.TOGGLE_BUTTON, new SelectableTheme.Builder<>()
                    .color(Color.BLUE_ACCENT.brighter(0))
                    .selectedColor(Color.WHITE.main)
                    .selectedIconColor(Color.RED.brighter(0)))
            .widgetThemeHover(IThemeApi.TOGGLE_BUTTON, new SelectableTheme.Builder<>()
                    .selectedIconColor(Color.DEEP_PURPLE.brighter(0)))
            .textColor(IThemeApi.TEXT_FIELD, Color.DEEP_PURPLE.main);

    private static final IIcon tooltipLine = new IDrawable() {
        @Override
        public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
            int high = Color.PURPLE.main;
            int low = Color.withAlpha(high, 0.05f);
            GuiDraw.drawHorizontalGradientRect(x, y + 1, width / 2f, 1, low, high);
            GuiDraw.drawHorizontalGradientRect(x + width / 2f, y + 1, width / 2f, 1, high, low);
        }
    }.asIcon().height(3);

    @SubscribeEvent
    public void onItemUse(PlayerInteractEvent event) {
        if (event.entityPlayer.getEntityWorld().isRemote && event.entityPlayer.getHeldItem() != null) {
            ItemStack itemStack = event.entityPlayer.getHeldItem();
            if (itemStack.getItem() == Items.diamond) {
                ClientGUI.open(new TestGuis());
            } else if (itemStack.getItem() == Items.emerald) {
                ClientGUI.open(new GLTestGui());
            }
            //GuiManager.openClientUI(Platform.getClientPlayer(), new TestGui());
            /*HoloUI.builder()
                    .inFrontOf(Platform.getClientPlayer(), 5, false)
                    .screenScale(0.5f)
                    .open(new TestGui());*/
            //ClientGUI.open(new ResizerTest());
        }
    }

    @SubscribeEvent
    public void onRichTooltip(RichTooltipEvent.Pre event) {
        if (enabledRichTooltipEventTest) {
            event.getTooltip()
                    .add(IKey.str("Powered By: ").style(IKey.GOLD, IKey.ITALIC))
                    .add(GuiTextures.MUI_LOGO.asIcon().size(18)).newLine()
                    .moveCursorToStart()
                    .moveCursorToNextLine()
                    .addLine(tooltipLine)
                    // replaces the Minecraft mod name in JEI item tooltips
                    .replace("Minecraft", key -> IKey.str("Chicken Jockey").style(IKey.BLUE, IKey.ITALIC))
                    .moveCursorToEnd();
        }
    }

    @SubscribeEvent
    public void onThemeTooltip(ReloadThemeEvent.Pre event) {
        IThemeApi.get().registerTheme(testTheme);
    }
}
