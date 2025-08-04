package dev.tianmi.sussypatches.core.mixin.compat.variousgrsissue;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.integration.groovy.GrSRecipeHelper;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2785")
@Mixin(value = GrSRecipeHelper.class, remap = false)
public abstract class GrSRecipeHelperMixin {

    // Using hard @Redirect here since it's a bug anyway.
    @Redirect(method = "getRecipeRemoveLine",
              at = @At(value = "INVOKE",
                       target = "Lgregtech/api/recipes/ingredients/GTRecipeInput;getAmount()I",
                       ordinal = 0))
    private static int skipThis(GTRecipeInput idc) {
        return 0; // Use 0 < 1 to skip the logic
    }
}
