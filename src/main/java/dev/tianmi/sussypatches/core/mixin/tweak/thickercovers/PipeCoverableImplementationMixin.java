package dev.tianmi.sussypatches.core.mixin.tweak.thickercovers;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import gregtech.api.pipenet.tile.PipeCoverableImplementation;

@Mixin(value = PipeCoverableImplementation.class, remap = false)
public abstract class PipeCoverableImplementationMixin {

    @ModifyArg(method = "getCoverPlateThickness",
               at = @At(value = "INVOKE",
                        target = "Ljava/lang/Math;min(DD)D"),
               index = 0)
    private double doublesTheThickness(double original) {
        return original * 2;
    }
}
