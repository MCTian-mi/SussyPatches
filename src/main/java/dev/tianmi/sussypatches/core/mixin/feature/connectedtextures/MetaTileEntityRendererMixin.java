package dev.tianmi.sussypatches.core.mixin.feature.connectedtextures;

import net.minecraft.util.math.BlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import dev.tianmi.sussypatches.api.core.mixin.extension.CRSExtension;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.client.renderer.CubeRendererState;
import gregtech.client.renderer.handler.MetaTileEntityRenderer;

@Mixin(value = MetaTileEntityRenderer.class, remap = false)
public class MetaTileEntityRendererMixin {

    @ModifyExpressionValue(method = "renderBlock",
                           at = @At(value = "NEW",
                                    target = "gregtech/client/renderer/CubeRendererState"))
    private CubeRendererState writeBlockPos(
                                            CubeRendererState original,
                                            @Local(argsOnly = true) BlockPos pos,
                                            @Local(name = "metaTileEntity") MetaTileEntity mte) {
        return CRSExtension.cast(original).susy$withPos(pos);
    }
}
