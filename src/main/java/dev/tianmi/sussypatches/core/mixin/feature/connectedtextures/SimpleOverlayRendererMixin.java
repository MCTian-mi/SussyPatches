package dev.tianmi.sussypatches.core.mixin.feature.connectedtextures;

import net.minecraft.util.EnumFacing;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.sugar.Local;

import codechicken.lib.vec.Matrix4;
import dev.tianmi.sussypatches.client.renderer.textures.ConnectedTextures;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.client.utils.RenderUtil;

@Mixin(value = SimpleOverlayRenderer.class, remap = false)
public abstract class SimpleOverlayRendererMixin implements ICubeRenderer {

    @ModifyArg(method = "renderOrientedState",
               at = @At(value = "INVOKE",
                        target = "Lgregtech/client/renderer/texture/Textures;renderFace(Lcodechicken/lib/render/CCRenderState;Lcodechicken/lib/vec/Matrix4;[Lcodechicken/lib/render/pipeline/IVertexOperation;Lnet/minecraft/util/EnumFacing;Lcodechicken/lib/vec/Cuboid6;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/minecraft/util/BlockRenderLayer;)V"),
               require = 3)
    private Matrix4 fixZFighting(Matrix4 renderTranslation, @Local(argsOnly = true) EnumFacing facing) {
        if (ConnectedTextures.shouldOffset(this)) {
            // An arbitrary number to make it look good.
            // Would be much appreciated if anyone knows how to avoid z-fighting w/o this.
            renderTranslation = RenderUtil.adjustTrans(renderTranslation, facing, 10);
        }
        return renderTranslation;
    }
}
