package dev.tianmi.sussypatches.core.mixin.bugfix.redundantgas;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import gregtech.api.fluids.store.FluidStorageKey;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.unification.material.properties.FluidProperty;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2545")
@Mixin(value = FluidStorageKeys.class, remap = false)
public abstract class FluidStorageKeysMixin {

    /// This is a hard rewrite to make the
    /// @code property != null && property.getPrimaryKey() != FluidStorageKeys.LIQUID
    /// block always return false
    @Redirect(method = "lambda$static$3",
              at = @At(value = "INVOKE",
                       target = "Lgregtech/api/unification/material/properties/FluidProperty;getPrimaryKey()Lgregtech/api/fluids/store/FluidStorageKey;"))
    private static FluidStorageKey alwaysTheSame(FluidProperty ignored) {
        return FluidStorageKeys.LIQUID;
    }

    @ModifyExpressionValue(method = "lambda$static$3",
                           at = @At(value = "INVOKE",
                                    target = "Lgregtech/api/unification/material/Material;isElement()Z"))
    private static boolean extraCheckLogic(boolean isElement, @Local(name = "property") FluidProperty property) {
        return isElement || property == null || property.getPrimaryKey() != FluidStorageKeys.GAS;
    }
}
