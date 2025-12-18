package dev.tianmi.sussypatches.integration.grs;

import dev.tianmi.sussypatches.api.recipe.property.InfoProperty;
import dev.tianmi.sussypatches.api.recipe.property.StoichiometryProperty;
import dev.tianmi.sussypatches.api.unification.material.properties.MolarProperty;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.unification.material.Material;

public final class GroovyExpansions {

    public static <R extends RecipeBuilder<R>> R info(R builder, String translationKey, Object... args) {
        builder.applyProperty(InfoProperty.getInstance(), new InfoProperty.TranslationData(translationKey, args));
        return builder;
    }

    public static <R extends RecipeBuilder<R>> R exact(R builder) {
        builder.applyProperty(StoichiometryProperty.getInstance(), StoichiometryProperty.exact());
        return builder;
    }

    public static <R extends RecipeBuilder<R>> R nonStoichiometric(R builder) {
        builder.applyProperty(StoichiometryProperty.getInstance(), StoichiometryProperty.disableVerifier());
        return builder;
    }

    public static Material itemMolar(Material material, int itemsToMole) {
        material.setProperty(MolarProperty.MOLAR, MolarProperty.fromItemConversion(itemsToMole));
        return material;
    }

    public static Material fluidMolar(Material material, int fluidToMole) {
        material.setProperty(MolarProperty.MOLAR, MolarProperty.fromFluidConversion(fluidToMole));
        return material;
    }

    public static Material itemMolarFluidSize(Material material, int itemsToMole, int fluidToItem) {
        material.setProperty(MolarProperty.MOLAR, MolarProperty.fromItemConversion(itemsToMole, fluidToItem));
        return material;
    }

    public static Material fluidMolarFluidSize(Material material, int fluidToMole, int fluidToItem) {
        material.setProperty(MolarProperty.MOLAR, MolarProperty.fromFluidConversion(fluidToMole, fluidToItem));
        return material;
    }
}
