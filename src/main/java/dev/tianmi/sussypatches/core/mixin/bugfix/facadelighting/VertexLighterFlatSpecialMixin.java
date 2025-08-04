package dev.tianmi.sussypatches.core.mixin.bugfix.facadelighting;

import net.minecraftforge.client.model.pipeline.VertexLighterFlat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import gregtech.client.model.pipeline.VertexLighterFlatSpecial;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2843")
@Mixin(value = VertexLighterFlatSpecial.class, remap = false)
public abstract class VertexLighterFlatSpecialMixin extends VertexLighterFlat {

    // Dummy
    @SuppressWarnings("DataFlowIssue")
    VertexLighterFlatSpecialMixin() {
        super(null);
    }

    // Using hard @Redirect here since it's a bug anyway.
    @Redirect(method = "processQuad",
              at = @At(value = "INVOKE",
                       target = "Lgregtech/client/model/pipeline/VertexLighterFlatSpecial;updateColor([F[FFFFII)V"))
    private void toNonStatic(float[] normal, float[] color, float x, float y, float z, int tint, int multiplier) {
        this.updateColor(normal, color, x, y, z, tint, multiplier);
    }
}
