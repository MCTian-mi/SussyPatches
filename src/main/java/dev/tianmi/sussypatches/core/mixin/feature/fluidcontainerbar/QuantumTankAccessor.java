package dev.tianmi.sussypatches.core.mixin.feature.fluidcontainerbar;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import gregtech.common.metatileentities.storage.MetaTileEntityQuantumTank;

@Mixin(value = MetaTileEntityQuantumTank.class, remap = false)
public interface QuantumTankAccessor {

    @Accessor("maxFluidCapacity")
    int getMaxFluidCapacity();
}
