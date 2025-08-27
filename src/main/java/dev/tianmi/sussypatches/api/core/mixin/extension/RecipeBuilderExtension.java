package dev.tianmi.sussypatches.api.core.mixin.extension;

import dev.tianmi.sussypatches.api.annotation.MixinExtension;
import gregtech.api.gui.Widget;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.integration.jei.recipe.RecipeMapCategory;

@MixinExtension(RecipeMapCategory.class)
public interface RecipeBuilderExtension {
    Object info(String translationKey);
}
