package com.cleanroommc.modularui.widgets.textfield;

import com.cleanroommc.modularui.utils.ParseResult;

public interface INumberParser {

    ParseResult parse(String expr, double defaultValue);
}
