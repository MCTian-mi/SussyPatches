package dev.tianmi.sussypatches.core.mixin.tweak.dagrecycling;

import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Consumer;

/// Lets the recycling manager replay what [RecipeBuilder#buildAndRegister()] would have done, without the recycling
/// registration CEu would have injected into it.
@Mixin(value = RecipeBuilder.class, remap = false)
public interface RecipeBuilderAccessor {

    @Accessor("onBuildAction")
    Consumer<RecipeBuilder<?>> getOnBuildAction();

    @Accessor("recipeMap")
    RecipeMap<?> getRecipeMap();
}
