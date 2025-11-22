package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.sizer.Area;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.opengl.GL11;

import java.util.function.Supplier;

public class EntityDisplayWidget extends Widget<EntityDisplayWidget>{
    private final Supplier<EntityLivingBase> entitySupplier;
    private  boolean lookAtMouse = false;
    private  float mouseFollowStrength = 1f;

    public EntityDisplayWidget(Supplier<EntityLivingBase> e) {
        this.entitySupplier = e;
        this.size(18, 18);
    }

    public EntityDisplayWidget doesLookAtMouse(boolean doesLookAtMouse) {
        this.lookAtMouse = doesLookAtMouse;
        return this;
    }

    public EntityDisplayWidget mouseFollowStrength(float strength) {
        this.mouseFollowStrength = strength;
        return this;
    }

    public static void drawEntity(int x, int y, int scale, EntityLivingBase e, boolean lookAtMouse, float mouseX,
                                  float mouseY, float mouseFollowStrength) {
        if (e == null) return;
        GL11.glColor4f(1, 1, 1, 1);
        Platform.setupDrawItem();
        float yawX = 0;
        float pitchY = 0;
        if (lookAtMouse) {
            yawX = -mouseX;
            pitchY -= mouseY;
            yawX /= scale;
            pitchY /= scale;
            yawX *= mouseFollowStrength;
            pitchY *= mouseFollowStrength;
        }
        GuiInventory.func_147046_a(x, y, scale, yawX, pitchY, e);
        Platform.endDrawItem();
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        EntityLivingBase e = entitySupplier.get();
        Area area = this.getArea();
        drawEntity(
                +area.width / 2,
                area.height,
                area.width,
                e,
                this.lookAtMouse,
                context.getMouseX(),
                context.getMouseY(),
                mouseFollowStrength);
    }

}
