package dev.tianmi.sussypatches.core.mixin.tweak.activemteitems;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import gregtech.api.capability.impl.miner.MinerLogic;
import gregtech.api.metatileentity.MetaTileEntity;

@Mixin(value = MinerLogic.class, remap = false)
public abstract class MinerLogicMixin {

    @Shadow
    @Final
    protected MetaTileEntity metaTileEntity;

    @ModifyReturnValue(method = "isWorking", at = @At("TAIL"))
    private boolean alwaysActiveForItemRendering(boolean original) {
        return original || ((MetaTileEntityAccessor) metaTileEntity).getRenderContextStack() != null;
    }
}
