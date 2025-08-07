package dev.tianmi.sussypatches.core.mixin.compat.tweakerinfo;

import java.util.function.BooleanSupplier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import gregtech.api.gui.GuiTextures;
import gregtech.api.recipes.Recipe;
import gregtech.api.util.LocalizationUtils;
import gregtech.integration.RecipeCompatUtil;
import gregtech.integration.jei.recipe.GTRecipeWrapper;
import gregtech.integration.jei.utils.AdvancedRecipeWrapper;
import gregtech.integration.jei.utils.JeiButton;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2638")
@Mixin(value = GTRecipeWrapper.class, remap = false)
public abstract class GTRecipeWrapperMixin extends AdvancedRecipeWrapper {

    @Shadow
    @Final
    private Recipe recipe;

    @ModifyArg(method = "lambda$initExtras$1",
               at = @At(value = "INVOKE",
                        target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
                        ordinal = 0))
    private static Object initTweakerPredicates(Object original) {
        return LocalizationUtils.format("sussypatches.jei.remove_recipe.tooltip",
                RecipeCompatUtil.getTweakerName());
    }

    @ModifyArg(method = "initExtras",
               at = @At(value = "INVOKE",
                        target = "Lgregtech/integration/jei/utils/JeiButton;setActiveSupplier(Ljava/util/function/BooleanSupplier;)Lgregtech/integration/jei/utils/JeiButton;"))
    private BooleanSupplier useDefaultSupplier(BooleanSupplier creativePlayerPredicate) {
        return () -> creativePlayerPredicate.getAsBoolean() && !recipe.getIsCTRecipe() &&
                !recipe.isGroovyRecipe();
    }

    @Inject(method = "initExtras", at = @At("TAIL"))
    private void addTweakerInfo(CallbackInfo ci,
                                @Local(name = "creativePlayerCtPredicate") BooleanSupplier creativePlayerPredicate) {
        buttons.add(new JeiButton(166, 2, 10, 10)
                .setTextures(GuiTextures.INFO_ICON)
                .setTooltipBuilder(lines -> lines.add(recipe.isGroovyRecipe() ?
                        LocalizationUtils.format("sussypatches.jei.gs_recipe.tooltip") :
                        LocalizationUtils.format("sussypatches.jei.ct_recipe.tooltip")))
                .setClickAction((mc, x, y, button) -> false)
                .setActiveSupplier(() -> creativePlayerPredicate.getAsBoolean() &&
                        (recipe.getIsCTRecipe() || recipe.isGroovyRecipe())));
    }
}
