package dev.tianmi.sussypatches.core.mixin.tweak.cstorageinf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import gregtech.common.metatileentities.storage.MetaTileEntityCreativeChest;

@Mixin(value = MetaTileEntityCreativeChest.class, remap = false)
public abstract class CreativeChestMixin {

    @ModifyArg(method = "renderMetaTileEntity(DDDF)V",
               at = @At(value = "INVOKE",
                        target = "Lgregtech/client/renderer/texture/custom/QuantumStorageRenderer;renderChestStack(DDDLgregtech/common/metatileentities/storage/MetaTileEntityQuantumChest;Lnet/minecraft/item/ItemStack;JF)V"))
    private long useToken(long _420) {
        return -1;
    }
}
