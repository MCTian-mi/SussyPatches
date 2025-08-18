package dev.tianmi.sussypatches.api.util;

import org.apache.commons.lang3.tuple.Pair;

import dev.tianmi.sussypatches.api.core.mixin.extension.OCDisplayExtension;
import dev.tianmi.sussypatches.api.core.mixin.extension.OCDisplayExtension.DurationCalculator;
import dev.tianmi.sussypatches.api.core.mixin.extension.OCDisplayExtension.EUtCalculator;
import gregtech.api.GTValues;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTUtility;

public class SusUtil {

    public static String getPrefix(Material material) {
        return material.getModid().equals(GTValues.MODID) ? "" : material.getModid() + ":";
    }

    public static int getTierDifference(int startTier, int targetTier) {
        if (startTier == GTValues.ULV) {
            // Special treatments for ULV recipes
            if (targetTier == GTValues.ULV) {
                return 0;
            }
            startTier = GTValues.LV;
        }

        return targetTier - startTier;
    }

    // L: EUt; R: Duration
    public static Pair<Integer, Integer> getOCResult(Recipe recipe, int targetTier,
                                                     EUtCalculator eUtCalculator,
                                                     DurationCalculator durationCalculator) {
        int volt = recipe.getEUt(), dura = recipe.getDuration();

        int recipeTier = GTUtility.getTierByVoltage(volt);

        // This should always be >= 0
        int deltaTier = getTierDifference(recipeTier, targetTier);
        if (deltaTier <= 0) return Pair.of(volt, dura);

        return Pair.of(eUtCalculator.apply(volt, deltaTier),
                durationCalculator.apply(dura, deltaTier));
    }

    // L: EUt; R: Duration
    public static Pair<Integer, Integer> getOCResult(RecipeMap<?> recipeMap, Recipe recipe, int targetTier) {
        return getOCResult(recipe, targetTier,
                OCDisplayExtension.cast(recipeMap).sus$getEUtCalculator(),
                OCDisplayExtension.cast(recipeMap).sus$getDurationCalculator());
    }
}
