package dev.tianmi.sussypatches.core.mixin.tweak.activemteitems;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.common.metatileentities.electric.MetaTileEntityWorldAccelerator;

@Mixin(value = MetaTileEntityWorldAccelerator.class, remap = false)
public abstract class WorldAcceleratorMixin extends TieredMetaTileEntity {

    // Dummy
    WorldAcceleratorMixin() {
        super(null, 0);
    }

    @ModifyExpressionValue(method = "renderMetaTileEntity",
                           at = @At(value = "FIELD",
                                    target = "Lgregtech/common/metatileentities/electric/MetaTileEntityWorldAccelerator;isActive:Z",
                                    opcode = Opcodes.GETFIELD),
                           require = 2)
    private boolean alwaysActiveForItemRendering(boolean original) {
        return original || this.renderContextStack != null;
    }
}
