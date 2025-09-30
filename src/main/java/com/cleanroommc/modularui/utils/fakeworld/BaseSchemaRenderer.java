package com.cleanroommc.modularui.utils.fakeworld;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.Icon;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.GlStateManager;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.utils.Vector3f;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widgets.SchemaWidget;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;

import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BaseSchemaRenderer implements IDrawable {

    private static final Framebuffer FBO = new Framebuffer(1080, 1080, true);

    private final ISchema schema;
    private final IBlockAccess renderWorld;
    private final Framebuffer framebuffer;
    private final Camera camera = new Camera();
    private MovingObjectPosition lastRayTrace = null;

    public BaseSchemaRenderer(ISchema schema, Framebuffer framebuffer) {
        this.schema = schema;
        this.framebuffer = framebuffer;
        this.renderWorld = new RenderWorld(schema);
    }

    public BaseSchemaRenderer(ISchema schema) {
        this(schema, FBO);
    }

    @Nullable
    public MovingObjectPosition getLastRayTrace() {
        return lastRayTrace;
    }

    public ISchema getSchema() {
        return schema;
    }

    public Camera getCamera() {
        return camera;
    }

    @Override
    public SchemaWidget asWidget() {
        return new SchemaWidget(this);
    }

    @Override
    public Icon asIcon() {
        return IDrawable.super.asIcon().size(50);
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        render(x, y, width, height, context.getMouseX(), context.getMouseY());
    }

    protected void render(int x, int y, int width, int height, int mouseX, int mouseY) {
        // TODO: This isn't working. The world render does work, but the framebuffer doesn't. I wasn't able to draw anything to the
        // framebuffer, not even a simple rectangle. The same code works perfectly fine in 1.12. Raytracer also works.
        onSetupCamera();
        int lastFbo = bindFBO();
        setupCamera(this.framebuffer.framebufferWidth, this.framebuffer.framebufferHeight);
        renderWorld();
        if (doRayTrace()) {
            MovingObjectPosition result = null;
            if (Area.isInside(x, y, width, height, mouseX, mouseY)) {
                result = rayTrace(mouseX, mouseY, width, height);
            }
            if (result == null) {
                if (this.lastRayTrace != null) {
                    onRayTraceFailed();
                }
            } else {
                onSuccessfulRayTrace(result);
            }
            this.lastRayTrace = result;
        }
        onRendered();
        Platform.setupDrawTex();
        resetCamera();
        unbindFBO(lastFbo);
        this.framebuffer.checkFramebufferComplete();
        GlStateManager.viewport(0, 0, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);

        // bind FBO as texture
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        lastFbo = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        this.framebuffer.bindFramebufferTexture();
        GlStateManager.color(1, 1, 1, 1);

        // render rect with FBO texture
        drawFbo(x, y, width, height);
        GlStateManager.bindTexture(lastFbo);
    }

    private static void drawFbo(int x, int y, int width, int height) {
        Platform.startDrawing(Platform.DrawMode.QUADS, Platform.VertexFormat.POS_TEX, bufferBuilder -> {
            bufferBuilder.pos(x + width, y + height, 0).tex(1, 0).endVertex();
            bufferBuilder.pos(x + width, y, 0).tex(1, 1).endVertex();
            bufferBuilder.pos(x, y, 0).tex(0, 1).endVertex();
            bufferBuilder.pos(x, y + height, 0).tex(0, 0).endVertex();
        });
    }

    protected MovingObjectPosition rayTrace(int mouseX, int mouseY, int width, int height) {
        final float halfPI = (float) (Math.PI / 2);
        Vector3f cameraPos = camera.getPos();
        float yaw = camera.getYaw();
        float pitch = camera.getPitch();

        Vector3f mouseXShift = new Vector3f(1, 0, 0)
                .rotatePitch(pitch)
                .rotateYaw(-yaw + halfPI)
                .scale(mouseX - width / 2f)
                .scale(1 / 32f);
        Vector3f mouseYShift = new Vector3f(0, -1, 0)
                .rotatePitch(pitch)
                .rotateYaw(-yaw + halfPI)
                .scale(mouseY - height / 2f)
                .scale(1 / 32f);
        Vector3f mousePos = Vector3f.add(cameraPos, mouseXShift, mouseYShift, null);
        Vector3f focus = camera.getLookAt();
        float perspectiveCompensation = isIsometric() ? 1 : cameraPos.distanceTo(focus) / 3 * width / 100;
        Vector3f underMousePos = focus.add(mouseXShift.scale(perspectiveCompensation), null).add(mouseYShift.scale(perspectiveCompensation));
        Vector3f look = Vector3f.sub(underMousePos, mousePos, null).scale(10);
        Vector3f.add(mousePos, look, underMousePos);
        return schema.getWorld().rayTraceBlocks(mousePos.toVec3d(), underMousePos.toVec3d(), true);
    }

    private void renderWorld() {
        Minecraft mc = Minecraft.getMinecraft();
        Platform.setupDrawTex();
        GlStateManager.enableCull();
        GlStateManager.enableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        mc.entityRenderer.disableLightmap(0); // 1.7.10: arg unused
        mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        int oldPass = MinecraftForgeClient.getRenderPass();
        GlStateManager.disableLighting();
        Platform.setupDrawGradient(); // needed for ambient occlusion

        List<TileEntity> tesr = null;
        try { // render block in each layer
            Tessellator.instance.startDrawingQuads();
            Tessellator.instance.setTranslation(0, 0, 0);
            Tessellator.instance.setBrightness(15 << 20 | 15 << 4);
            for (int pass = 0; pass < 2; pass++) {
                if (pass == 0 && isTesrEnabled()) {
                    tesr = renderBlocksInLayer(mc, pass, true);
                } else {
                    renderBlocksInLayer(mc, pass, false);
                }
            }
        } finally {
            Tessellator.instance.draw();
            Tessellator.instance.setTranslation(0, 0, 0);
            ForgeHooksClient.setRenderPass(oldPass);
        }

        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableLighting();

        try { // render TESR
            if (tesr != null && !tesr.isEmpty()) {
                renderTesr(tesr, 0);
                if (!tesr.isEmpty()) { // any tesr that don't render in pass 1 or 2 are removed from the list
                    renderTesr(tesr, 1);
                    renderTesr(tesr, 2);
                }
            }
        } finally {
            ForgeHooksClient.setRenderPass(-1);
        }

        Platform.endDrawGradient();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
    }

    private List<TileEntity> renderBlocksInLayer(Minecraft mc, int pass, boolean collectTesr) {
        List<TileEntity> tesr = collectTesr ? new ArrayList<>() : null;
        ForgeHooksClient.setRenderPass(pass);
        //setDefaultPassRenderState(pass);
        RenderBlocks renderBlocks = new RenderBlocks(this.renderWorld);
        this.schema.forEach(pair -> {
            BlockPos pos = pair.getKey();
            Block block = pair.getValue().getBlock();
            //int meta = pair.getValue().getBlockMeta();
            if (block.isAir(this.renderWorld, pos.getX(), pos.getY(), pos.getZ())) return;
            if (collectTesr) {
                TileEntity te = pair.getValue().getTileEntity();
                if (te != null && !te.isInvalid()) {
                    if (te.xCoord != pos.x) te.xCoord = pos.x;
                    if (te.yCoord != pos.y) te.yCoord = pos.y;
                    if (te.zCoord != pos.z) te.zCoord = pos.z;
                    if (TileEntityRendererDispatcher.instance.getSpecialRendererByClass(te.getClass()) != null) {
                        // only collect tiles to render which actually have a tesr
                        tesr.add(te);
                    }
                }
            }
            if (block.canRenderInPass(pass)) {
                renderBlocks.blockAccess = this.renderWorld;
                renderBlocks.setRenderBounds(0, 0, 0, 1, 1, 1);
                renderBlocks.renderAllFaces = true;
                renderBlocks.renderBlockByRenderType(block, pos.x, pos.y, pos.z);
            }
        });
        return tesr;
    }

    private static void renderTesr(List<TileEntity> tileEntities, int pass) {
        ForgeHooksClient.setRenderPass(pass);
        GlStateManager.color(1, 1, 1, 1);
        setDefaultPassRenderState(pass);
        for (Iterator<TileEntity> iterator = tileEntities.iterator(); iterator.hasNext(); ) {
            TileEntity tile = iterator.next();
            if (tile == null || tile.isInvalid()) continue;
            if (pass == 0 && (!tile.shouldRenderInPass(1) || !tile.shouldRenderInPass(2))) {
                // remove tiles that don't render in further passes
                iterator.remove();
            }
            if (tile.shouldRenderInPass(pass)) {
                TileEntityRendererDispatcher.instance.renderTileEntityAt(tile, tile.xCoord, tile.yCoord, tile.zCoord, 0);
            }
        }
    }

    private static void setDefaultPassRenderState(int pass) {
        GlStateManager.color(1, 1, 1, 1);
        if (pass == 0) { // SOLID
            GlStateManager.enableDepth();
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
        } else { // TRANSLUCENT
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.depthMask(false);
        }
    }

    protected final void setupCamera(int width, int height) {

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushClientAttrib(GL11.GL_ALL_CLIENT_ATTRIB_BITS);
        Minecraft.getMinecraft().entityRenderer.disableLightmap(0); // 1.7.10: arg unused
        GlStateManager.disableLighting();
        GlStateManager.enableDepth();
        GlStateManager.enableBlend();

        // setup viewport and clear GL buffers
        GlStateManager.viewport(0, 0, width, height);
        Color.setGlColor(getClearColor());
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        // setup projection matrix to perspective
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();

        /*float near = isIsometric() ? 1f : 0.1f;
        float far = 10000.0f;
        float fovY = 60.0f; // Field of view in the Y direction
        float aspect = (float) width / height; // width and height are the dimensions of your window
        float top = near * (float) Math.tan(Math.toRadians(fovY) / 2.0);
        float bottom = -top;
        float left = aspect * bottom;
        float right = aspect * top;*/
        /*if (isIsometric()) {
            GL11.glOrtho(left, right, bottom, top, near, far);
        } else {
            GL11.glFrustum(left, right, bottom, top, near, far);
        }*/
        float aspectRatio = width / (height * 1.0f);
        GLU.gluPerspective(60f, aspectRatio, 0.1f, 10000f);

        // setup modelview matrix
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        if (isIsometric()) {
            GlStateManager.scale(0.1, 0.1, 0.1);
        }
        var c = this.camera.getPos();
        var lookAt = this.camera.getLookAt();
        GLU.gluLookAt(c.x, c.y, c.z, lookAt.x, lookAt.y, lookAt.z, 0, 1, 0);
    }

    protected final void resetCamera() {
        // reset viewport
        Minecraft minecraft = Minecraft.getMinecraft();
        GlStateManager.viewport(0, 0, minecraft.displayWidth, minecraft.displayHeight);
        // reset projection matrix
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.popMatrix();
        // reset modelview matrix
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.popMatrix();
        GlStateManager.disableBlend();
        GlStateManager.disableDepth();
        // reset attributes
        GL11.glPopClientAttrib();
        GL11.glPopAttrib();
    }

    private int bindFBO() {
        int lastID = GL11.glGetInteger(EXTFramebufferObject.GL_FRAMEBUFFER_BINDING_EXT);
        this.framebuffer.setFramebufferColor(1F, 1F, 1F, 0.0F);
        //this.framebuffer.framebufferClear();
        this.framebuffer.bindFramebuffer(true);
        GlStateManager.pushMatrix();
        return lastID;
    }

    private void unbindFBO(int lastID) {
        GlStateManager.popMatrix();
        //this.framebuffer.unbindFramebufferTexture();
        // func_153171_g = glBindFramebuffer
        // field_153198_e = GL_FRAMEBUFFER = 36160
        OpenGlHelper.func_153171_g(OpenGlHelper.field_153198_e, lastID);
        GL11.glViewport(0, 0, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
    }

    @ApiStatus.OverrideOnly
    protected void onSetupCamera() {}

    @ApiStatus.OverrideOnly
    protected void onRendered() {}

    @ApiStatus.OverrideOnly
    protected void onSuccessfulRayTrace(@NotNull MovingObjectPosition result) {}

    @ApiStatus.OverrideOnly
    protected void onRayTraceFailed() {}

    public boolean doRayTrace() {
        return false;
    }

    public int getClearColor() {
        return Color.withAlpha(Color.RED.brighter(1), 1f);
    }

    public boolean isIsometric() {
        return false;
    }

    public boolean isTesrEnabled() {
        return true;
    }
}



