package dev.tianmi.sussypatches.integration.grs;

import dev.tianmi.sussypatches.api.recipe.property.InfoProperty;
import dev.tianmi.sussypatches.api.recipe.property.StoichiometryProperty;
import gregtech.api.recipes.RecipeBuilder;

public final class GroovyExpansions {

    public static <R extends RecipeBuilder<R>> R info(R builder, String translationKey, Object... args) {
        builder.applyProperty(InfoProperty.getInstance(), new InfoProperty.TranslationData(translationKey, args));
        return builder;
    }

    public static <R extends RecipeBuilder<R>> R lossy(R builder) {
        builder.applyProperty(StoichiometryProperty.getInstance(), StoichiometryProperty.lossy());
        return builder;
    }

    public static <R extends RecipeBuilder<R>> R nonStoichiometric(R builder) {
        builder.applyProperty(StoichiometryProperty.getInstance(), StoichiometryProperty.disableVerifier());
        return builder;
    }
}
