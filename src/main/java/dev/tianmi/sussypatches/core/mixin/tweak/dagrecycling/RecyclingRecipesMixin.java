package dev.tianmi.sussypatches.core.mixin.tweak.dagrecycling;

import com.llamalad7.mixinextras.sugar.Local;
import dev.tianmi.sussypatches.loaders.recipes.RecyclingManager;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.loaders.recipe.RecyclingRecipes;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RecyclingRecipes.class, remap = false)
public abstract class RecyclingRecipesMixin {

    /// Turns the recycling data CEu registered on the fly into the input of the dependency graph, and lets
    /// [#RecyclingManager#init] write the result back before CEu builds its own recipes from it.
    @Inject(method = "init", at = @At("HEAD"))
    private static void initRecyclingManager(CallbackInfo ci) {
        RecyclingManager.init();
    }

    /// @implNote Deliberately without an `ordinal`: [RecyclingRecipes#registerArcRecycling] builds its recipe in two
    ///           mutually exclusive branches, and both of them have to be recorded. The single call in
    ///           [RecyclingRecipes#registerMaceratorRecycling] is the other target. Extractor recycling is left alone,
    ///           since it does not use the material list this graph is built from.
    @Redirect(method = {"registerMaceratorRecycling", "registerArcRecycling"},
              at = @At(target = "Lgregtech/api/recipes/RecipeBuilder;buildAndRegister()V", value = "INVOKE"))
    private static void registerRecyclingRecipe(RecipeBuilder<?> builder, @Local(argsOnly = true) ItemStack input) {
        RecyclingManager.buildAndRegister(builder, input);
    }
}
