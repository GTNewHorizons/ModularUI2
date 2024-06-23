package com.cleanroommc.modularui.utils.fakeworld;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widget.sizer.Area;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.tileentity.TileEntity;
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;

import org.joml.Vector3d;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector3f;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

import static com.cleanroommc.modularui.drawable.BufferBuilder.bufferbuilder;

public class SchemaRenderer implements IDrawable {

    private static final Framebuffer FBO = new Framebuffer(1080, 1080, true);

    private final ISchema schema;
    private final IBlockAccess renderWorld;
    private final Framebuffer framebuffer;
    private final Camera camera = new Camera(new Vector3f(), new Vector3f());
    private boolean cameraSetup = false;
    private DoubleSupplier scale;
    private BooleanSupplier disableTESR;
    private Consumer<IRayTracer> onRayTrace;
    private Consumer<Projection> afterRender;
    private BiConsumer<Camera, ISchema> cameraFunc;
    private int clearColor = 0;
    private boolean isometric = false;

    public SchemaRenderer(ISchema schema, Framebuffer framebuffer) {
        this.schema = schema;
        this.framebuffer = framebuffer;
        this.renderWorld = new RenderWorld(schema);
    }

    public SchemaRenderer(ISchema schema) {
        this(schema, FBO);
    }

    public SchemaRenderer cameraFunc(BiConsumer<Camera, ISchema> camera) {
        this.cameraFunc = camera;
        return this;
    }

    public SchemaRenderer onRayTrace(Consumer<IRayTracer> consumer) {
        this.onRayTrace = consumer;
        return this;
    }

    public SchemaRenderer afterRender(Consumer<Projection> consumer) {
        this.afterRender = consumer;
        return this;
    }

    public SchemaRenderer isometric(boolean isometric) {
        this.isometric = isometric;
        return this;
    }

    public SchemaRenderer scale(double scale) {
        return scale(() -> scale);
    }

    public SchemaRenderer scale(DoubleSupplier scale) {
        this.scale = scale;
        return this;
    }

    public SchemaRenderer disableTESR(boolean disable) {
        return disableTESR(() -> disable);
    }

    public SchemaRenderer disableTESR(BooleanSupplier disable) {
        this.disableTESR = disable;
        return this;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        render(x, y, width, height, context.getMouseX(), context.getMouseY());
    }

