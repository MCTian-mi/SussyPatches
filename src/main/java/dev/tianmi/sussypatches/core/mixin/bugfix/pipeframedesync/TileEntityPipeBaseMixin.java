package dev.tianmi.sussypatches.core.mixin.bugfix.pipeframedesync;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import gregtech.api.pipenet.tile.TileEntityPipeBase;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2847")
@Mixin(value = TileEntityPipeBase.class, remap = false)
public abstract class TileEntityPipeBaseMixin {

    @ModifyExpressionValue(method = "setFrameMaterial",
                           at = @At(value = "FIELD",
                                    target = "Lnet/minecraft/world/World;isRemote:Z",
                                    opcode = Opcodes.GETFIELD,
                                    remap = true))
    private boolean reverseIsRemote(boolean original) {
        return !original;
    }
}
