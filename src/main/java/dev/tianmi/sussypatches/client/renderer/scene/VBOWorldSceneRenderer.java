package dev.tianmi.sussypatches.client.renderer.scene;

import java.nio.ByteBuffer;
import java.util.Collection;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import dev.tianmi.sussypatches.api.core.mixin.extension.WSRExtension;
import dev.tianmi.sussypatches.api.util.OpenGL3Helper;
import dev.tianmi.sussypatches.api.util.RenderPass;
import gregtech.client.renderer.scene.ISceneRenderHook;
import gregtech.client.renderer.scene.ImmediateWorldSceneRenderer;
import gregtech.client.renderer.scene.WorldSceneRenderer;

@SideOnly(Side.CLIENT)
public class VBOWorldSceneRenderer extends ImmediateWorldSceneRenderer {

    protected final int[] vaos;
    protected final VertexBuffer[] vbos;
    protected boolean isDirty = true;

    public VBOWorldSceneRenderer(World world) {
        super(world);
        int layers = BlockRenderLayer.values().length;
        this.vaos = new int[layers];
        this.vbos = new VertexBuffer[layers];
    }

    private void uploadVBO() {
        Minecraft mc = Minecraft.getMinecraft();
        BlockRenderLayer oldRenderLayer = MinecraftForgeClient.getRenderLayer();

        try { // render block in each layer
            for (BlockRenderLayer layer : BlockRenderLayer.values()) {

                int index = layer.ordinal();
                this.vaos[index] = OpenGL3Helper.isGl3Loaded() ? OpenGL3Helper.genVertexArrays() : -1;
                this.vbos[index] = new VertexBuffer(DefaultVertexFormats.BLOCK);

                ForgeHooksClient.setRenderLayer(layer);
                int pass = layer == BlockRenderLayer.TRANSLUCENT ? 1 : 0;
                setDefaultPassRenderState(pass);

                BufferBuilder buffer = Tessellator.getInstance().getBuffer();
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
                BlockRendererDispatcher brd = mc.getBlockRendererDispatcher();

                var renderedBlocks = WSRExtension.cast(this).sus$getRenderedBlocks();

                for (BlockPos pos : renderedBlocks) {
                    IBlockState state = world.getBlockState(pos);
                    Block block = state.getBlock();
                    if (block == Blocks.AIR) continue;
                    state = state.getActualState(world, pos);
                    if (block.canRenderInLayer(state, layer)) {
                        brd.renderBlock(state, pos, world, buffer);
                    }
                }
                buffer.finishDrawing();
                buffer.reset();

                int vao = this.vaos[index];
                var vbo = this.vbos[index];

                ByteBuffer data = buffer.getByteBuffer();
                vbo.bufferData(data);
                if (OpenGL3Helper.isGl3Loaded()) {
                    OpenGL3Helper.bindVertexArray(vao);
                    vbo.bindBuffer();
                    setupClientStates();
                    setupArrayPointers();
                    OpenGL3Helper.bindVertexArray(0);
                    vbo.unbindBuffer();
                }
            }
        } finally {
            ForgeHooksClient.setRenderLayer(oldRenderLayer);
        }
        this.isDirty = false;
    }

    @Override
    protected void drawWorld() {
        if (this.isDirty) {
            uploadVBO();
        }

        Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.enableCull();
        GlStateManager.enableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        mc.entityRenderer.disableLightmap();
        mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.disableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();

        var oldRenderLayer = MinecraftForgeClient.getRenderLayer();
        for (var layer : BlockRenderLayer.values()) {

            ForgeHooksClient.setRenderLayer(layer);

            var pass = layer == BlockRenderLayer.TRANSLUCENT ? RenderPass.TRANSLUCENT : RenderPass.NORMAL;
            setPassRenderState(pass);

            GlStateManager.pushMatrix();
            {
                int vao = this.vaos[layer.ordinal()];
                var vbo = this.vbos[layer.ordinal()];
                vbo.bindBuffer();
                if (OpenGL3Helper.isGl3Loaded()) {
                    OpenGL3Helper.bindVertexArray(vao);
                    vbo.drawArrays(GL11.GL_QUADS);
                    OpenGL3Helper.bindVertexArray(0);
                } else {
                    setupClientStates();
                    setupArrayPointers();
                    vbo.drawArrays(GL11.GL_QUADS);
                }
                vbo.unbindBuffer();
            }
            GlStateManager.popMatrix();
        }
        ForgeHooksClient.setRenderLayer(oldRenderLayer);

        renderTESR(); // Handles TileEntities

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
    }

    @Override
    public WorldSceneRenderer addRenderedBlocks(Collection<BlockPos> blocks, ISceneRenderHook _null) {
        this.isDirty = true;
        return super.addRenderedBlocks(blocks, _null);
    }

    protected void setupClientStates() {
        GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glEnableClientState(GL11.GL_COLOR_ARRAY);
    }

    protected void setupArrayPointers() {
        // 28 == DefaultVertexFormats.BLOCK.getSize();
        GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, 28, 0);
        GlStateManager.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 28, 12);
        GlStateManager.glTexCoordPointer(2, GL11.GL_FLOAT, 28, 16);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glTexCoordPointer(2, GL11.GL_SHORT, 28, 24);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    protected void renderTESR() {
        RenderHelper.enableStandardItemLighting();
        for (var pass : RenderPass.values()) {
            ForgeHooksClient.setRenderPass(pass.ordinal());
            setPassRenderState(pass);

            var renderedBlocks = WSRExtension.cast(this).sus$getRenderedBlocks();
            for (BlockPos pos : renderedBlocks) {
                TileEntity tile = world.getTileEntity(pos);
                if (tile != null) {
                    if (tile.shouldRenderInPass(pass.ordinal())) {
                        TileEntityRendererDispatcher.instance.render(tile, pos.getX(), pos.getY(), pos.getZ(), 0);
                    }
                }
            }
        }
        ForgeHooksClient.setRenderPass(-1);
        RenderHelper.disableStandardItemLighting();
    }

    public static void setPassRenderState(RenderPass pass) {
        GlStateManager.color(1, 1, 1, 1);
        if (pass.isTranslucent()) { // TRANSLUCENT
            GlStateManager.disableBlend();
            GlStateManager.enableCull();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO);
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
            GlStateManager.enableBlend();
            GlStateManager.depthMask(false);
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
        } else { // NORMAL
            GlStateManager.enableDepth();
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
            GlStateManager.shadeModel(GL11.GL_FLAT);
        }
    }
}
