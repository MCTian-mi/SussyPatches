package dev.tianmi.sussypatches.core.mixin.compat.grsrecipecreator;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import gregtech.api.fluids.GTFluid.GTMaterialFluid;

@Mixin(value = GTMaterialFluid.class, remap = false)
public interface GTMaterialFluidAccessor {

    @Nullable
    @Final
    @Accessor("translationKey")
    String getTranslationKey();
}
