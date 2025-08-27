package dev.tianmi.sussypatches.api.core.mixin.extension;

import dev.tianmi.sussypatches.api.annotation.MixinExtension;
import gregtech.integration.jei.recipe.RecipeMapCategory;

@MixinExtension(RecipeMapCategory.class)
public interface RecipeBuilderExtension {

    Object info(String translationKey);
}
