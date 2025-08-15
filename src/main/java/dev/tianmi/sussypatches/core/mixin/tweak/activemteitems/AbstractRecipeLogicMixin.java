package dev.tianmi.sussypatches.core.mixin.tweak.activemteitems;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.metatileentity.MTETrait;

@Mixin(value = AbstractRecipeLogic.class, remap = false)
public abstract class AbstractRecipeLogicMixin extends MTETrait {

    // Dummy
    @SuppressWarnings("DataFlowIssue")
    AbstractRecipeLogicMixin() {
        super(null);
    }

    @ModifyReturnValue(method = "isActive", at = @At("TAIL"))
    private boolean alwaysActiveForItemRendering(boolean original) {
        return original || ((MetaTileEntityAccessor) metaTileEntity).getRenderContextStack() != null;
    }
}
