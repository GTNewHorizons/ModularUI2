package com.cleanroommc.modularui.drawable.text;

import com.cleanroommc.modularui.api.drawable.ITextLine;
import com.cleanroommc.modularui.screen.viewport.GuiContext;

import java.util.List;

/**
 * Text renderer used for tooltips. Kept separate so tooltip text can be targeted on its own.
 */
public class TooltipTextRenderer extends TextRenderer {

    public static final TooltipTextRenderer INSTANCE = new TooltipTextRenderer();

    @Override
    public void drawCompiled(GuiContext context, List<ITextLine> lines) {
        super.drawCompiled(context, lines);
    }
}
