package dev.tianmi.sussypatches.core.mixin.api.recipeinfo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import dev.tianmi.sussypatches.api.core.mixin.extension.RecipeBuilderExtension;
import dev.tianmi.sussypatches.api.recipes.InfoProperty;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.recipeproperties.RecipeProperty;

@Mixin(value = RecipeBuilder.class, remap = false)
public abstract class RecipeBuilderMixin<R extends RecipeBuilder<R>> implements RecipeBuilderExtension {

    public Object info(@Nullable String translationKey) {
        this.applyProperty(InfoProperty.getInstance(), new InfoProperty.TranslationData(translationKey));
        return this;
    }

    public Object info(@Nullable String translationKey, Object... args) {
        this.applyProperty(InfoProperty.getInstance(), new InfoProperty.TranslationData(translationKey, args));
        return this;
    }

    @Shadow
    public abstract boolean applyProperty(@NotNull RecipeProperty<?> property, @Nullable Object value);
}
