package dev.tianmi.sussypatches.api.core.mixin.extension;

import net.minecraft.util.math.BlockPos;

import dev.tianmi.sussypatches.api.annotation.MixinExtension;
import gregtech.client.renderer.CubeRendererState;

/// A Mixin extension interface for [CubeRendererState]
/// to allow getting the position of the block being rendered.
///
/// @see CubeRendererStateMixin
/// @see MetaTileEntityRendererMixin
@MixinExtension(CubeRendererState.class)
public interface CRSExtension {

    static CRSExtension cast(CubeRendererState state) {
        return (CRSExtension) state;
    }

    CubeRendererState sus$withPos(BlockPos pos);

    BlockPos sus$getPos();
}
