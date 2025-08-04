package dev.tianmi.sussypatches.core.mixin.bugfix.pipeinvcrash;

import net.minecraft.util.EnumFacing;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import gregtech.api.pipenet.tile.TileEntityPipeBase;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2844")
@Mixin(value = TileEntityPipeBase.class, remap = false)
public abstract class TileEntityPipeBaseMixin {

    @Inject(method = "isFaceBlocked(Lnet/minecraft/util/EnumFacing;)Z",
            at = @At(value = "HEAD"),
            cancellable = true)
    private void checkNotNull(EnumFacing side, CallbackInfoReturnable<Boolean> cir) {
        if (side == null) cir.setReturnValue(true);
    }
}
