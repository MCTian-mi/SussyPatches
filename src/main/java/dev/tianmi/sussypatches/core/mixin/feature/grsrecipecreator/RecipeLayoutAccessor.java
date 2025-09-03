package dev.tianmi.sussypatches.core.mixin.feature.grsrecipecreator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.gui.recipes.RecipeLayout;

@Mixin(value = RecipeLayout.class, remap = false)
public interface RecipeLayoutAccessor {

    @Accessor("recipeWrapper")
    IRecipeWrapper getRecipeWrapper();
}
