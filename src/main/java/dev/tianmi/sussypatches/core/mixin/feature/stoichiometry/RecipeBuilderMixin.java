package dev.tianmi.sussypatches.core.mixin.feature.stoichiometry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.tianmi.sussypatches.common.stoichiometry.StoichiometryVerifier;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.ValidationResult;

@Mixin(value = RecipeBuilder.class, remap = false)
public abstract class RecipeBuilderMixin {

    @Shadow
    protected RecipeMap<?> recipeMap;

    @Inject(method = "build", at = @At("RETURN"))
    private void sus$runStoichiometryCheck(CallbackInfoReturnable<ValidationResult<Recipe>> cir) {
        ValidationResult<Recipe> validation = cir.getReturnValue();
        if (validation == null || validation.getType() != EnumValidationResult.VALID) {
            return;
        }

        if (recipeMap == null) {
            return;
        }

        Recipe recipe = validation.getResult();
        if (recipe == null) {
            return;
        }

        StoichiometryVerifier.verify(recipe, recipeMap);
    }
}
