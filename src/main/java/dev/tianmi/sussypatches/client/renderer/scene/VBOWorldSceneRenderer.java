package dev.tianmi.sussypatches.client.renderer.scene;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.optifine.shaders.ShadersRender;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import dev.tianmi.sussypatches.api.core.mixin.extension.WSRExtension;
import dev.tianmi.sussypatches.api.util.OptiFineHelper;
import dev.tianmi.sussypatches.api.util.RenderPass;
import dev.tianmi.sussypatches.api.util.SusMods;
import dev.tianmi.sussypatches.client.renderer.buffer.VertexArrayObject;
import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.Mods;
import gregtech.client.renderer.scene.ISceneRenderHook;
import gregtech.client.renderer.scene.ImmediateWorldSceneRenderer;
import gregtech.client.renderer.scene.WorldSceneRenderer;

@SideOnly(Side.CLIENT)
public class VBOWorldSceneRenderer extends ImmediateWorldSceneRenderer {

    protected static final VertexArrayObject[] VAOS = new VertexArrayObject[BlockRenderLayer.values().length];
    protected static final VertexBuffer[] VBOS = new VertexBuffer[BlockRenderLayer.values().length];
    protected static final Map<BlockPos, TileEntity> TILES = new LinkedHashMap<>();
    protected boolean isDirty = true;

    public VBOWorldSceneRenderer(World world) {
        super(world);
    }

    private void uploadVBO() {
        Minecraft mc = Minecraft.getMinecraft();
        BlockRenderLayer oldRenderLayer = MinecraftForgeClient.getRenderLayer();

        try { // render block in each layer
            for (BlockRenderLayer layer : BlockRenderLayer.values()) {

                ForgeHooksClient.setRenderLayer(layer);
                int pass = layer == BlockRenderLayer.TRANSLUCENT ? 1 : 0;
                setDefaultPassRenderState(pass);

                OptiFineHelper.preRenderChunkLayer(layer);

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

                int i = layer.ordinal();
                var vbo = VBOS[i];
                if (vbo == null) vbo = VBOS[i] = new VertexBuffer(DefaultVertexFormats.BLOCK);
                vbo.bufferData(buffer.getByteBuffer());

                if (SusMods.OpenGL3.isLoaded()) {
                    var vao = VAOS[i];
                    if (vao == null) vao = VAOS[i] = new VertexArrayObject();
                    vao.bindVertexArray();
                    vbo.bindBuffer();
                    enableClientStates();
                    setupArrayPointers();
                    vao.unbindVertexArray();
                    disableClientStates();
                    vbo.unbindBuffer();
                }

                OptiFineHelper.postRenderChunkLayer(layer);
            }
        } finally {
            ForgeHooksClient.setRenderLayer(oldRenderLayer);
        }
        this.isDirty = false;
    }

    @Override
    protected void drawWorld() {
        if (this.isDirty) uploadVBO();

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

            OptiFineHelper.preRenderChunkLayer(layer);

            GlStateManager.pushMatrix();
            {
                int i = layer.ordinal();
                var vbo = VBOS[i];
                int preVBO = GlStateManager.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
                vbo.bindBuffer();
                if (SusMods.OpenGL3.isLoaded()) {
                    var vao = VAOS[i];
                    vao.bindVertexArray();
                    vbo.drawArrays(GL11.GL_QUADS);
                    vao.unbindVertexArray();
                } else {
                    enableClientStates();
                    setupArrayPointers();
                    vbo.drawArrays(GL11.GL_QUADS);
                    disableClientStates();
                }
                vbo.unbindBuffer();
                OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, preVBO);
            }
            GlStateManager.popMatrix();

            OptiFineHelper.postRenderChunkLayer(layer);
        }
        ForgeHooksClient.setRenderLayer(oldRenderLayer);

        renderTileEntities(); // Handle TileEntities

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
    }

    @Override
    public WorldSceneRenderer addRenderedBlocks(Collection<BlockPos> blocks, ISceneRenderHook _null) {
        this.isDirty = true;
        super.addRenderedBlocks(blocks, _null);
        TILES.clear();
        blocks.forEach(pos -> {
            TileEntity tile = world.getTileEntity(pos);
            if (tile != null && (!(tile instanceof IGregTechTileEntity gtte) ||
                    // Put MTEs only when it has FastRenderer
                    gtte.getMetaTileEntity() instanceof IFastRenderMetaTileEntity)) {
                TILES.put(pos, tile);
            }
        });
        return this;
    }

    protected void enableClientStates() {
        GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glEnableClientState(GL11.GL_COLOR_ARRAY);
    }

    protected void disableClientStates() {
        for (VertexFormatElement element : DefaultVertexFormats.BLOCK.getElements()) {
            switch (element.getUsage()) {
                case POSITION -> GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY);
                case COLOR -> GlStateManager.glDisableClientState(GL11.GL_COLOR_ARRAY);
                case UV -> {
                    OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + element.getIndex());
                    GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                    OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                }
                default -> {}
            }
        }
    }

    protected void setupArrayPointers() {
        if (Mods.Optifine.isModLoaded()) {
            // OptiFine I hate you
            ShadersRender.setupArrayPointersVbo();
        } else {
            // 28 == DefaultVertexFormats.BLOCK.getSize();
            GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, 28, 0);
            GlStateManager.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 28, 12);
            GlStateManager.glTexCoordPointer(2, GL11.GL_FLOAT, 28, 16);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.glTexCoordPointer(2, GL11.GL_SHORT, 28, 24);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        }
    }

    protected void renderTileEntities() {
        RenderHelper.enableStandardItemLighting();
        var terd = TileEntityRendererDispatcher.instance;
        for (var pass : RenderPass.values()) {
            ForgeHooksClient.setRenderPass(pass.ordinal());
            setPassRenderState(pass);

            TILES.forEach((pos, tile) -> {
                if (tile.shouldRenderInPass(pass.ordinal())) {
                    terd.render(tile, pos.getX(), pos.getY(), pos.getZ(), 0);
                }
            });
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
