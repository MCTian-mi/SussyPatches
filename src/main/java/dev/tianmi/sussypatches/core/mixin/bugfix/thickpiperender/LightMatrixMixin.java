package dev.tianmi.sussypatches.core.mixin.bugfix.thickpiperender;

import codechicken.lib.colour.ColourRGBA;
import codechicken.lib.lighting.LC;
import codechicken.lib.lighting.LightMatrix;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

// Fixes lighting issue by separating block/skylight interpolation
@Mixin(value = LightMatrix.class, remap = false)
public abstract class LightMatrixMixin implements IVertexOperation {

    @Shadow
    public abstract int[] brightness(int side);

    @Shadow
    public abstract float[] ao(int side);

    /// @author MCTian_mi
    /// @reason to avoid unnecessary calculations
    @Override
    @Overwrite
    public void operate(CCRenderState state) {
        LC lc = state.lc;
        float[] a = ao(lc.side);
        float f = (a[0] * lc.fa + a[1] * lc.fb + a[2] * lc.fc + a[3] * lc.fd);
        int[] b = brightness(lc.side);
        state.colour = ColourRGBA.multiplyC(state.colour, f);
        int block = Math.round((b[0] & 0xFFFF) * lc.fa + (b[1] & 0xFFFF) * lc.fb +
                (b[2] & 0xFFFF) * lc.fc + (b[3] & 0xFFFF) * lc.fd);
        int sky = Math.round((b[0] >>> 16) * lc.fa + (b[1] >>> 16) * lc.fb +
                (b[2] >>> 16) * lc.fc + (b[3] >>> 16) * lc.fd);
        state.brightness = (sky << 16) | (block & 0xFFFF);
    }
}
