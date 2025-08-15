package dev.tianmi.sussypatches.core.mixin.tweak.activemteitems;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.common.metatileentities.electric.MetaTileEntityMagicEnergyAbsorber;

@Mixin(value = MetaTileEntityMagicEnergyAbsorber.class, remap = false)
public abstract class MagicEnergyAbsorberMixin extends MetaTileEntity {

    // Dummy
    MagicEnergyAbsorberMixin() {
        super(null);
    }

    @ModifyExpressionValue(method = "getRenderer",
                           at = @At(value = "FIELD",
                                    target = "Lgregtech/common/metatileentities/electric/MetaTileEntityMagicEnergyAbsorber;isActive:Z",
                                    opcode = Opcodes.GETFIELD))
    private boolean alwaysActiveForItemRendering(boolean original) {
        return original || this.renderContextStack != null;
    }
}
