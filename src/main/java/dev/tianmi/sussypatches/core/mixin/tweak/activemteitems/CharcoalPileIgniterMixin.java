package dev.tianmi.sussypatches.core.mixin.tweak.activemteitems;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.metatileentities.primitive.MetaTileEntityCharcoalPileIgniter;

@Mixin(value = MetaTileEntityCharcoalPileIgniter.class, remap = false)
public abstract class CharcoalPileIgniterMixin extends MultiblockControllerBase {

    // Dummy
    CharcoalPileIgniterMixin() {
        super(null);
    }

    @ModifyExpressionValue(method = "renderMetaTileEntity",
                           at = @At(value = "FIELD",
                                    target = "Lgregtech/common/metatileentities/primitive/MetaTileEntityCharcoalPileIgniter;isActive:Z",
                                    opcode = Opcodes.GETFIELD))
    private boolean alwaysActiveForItemRendering(boolean original) {
        return original || this.renderContextStack != null;
    }
}
