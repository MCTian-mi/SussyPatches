package dev.tianmi.sussypatches.core.mixin.tweak.activemteitems;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import gregtech.api.capability.impl.FluidDrillLogic;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityFluidDrill;

@Mixin(value = FluidDrillLogic.class, remap = false)
public abstract class FluidDrillLogicMixin {

    @Shadow
    @Final
    private MetaTileEntityFluidDrill metaTileEntity;

    @ModifyReturnValue(method = "isActive", at = @At("TAIL"))
    private boolean alwaysActiveForItemRendering(boolean original) {
        return original || ((MetaTileEntityAccessor) metaTileEntity).getRenderContextStack() != null;
    }
}
