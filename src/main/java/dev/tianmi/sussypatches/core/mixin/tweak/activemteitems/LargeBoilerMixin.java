package dev.tianmi.sussypatches.core.mixin.tweak.activemteitems;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.common.metatileentities.multi.MetaTileEntityLargeBoiler;

@Mixin(value = MetaTileEntityLargeBoiler.class, remap = false)
public abstract class LargeBoilerMixin extends MultiblockWithDisplayBase {

    // Dummy
    LargeBoilerMixin() {
        super(null);
    }

    @ModifyExpressionValue(method = "renderMetaTileEntity",
                           at = @At(value = "INVOKE",
                                    target = "Lgregtech/common/metatileentities/multi/MetaTileEntityLargeBoiler;isActive()Z"))
    private boolean alwaysActiveForItemRendering(boolean original) {
        return original || this.renderContextStack != null;
    }
}
