package dev.tianmi.sussypatches.core.mixin.bugfix.variousgrsissue;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.integration.groovy.GrSRecipeHelper;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2785")
@Mixin(value = GrSRecipeHelper.class, remap = false)
public abstract class GrSRecipeHelperMixin {

    @ModifyReturnValue(method = "getRecipeRemoveLine",
                       at = @At(value = "INVOKE",
                                target = "Lgregtech/api/recipes/ingredients/GTRecipeInput;getAmount()I",
                                ordinal = 0))
    private static int skipThis(final GTRecipeInput idc) {
        return Integer.MAX_VALUE;
    }
}
