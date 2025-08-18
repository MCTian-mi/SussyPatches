package dev.tianmi.sussypatches.api.core.mixin.extension;

import dev.tianmi.sussypatches.api.annotation.MixinExtension;
import gregtech.api.recipes.RecipeMap;

@MixinExtension(RecipeMap.class)
public interface OCDisplayExtension {

    static OCDisplayExtension cast(RecipeMap<?> recipeMap) {
        return (OCDisplayExtension) recipeMap;
    }

    boolean sus$hasOCButton();

    void sus$setHasOCButton(boolean enabled);

    void sus$setEUtCalculator(EUtCalculator calculator);

    EUtCalculator sus$getEUtCalculator();

    void sus$setDurationCalculator(DurationCalculator calculator);

    DurationCalculator sus$getDurationCalculator();

    @FunctionalInterface
    interface EUtCalculator {

        EUtCalculator DEFAULT = (voltage, deltaTier) -> voltage * (int) Math.pow(4, deltaTier);

        int apply(int voltage, int deltaTier);
    }

    @FunctionalInterface
    interface DurationCalculator {

        DurationCalculator DEFAULT = (duration, deltaTier) -> Math.max(1, duration / (int) Math.pow(2, deltaTier));

        DurationCalculator PERFECT_OC = (duration, deltaTier) -> Math.max(1, duration / (int) Math.pow(4, deltaTier));

        int apply(int duration, int deltaTier);
    }
}
