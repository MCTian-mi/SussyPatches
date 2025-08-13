package dev.tianmi.sussypatches.core.mixin.feature.connectedtextures;

import net.minecraft.util.math.BlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import dev.tianmi.sussypatches.api.core.mixin.extension.CRSExtension;
import gregtech.client.renderer.CubeRendererState;

@Mixin(value = CubeRendererState.class, remap = false)
public abstract class CubeRendererStateMixin implements CRSExtension {

    @Unique
    private BlockPos sus$pos = BlockPos.ORIGIN;

    @Unique
    @Override
    public CubeRendererState sus$withPos(BlockPos pos) {
        this.sus$pos = pos;
        return (CubeRendererState) (Object) this;
    }

    @Unique
    @Override
    public BlockPos sus$getPos() {
        return this.sus$pos;
    }
}
