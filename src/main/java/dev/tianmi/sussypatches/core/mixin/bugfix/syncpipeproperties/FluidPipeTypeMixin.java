package dev.tianmi.sussypatches.core.mixin.bugfix.syncpipeproperties;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.common.pipelike.fluidpipe.FluidPipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = FluidPipeType.class, remap = false)
public abstract class FluidPipeTypeMixin {

    @ModifyReturnValue(method = "modifyProperties(Lgregtech/api/unification/material/properties/FluidPipeProperties;)Lgregtech/api/unification/material/properties/FluidPipeProperties;",
                       at = @At("RETURN"))
    private FluidPipeProperties sync(FluidPipeProperties modified, @Local(argsOnly = true) FluidPipeProperties original) {
        for (var attribute : original.getContainedAttributes()) {
            modified.setCanContain(attribute, original.canContain(attribute));
        }
        return modified;
    }
}
