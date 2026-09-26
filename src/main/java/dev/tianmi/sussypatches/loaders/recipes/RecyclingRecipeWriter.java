package dev.tianmi.sussypatches.loaders.recipes;

import dev.tianmi.sussypatches.api.recycling.Recyclable;
import dev.tianmi.sussypatches.core.mixin.tweak.dagrecycling.RecipeBuilderAccessor;
import dev.tianmi.sussypatches.core.mixin.tweak.dagrecycling.RecyclingRecipesMixin;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.ValidationResult;
import gregtech.loaders.recipe.RecyclingRecipes;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;

/// Owns the recycling recipes (arc furnace and macerator) generated from the DAG: it records every recipe CEu's
/// [RecyclingRecipes] builds from the unification map, so that a node whose data changed can have exactly its own
/// old recipes removed and rebuilt.
@NullMarked
public final class RecyclingRecipeWriter {

    /// Recyclable -> the arc furnace recipe CEu built for it, so that it can be removed again.
    public static final Object2ObjectOpenHashMap<Recyclable, Recipe> arcRecipes = new Object2ObjectOpenHashMap<>();
    /// Recyclable -> the macerator recipe CEu built for it, so that it can be removed again.
    public static final Object2ObjectOpenHashMap<Recyclable, Recipe> maceratorRecipes = new Object2ObjectOpenHashMap<>();

    private RecyclingRecipeWriter() {
    }

    /// Only removes recipes from [RecipeMaps#MACERATOR_RECIPES] and [RecipeMaps#ARC_FURNACE_RECIPES].
    ///
    /// @implNote As long as [RecipeMap#removeRecipe(Recipe)] is called while a groovy script runs, CEu treats the
    ///           recipe as a GrS recipe and keeps a backup of it.
    public static void removeByRecyclable(Recyclable recyclable) {
        Recipe recipe = arcRecipes.remove(recyclable);
        if (recipe != null) {
            RecipeMaps.ARC_FURNACE_RECIPES.removeRecipe(recipe);
        }

        recipe = maceratorRecipes.remove(recyclable);
        if (recipe != null) {
            RecipeMaps.MACERATOR_RECIPES.removeRecipe(recipe);
        }
    }

    /// Rebuilds the macerator and arc furnace recipes of `node` from its freshly computed unification info, so that
    /// [RecipeMap#compileRecipe] does not silently reject them as duplicates.
    public static void rebuild(Recyclable node) {
        ItemStack output = node.asStack();
        ItemMaterialInfo info = OreDictUnifier.getMaterialInfo(output);
        if (info == null) return;

        RecyclingRecipes.registerRecyclingRecipes(output, new ArrayList<>(info.getMaterials()), false, null);
    }

    /// Mixin hook, with lots of code copied from CEu.
    ///
    /// @see RecyclingRecipesMixin
    /// @see RecipeBuilder#buildAndRegister()
    public static void buildAndRegister(RecipeBuilder<?> recipeBuilder, ItemStack output) {
        var accessor = (RecipeBuilderAccessor) recipeBuilder;

        var buildAction = accessor.getOnBuildAction();
        if (buildAction != null) {
            buildAction.accept(recipeBuilder);
        }

        ValidationResult<Recipe> validationResult = recipeBuilder.build();
        RecipeMap<?> recipeMap = accessor.getRecipeMap();
        recipeMap.addRecipe(validationResult);

        /// Extra code to register recycling recipes
        if (validationResult.getType() == EnumValidationResult.VALID) {
            Recipe recipe = validationResult.getResult();
            Recyclable recyclable = Recyclable.from(output);
            if (recyclable == null) return;

            if (recipeMap == RecipeMaps.ARC_FURNACE_RECIPES) {
                arcRecipes.put(recyclable, recipe);
            } else if (recipeMap == RecipeMaps.MACERATOR_RECIPES) {
                maceratorRecipes.put(recyclable, recipe);
            }
        }
    }
}
