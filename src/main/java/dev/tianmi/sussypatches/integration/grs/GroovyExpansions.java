package dev.tianmi.sussypatches.integration.grs;

import dev.tianmi.sussypatches.api.recipe.property.InfoProperty;
import gregtech.api.recipes.RecipeBuilder;

public final class GroovyExpansions {

    public static <R extends RecipeBuilder<R>> RecipeBuilder<R> info(RecipeBuilder<R> builder, String translationKey,
                                                                     Object... args) {
        builder.applyProperty(InfoProperty.getInstance(), new InfoProperty.TranslationData(translationKey, args));
        return builder;
    }
}
