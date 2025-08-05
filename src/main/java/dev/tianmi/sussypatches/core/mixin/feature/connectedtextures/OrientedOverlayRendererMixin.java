package dev.tianmi.sussypatches.core.mixin.feature.connectedtextures;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;

// A dirty workaround
@Mixin(value = OrientedOverlayRenderer.class, remap = false)
public class OrientedOverlayRendererMixin {

    @ModifyArg(method = "renderOrientedState",
               at = @At(value = "INVOKE",
                        target = "Lgregtech/client/utils/RenderUtil;adjustTrans(Lcodechicken/lib/vec/Matrix4;Lnet/minecraft/util/EnumFacing;I)Lcodechicken/lib/vec/Matrix4;"))
    private int fixZFighting(int original) {
        return 10 * original;
    }
}
