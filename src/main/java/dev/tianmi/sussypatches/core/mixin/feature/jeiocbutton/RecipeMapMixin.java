package dev.tianmi.sussypatches.core.mixin.feature.jeiocbutton;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import dev.tianmi.sussypatches.api.core.mixin.extension.OCDisplayExtension;
import gregtech.api.recipes.RecipeMap;

@Mixin(value = RecipeMap.class, remap = false)
public abstract class RecipeMapMixin implements OCDisplayExtension {

    @Unique
    private boolean sus$jeiOCButton = true;

    @Unique
    private EUtCalculator sus$EUtCalculator = EUtCalculator.DEFAULT;

    @Unique
    private DurationCalculator sus$durationCalculator = DurationCalculator.DEFAULT;

    @Unique
    @Override
    public boolean sus$hasOCButton() {
        return this.sus$jeiOCButton;
    }

    @Unique
    @Override
    public void sus$setHasOCButton(boolean enabled) {
        this.sus$jeiOCButton = enabled;
    }

    @Unique
    @Override
    public void sus$setEUtCalculator(EUtCalculator calculator) {
        this.sus$EUtCalculator = calculator;
    }

    @Unique
    @Override
    public EUtCalculator sus$getEUtCalculator() {
        return this.sus$EUtCalculator;
    }

    @Unique
    @Override
    public void sus$setDurationCalculator(DurationCalculator calculator) {
        this.sus$durationCalculator = calculator;
    }

    @Unique
    @Override
    public DurationCalculator sus$getDurationCalculator() {
        return this.sus$durationCalculator;
    }
}
