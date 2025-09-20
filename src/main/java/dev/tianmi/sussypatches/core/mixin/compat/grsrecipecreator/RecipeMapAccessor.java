package dev.tianmi.sussypatches.core.mixin.compat.grsrecipecreator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ProgressWidget.MoveType;
import gregtech.api.recipes.RecipeMap;

@Mixin(value = RecipeMap.class, remap = false)
public interface RecipeMapAccessor {

    @Accessor("progressBarTexture")
    TextureArea getProgressBarTexture();

    @Accessor("moveType")
    MoveType getMoveType();
}
