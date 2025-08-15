package dev.tianmi.sussypatches.core.mixin.tweak.activemteitems;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.common.metatileentities.electric.MetaTileEntityItemCollector;

@Mixin(value = MetaTileEntityItemCollector.class, remap = false)
public abstract class ItemCollectorMixin extends TieredMetaTileEntity {

    // Dummy
    ItemCollectorMixin() {
        super(null, 0);
    }

    @ModifyExpressionValue(method = "renderMetaTileEntity",
                           at = @At(value = "FIELD",
                                    target = "Lgregtech/common/metatileentities/electric/MetaTileEntityItemCollector;isWorking:Z",
                                    opcode = Opcodes.GETFIELD))
    private boolean alwaysActiveForItemRendering(boolean original) {
        return original || this.renderContextStack != null;
    }
}
