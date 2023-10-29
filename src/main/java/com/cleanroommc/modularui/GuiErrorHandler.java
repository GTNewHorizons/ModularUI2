package com.cleanroommc.modularui;

import com.cleanroommc.modularui.api.widget.IGuiElement;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class GuiErrorHandler {

    public static final GuiErrorHandler INSTANCE = new GuiErrorHandler();

    private final Set<GuiError> errorSet = new HashSet<>();
    private final List<GuiError> errors = new ArrayList<>();

    private GuiErrorHandler() {
    }

    public void clear() {
        this.errors.clear();
    }

    void pushError(IGuiElement reference, GuiError.Type type, String msg) {
        GuiError error = new GuiError(msg, reference, type);
        if (this.errorSet.add(error)) {
            ModularUI.LOGGER.error(msg);
            this.errors.add(error);
        }
    }

    public List<GuiError> getErrors() {
        return this.errors;
    }

    public void drawErrors(int x, int y) {
    }
}