    public void render(int x, int y, int width, int height, int mouseX, int mouseY) {
        if (this.cameraFunc != null) {
            this.cameraFunc.accept(this.camera, this.schema);
        }
        if (Objects.nonNull(scale)) {
            Vector3f cameraPos = camera.getPos();
            Vector3f looking = camera.getLookAt();
            Vector3f.sub(cameraPos, looking, cameraPos);
            if (cameraPos.length() != 0.0f) cameraPos.normalise();
            cameraPos.scale((float) scale.getAsDouble());
            Vector3f.add(looking, cameraPos, cameraPos);
        }
        int lastFbo = bindFBO();
        setupCamera(this.framebuffer.framebufferWidth, this.framebuffer.framebufferHeight);
        renderWorld();
        if (this.onRayTrace != null && Area.isInside(x, y, width, height, mouseX, mouseY)) {
            this.onRayTrace.accept(new IRayTracer() {
                @Override
                public MovingObjectPosition rayTrace(int screenX, int screenY) {
                    return SchemaRenderer.this.rayTrace(Projection.INSTANCE.unProject(screenX, screenY));
                }

                @Override
                public MovingObjectPosition rayTraceMousePos() {
                    return rayTrace(mouseX, mouseY);
                }
            });
        }
        resetCamera();
        unbindFBO(lastFbo);

        // bind FBO as texture
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        lastFbo = GL11.glGetInteger(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.framebuffer.framebufferTexture);
        GL11.glColor4f(1, 1, 1, 1);

        // render rect with FBO texture
        Tessellator.instance.startDrawingQuads();

        bufferbuilder.pos(x + width, y + height, 0).tex(1, 0).endVertex();
        bufferbuilder.pos(x + width, y, 0).tex(1, 1).endVertex();
        bufferbuilder.pos(x, y, 0).tex(0, 1).endVertex();
        bufferbuilder.pos(x, y + height, 0).tex(0, 0).endVertex();
        Tessellator.instance.draw();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, lastFbo);
    }

    private void renderWorld() {
//        Minecraft mc = Minecraft.getMinecraft();
//        GL11.glEnable(GL11.GL_CULL_FACE);
//        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
//        RenderHelper.disableStandardItemLighting();
//        mc.entityRenderer.disableLightmap(0);
//        mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
//        BlockRenderLayer oldRenderLayer = MinecraftForgeClient.getRenderLayer();
//        GL11.glDisable(GL11.GL_LIGHTING);
//        GL11.glEnable(GL11.GL_TEXTURE_2D);
//        GL11.glEnable(GL11.GL_ALPHA_TEST);
//
//        try { // render block in each layer
//            for (BlockRenderLayer layer : BlockRenderLayer.values()) {
//                ForgeHooksClient.setRenderLayer(layer);
//                int pass = layer == BlockRenderLayer.TRANSLUCENT ? 1 : 0;
//                setDefaultPassRenderState(pass);
//                BufferBuilder buffer = Tessellator.getInstance().getBuffer();
//                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
//                BlockRendererDispatcher blockrendererdispatcher = mc.getBlockRendererDispatcher();
//                this.schema.forEach(pair -> {
//                    BlockPos pos = pair.getKey();
//                    IBlockState state = pair.getValue().getBlockState();
//                    if (!state.getBlock().isAir(state, this.renderWorld, pos) && state.getBlock().canRenderInLayer(state, layer)) {
//                        blockrendererdispatcher.renderBlock(state, pos, this.renderWorld, buffer);
//                    }
//                });
//                Tessellator.getInstance().draw();
//                Tessellator.getInstance().getBuffer().setTranslation(0, 0, 0);
//            }
//        } finally {
//            ForgeHooksClient.setRenderLayer(oldRenderLayer);
//        }
//
//        RenderHelper.enableStandardItemLighting();
//        GL11.glEnable(GL11.GL_LIGHTING);
//
//        // render TESR
//        if (disableTESR == null || !disableTESR.getAsBoolean()) {
//            for (int pass = 0; pass < 2; pass++) {
//                ForgeHooksClient.setRenderPass(pass);
//                int finalPass = pass;
//                GL11.glColor4f(1, 1, 1, 1);
//                setDefaultPassRenderState(pass);
//                this.schema.forEach(pair -> {
//                    BlockPos pos = pair.getKey();
//                    TileEntity tile = pair.getValue().getTileEntity();
//                    if (tile != null && tile.shouldRenderInPass(finalPass)) {
//                        TileEntityRendererDispatcher.instance.renderTileEntityAt(tile, pos.getX(), pos.getY(), pos.getZ(), 0);
//                    }
//                });
//            }
//        }
//        ForgeHooksClient.setRenderPass(-1);
//        GL11.glEnable(GL11.GL_DEPTH_TEST);
//        GL11.glDisable(GL11.GL_BLEND);
//        GL11.glDepthMask(true);
//        if (this.afterRender != null) {
//            this.afterRender.accept(Projection.INSTANCE);
//        }
    }

    private static void setDefaultPassRenderState(int pass) {
        GL11.glColor4f(1, 1, 1, 1);
        if (pass == 0) { // SOLID
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDepthMask(true);
        } else { // TRANSLUCENT
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            GL11.glDepthMask(false);
        }
    }

    protected void setupCamera(int width, int height) {
        //GlStateManager.pushAttrib();

        Minecraft.getMinecraft().entityRenderer.disableLightmap(0);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);

        // setup viewport and clear GL buffers
        GL11.glViewport(0, 0, width, height);
        Color.setGlColor(clearColor);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        // setup projection matrix to perspective
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();

        float near = this.isometric ? 1f : 0.1f;
        float far = 10000.0f;
        float fovY = 60.0f; // Field of view in the Y direction
        float aspect = (float) width / height; // width and height are the dimensions of your window
        float top = near * (float) Math.tan(Math.toRadians(fovY) / 2.0);
        float bottom = -top;
        float left = aspect * bottom;
        float right = aspect * top;
        if (this.isometric) {
            GL11.glOrtho(left, right, bottom, top, near, far);
        } else {
            GL11.glFrustum(left, right, bottom, top, near, far);
        }

        // setup modelview matrix
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        if (this.isometric) {
            GL11.glScaled(0.1, 0.1, 0.1);
        }
        var c = this.camera.getPos();
        var lookAt = this.camera.getLookAt();
        GLU.gluLookAt(c.x, c.y, c.z, lookAt.x, lookAt.y, lookAt.z, 0, 1, 0);
        this.cameraSetup = true;
    }

    protected void resetCamera() {
        this.cameraSetup = false;
        // reset viewport
        Minecraft minecraft = Minecraft.getMinecraft();
        GL11.glViewport(0, 0, minecraft.displayWidth, minecraft.displayHeight);

        // reset projection matrix
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();

        // reset modelview matrix
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        // reset attributes
        // GlStateManager.popAttrib();
    }

    private int bindFBO() {
        int lastID = GL11.glGetInteger(EXTFramebufferObject.GL_FRAMEBUFFER_BINDING_EXT);
        this.framebuffer.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
        this.framebuffer.framebufferClear();
        this.framebuffer.bindFramebuffer(true);
        GL11.glPushMatrix();
        return lastID;
    }

    private void unbindFBO(int lastID) {
        GL11.glPopMatrix();
        this.framebuffer.unbindFramebufferTexture();
        OpenGlHelper.func_153171_g(OpenGlHelper.field_153198_e, lastID); // glBindFramebuffer GL_FRAMEBUFFER
    }

    private MovingObjectPosition rayTrace(Vector3f hitPos) {
        Vec3 startPos = Vec3.createVectorHelper(this.camera.getPos().x, this.camera.getPos().y, this.camera.getPos().z);
        hitPos.scale(2); // Double view range to ensure pos can be seen.
        Vec3 endPos = Vec3.createVectorHelper((hitPos.x - startPos.xCoord), (hitPos.y - startPos.yCoord), (hitPos.z - startPos.zCoord));
        return this.schema.getWorld().rayTraceBlocks(startPos, endPos);
    }

    public boolean isCameraSetup() {
        return cameraSetup;
    }

    public interface IRayTracer {

        MovingObjectPosition rayTrace(int screenX, int screenY);

        MovingObjectPosition rayTraceMousePos();
    }

    public interface ICamera {

        void setupCamera(Vector3f cameraPos, Vector3f lookAt);
    }
}
