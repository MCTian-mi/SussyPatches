package dev.tianmi.sussypatches.core.mixin.tweak.cstorageinf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import gregtech.common.metatileentities.storage.MetaTileEntityCreativeTank;

@Mixin(value = MetaTileEntityCreativeTank.class, remap = false)
public abstract class CreativeTankMixin {

    @ModifyArg(method = "renderMetaTileEntity(DDDF)V",
               at = @At(value = "INVOKE",
                        target = "Lgregtech/client/renderer/texture/custom/QuantumStorageRenderer;renderTankAmount(DDDLnet/minecraft/util/EnumFacing;J)V"))
    private long useToken(long _69) {
        return -1;
    }
}
