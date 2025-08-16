package dev.tianmi.sussypatches.core.mixin.feature.interactivestorage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.tianmi.sussypatches.api.core.mixin.extension.QChestCDExtension;
import dev.tianmi.sussypatches.common.helper.QChestInteractions;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumChest;

@Mixin(value = MetaTileEntityQuantumChest.class, remap = false)
public abstract class QuantumChestMixin extends MetaTileEntity implements QChestCDExtension {

    @Unique
    private int sus$coolDown = 0;

    // Dummy
    QuantumChestMixin() {
        super(null);
    }

    @Unique
    @Override
    public int sus$getCoolDown() {
        return this.sus$coolDown;
    }

    @Unique
    @Override
    public void sus$refreshCoolDown() {
        this.sus$coolDown = QChestInteractions.COOL_DOWN;
    }

    @Inject(method = "update", at = @At("TAIL"))
    private void coolDown(CallbackInfo ci) {
        if (this.sus$coolDown > 0) {
            this.sus$coolDown--;
        }
    }
}
