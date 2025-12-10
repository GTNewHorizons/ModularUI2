package com.cleanroommc.modularui.utils.fakeworld;

import com.cleanroommc.modularui.drawable.BufferBuilder;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.GlStateManager;
import com.cleanroommc.modularui.utils.Platform;

import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class BlockHighlight {

    // this is magic
    protected static final float[][] vertices = new float[6][12];

    static {
        int[][] intVertices = {
                {1, 0, 0, 1, 0, 1, 0, 0, 1, 0, 0, 0},
                {0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0},
                {0, 1, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0},
                {0, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1},
                {0, 0, 0, 0, 0, 1, 0, 1, 1, 0, 1, 0},
                {1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 0, 0}
        };
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 12; j++) {
                int v = intVertices[i][j];
                vertices[i][j] = v == 1 ? 1.005f : -0.005f;
            }
        }
    }

    private int color;
    private boolean allSides;
    private float frameThickness;

    public BlockHighlight(int color) {
        this(color, true, 0.0F);
    }

    public BlockHighlight(int color, float frameThickness) {
        this(color, true, frameThickness);
    }

    public BlockHighlight(int color, boolean allSides) {
        this(color, allSides, 0.0F);
    }

    public BlockHighlight(int color, boolean allSides, float frameThickness) {
        this.color = color;
        this.allSides = allSides;
        this.frameThickness = frameThickness;
    }

    public final void renderHighlight(MovingObjectPosition result, Vector3f camera) {
        if (result != null && result.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            renderHighlight(result.blockX, result.blockY, result.blockZ, ForgeDirection.getOrientation(result.sideHit), camera);
        }
    }

    public final void renderHighlight(int x, int y, int z, ForgeDirection side, Vector3f camera) {
        Platform.setupDrawColor();
        GlStateManager.disableLighting();
        Color.setGlColor(this.color);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        float dist = camera.distanceSquared(x + 0.5f, y + 0.5f, z + 0.5f);
        doRender(side, dist);
        GlStateManager.popMatrix();
    }

    protected void doRender(ForgeDirection side, float sqDistance) {
        if (this.allSides) side = null;
        if (this.frameThickness >= 0) {
            // scale thickness with distance to block
            float d = (float) (this.frameThickness * (1 + Math.max(0, Math.sqrt(sqDistance) - 3) / 5));
            renderFrame(side, d);
        } else {
            renderSolid(side);
        }
    }

    public float getFrameThickness() {
        return frameThickness;
    }

    public int getColor() {
        return color;
    }

    public boolean isAllSides() {
        return allSides;
    }

    public void setFrameThickness(float frameThickness) {
        this.frameThickness = frameThickness;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setAllSides(boolean allSides) {
        this.allSides = allSides;
    }

    protected static void renderSolid(@Nullable ForgeDirection side) {
        Platform.startDrawing(Platform.DrawMode.QUADS, Platform.VertexFormat.POS, builder -> {
            if (side == null) {
                for (int i = 0; i < 6; i++) {
                    buildFace(builder, ForgeDirection.getOrientation(i));
                }
            } else {
                buildFace(builder, side);
            }
        });
    }

    protected static void renderFrame(@Nullable ForgeDirection side, float d) {
        if (side == null) {
            for (int i = 0; i < 6; i++) {
                buildFrameFace(ForgeDirection.getOrientation(i), d);
            }
        } else {
            buildFrameFace(side, d);
        }
    }

    protected static void buildFrameFace(ForgeDirection facing, float d) {
        float[] vert = vertices[facing.ordinal()];
        // draw faces individually do avoid weird lines between faces
        Platform.startDrawing(Platform.DrawMode.TRIANGLE_STRIP, Platform.VertexFormat.POS, builder -> {
            buildVertex(builder, vert, 9);
            buildInnerVertex(builder, vert, 9, facing, d);
            buildVertex(builder, vert, 6);
            buildInnerVertex(builder, vert, 6, facing, d);
            buildVertex(builder, vert, 3);
            buildInnerVertex(builder, vert, 3, facing, d);
            buildVertex(builder, vert, 0);
            buildInnerVertex(builder, vert, 0, facing, d);
            buildVertex(builder, vert, 9);
            buildInnerVertex(builder, vert, 9, facing, d);
        });
    }

    protected static void buildFace(BufferBuilder builder, ForgeDirection facing) {
        float[] vert = vertices[facing.ordinal()];
        buildVertex(builder, vert, 0);
        buildVertex(builder, vert, 3);
        buildVertex(builder, vert, 6);
        buildVertex(builder, vert, 9);
    }

    protected static void buildVertex(BufferBuilder builder, float[] vertices, int i) {
        float x = vertices[i];
        float y = vertices[i + 1];
        float z = vertices[i + 2];
        builder.pos(x, y, z).endVertex();
    }

    private static void buildInnerVertex(BufferBuilder builder, float[] vertices, int i, ForgeDirection side, float d) {
        float x = vertices[i];
        float y = vertices[i + 1];
        float z = vertices[i + 2];
        if (side != ForgeDirection.EAST && side != ForgeDirection.WEST) {
            if (x >= 1) x -= d;
            else x += d;
        }
        if (side != ForgeDirection.DOWN && side != ForgeDirection.UP) {
            if (y >= 1) y -= d;
            else y += d;
        }
        if (side != ForgeDirection.NORTH && side != ForgeDirection.SOUTH) {
            if (z >= 1) z -= d;
            else z += d;
        }
        builder.pos(x, y, z).endVertex();
    }
}
