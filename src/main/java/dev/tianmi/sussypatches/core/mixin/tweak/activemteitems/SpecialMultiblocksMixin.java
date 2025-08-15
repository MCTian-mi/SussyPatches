package dev.tianmi.sussypatches.core.mixin.tweak.activemteitems;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.common.metatileentities.multi.electric.*;

@Mixin(value = {
        MetaTileEntityActiveTransformer.class,
        MetaTileEntityDataBank.class,
        MetaTileEntityHPCA.class,
        MetaTileEntityCleanroom.class,
        MetaTileEntityPowerSubstation.class,
}, remap = false)
public abstract class SpecialMultiblocksMixin extends MultiblockWithDisplayBase {

    // Dummy
    SpecialMultiblocksMixin() {
        super(null);
    }

    @ModifyReturnValue(method = "isActive", at = @At("TAIL"))
    private boolean alwaysActiveForItemRendering(boolean original) {
        return original || this.renderContextStack != null;
    }
}
