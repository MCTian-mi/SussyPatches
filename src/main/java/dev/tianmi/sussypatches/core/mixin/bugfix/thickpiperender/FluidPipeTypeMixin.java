package dev.tianmi.sussypatches.core.mixin.bugfix.thickpiperender;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import gregtech.common.pipelike.fluidpipe.FluidPipeType;

@Mixin(value = FluidPipeType.class, remap = false)
public abstract class FluidPipeTypeMixin {

    @ModifyConstant(method = "<clinit>",
                    constant = @Constant(floatValue = 0.95F),
                    require = 2)
    private static float sus$thickPipeRender(float _0_95F) {
        return 0.9375F; // Don't ask me why this works, I am as clueless as you.
    }
}
