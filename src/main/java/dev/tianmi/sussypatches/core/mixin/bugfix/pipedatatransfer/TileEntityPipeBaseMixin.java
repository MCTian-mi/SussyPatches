package dev.tianmi.sussypatches.core.mixin.bugfix.pipedatatransfer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import gregtech.api.pipenet.tile.PipeCoverableImplementation;
import gregtech.api.pipenet.tile.TileEntityPipeBase;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2619")
@Mixin(value = TileEntityPipeBase.class, remap = false)
public abstract class TileEntityPipeBaseMixin {

    @WrapOperation(method = "transferDataFrom",
                   at = @At(value = "INVOKE",
                            target = "Lgregtech/api/pipenet/tile/PipeCoverableImplementation;transferDataTo(Lgregtech/api/pipenet/tile/PipeCoverableImplementation;)V"))
    private void reverseTransferTarget(PipeCoverableImplementation self, PipeCoverableImplementation other,
                                       Operation<Void> method) {
        method.call(other, self);
    }
}
