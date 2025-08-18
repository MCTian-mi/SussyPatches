package dev.tianmi.sussypatches.core.mixin.feature.jeiocbutton;

import java.util.Collection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import dev.tianmi.sussypatches.api.core.mixin.extension.OCDisplayExtension;
import dev.tianmi.sussypatches.api.util.SusUtil;
import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GTUtility;
import gregtech.integration.jei.recipe.GTRecipeWrapper;
import gregtech.integration.jei.utils.AdvancedRecipeWrapper;
import gregtech.integration.jei.utils.JeiButton;

@Mixin(value = GTRecipeWrapper.class, remap = false)
public abstract class GTRecipeWrapperMixin extends AdvancedRecipeWrapper {

    @Shadow
    @Final
    private RecipeMap<?> recipeMap;

    @Shadow
    @Final
    private Recipe recipe;

    @Unique
    private int sus$tier;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initMinimumTier(RecipeMap<?> recipeMap, Recipe recipe, CallbackInfo ci) {
        this.sus$tier = GTUtility.getTierByVoltage(recipe.getEUt());
    }

    @Inject(method = "initExtras", at = @At("HEAD"))
    private void addOCButton(CallbackInfo ci) {
        final int x = 152, y = 96, width = 24, height = 14;
        buttons.add(new JeiButton(x, y, width, height) {

            @Override
            public void render(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
                super.render(minecraft, recipeWidth, recipeHeight, mouseX, mouseY);
                final var fr = Minecraft.getMinecraft().fontRenderer;
                final var text = GTValues.VNF[sus$tier];
                final int sWidth = fr.getStringWidth(text);
                final int sHeight = 8; // Hardcoded
                Minecraft.getMinecraft().fontRenderer.drawString(text, x + (width - sWidth + 1) / 2,
                        y + (height - sHeight + 1) / 2, 0);
            }
        }
                .setTextures(GuiTextures.VANILLA_BUTTON.getSubArea(0, 0, 1, 0.5)) // TODO
                .setActiveSupplier(() -> OCDisplayExtension.cast(recipeMap).sus$hasOCButton())
                .setTooltipBuilder(lines -> lines.add("Current Tier: " + GTValues.VNF[sus$tier]))
                .setClickAction((minecraft, mouseX, mouseY, mouseButton) -> {
                    this.sus$tier = Math.max((sus$tier + 1) % GTValues.V.length,
                            GTUtility.getTierByVoltage(recipe.getEUt()));
                    return true;
                }));
    }

    @Inject(method = "addIngredientTooltips",
            at = @At(value = "INVOKE_ASSIGN",
                     target = "Ljava/util/Collection;add(Ljava/lang/Object;)Z"),
            require = 2)
    private void addTieredBoostResult(@NotNull Collection<String> tooltip,
                                      boolean notConsumed,
                                      boolean input,
                                      @Nullable Object ingredient,
                                      @Nullable Object ingredient2,
                                      CallbackInfo ci,
                                      @Local(name = "chance") double chance,
                                      @Local(name = "boost") double boost) {
        // Add the total chance to the tooltip
        if (OCDisplayExtension.cast(recipeMap).sus$hasOCButton()) {
            int tierDifference = SusUtil.getTierDifference(recipe.getEUt(), sus$tier);

            // The total chance may or may not max out at 100%.
            // TODO possibly change in the future.
            // TODO: check this; better format
            double totalChance = Math.min(chance + boost * tierDifference, 100);
            tooltip.add(I18n.format("sussypatches.jei.recipe.chance_total.tooltip",
                    GTValues.VNF[sus$tier], totalChance));
        }
    }

    // TODO: use more robust injection point & injector
    @Redirect(method = "drawInfo",
              at = @At(value = "INVOKE",
                       target = "Ljava/lang/Long;valueOf(J)Ljava/lang/Long;",
                       ordinal = 1,
                       remap = true))
    private Long useOCResult(long ignored) {
        var pair = SusUtil.getOCResult(recipeMap, recipe, sus$tier);
        long volt = pair.getLeft();
        long dura = pair.getRight();
        return volt * dura;
    }

    @ModifyExpressionValue(method = "drawInfo",
                           at = { // spotless:off
                                   @At(ordinal = 1, target = "Lgregtech/api/recipes/Recipe;getEUt()I", value = "INVOKE"),
                                   @At(ordinal = 2, target = "Lgregtech/api/recipes/Recipe;getEUt()I", value = "INVOKE"),
                                   @At(ordinal = 3, target = "Lgregtech/api/recipes/Recipe;getEUt()I", value = "INVOKE")
                           },  // spotless:on
                           require = 3)
    private int useOCResult(int ignored) {
        return SusUtil.getOCResult(recipeMap, recipe, sus$tier).getLeft();
    }

    @ModifyExpressionValue(method = "drawInfo",
                           at = @At(value = "INVOKE",
                                    target = "Lgregtech/api/recipes/Recipe;getDuration()I",
                                    ordinal = 1))
    private int useOCResult2(int ignored) {
        return SusUtil.getOCResult(recipeMap, recipe, sus$tier).getRight();
    }
}
