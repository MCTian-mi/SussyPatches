package dev.tianmi.sussypatches.core.mixin.feature.fluidcontainerbar;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import gregtech.common.metatileentities.storage.MetaTileEntityDrum;

@Mixin(value = MetaTileEntityDrum.class, remap = false)
public interface DrumAccessor {

    @Accessor("tankSize")
    int getTankSize();
}
