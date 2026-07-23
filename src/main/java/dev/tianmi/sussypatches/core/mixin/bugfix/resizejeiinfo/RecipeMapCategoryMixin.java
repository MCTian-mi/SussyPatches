package dev.tianmi.sussypatches.core.mixin.bugfix.resizejeiinfo;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.category.GTRecipeCategory;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.integration.jei.recipe.RecipeMapCategory;
import mezz.jei.api.IGuiHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RecipeMapCategory.class, remap = false)
public class RecipeMapCategoryMixin {

    @Shadow
    @Final
    private ModularUI modularUI;

    @Inject(method = "<init>",
            at = @At(value = "INVOKE", target = "Lgregtech/api/gui/ModularUI;initWidgets()V", shift = At.Shift.AFTER))
    private void sus$calculateUISize(RecipeMap<?> recipeMap,
                                     GTRecipeCategory category,
                                     IGuiHelper guiHelper,
                                     CallbackInfo ci,
                                     @Share("width") LocalIntRef width,
                                     @Share("height") LocalIntRef height) {
        int maxRight = 0;
        int maxHeight = 0;

        for (Widget widget : modularUI.guiWidgets.values()) {
            Position pos = widget.getPosition();
            Size size = widget.getSize();
            maxRight = Math.max(maxRight, pos.x + size.width);
            maxHeight = Math.max(maxHeight, pos.y + size.height);
        }

        width.set(Math.max(modularUI.getWidth(), maxRight));
        height.set(Math.max(modularUI.getHeight(), maxHeight));
    }

    // This is a hard rewrite, any conflict should result in a hard crash
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lgregtech/api/gui/ModularUI;getWidth()I"))
    private int sus$useActualWidth(ModularUI ignored, @Share("width") LocalIntRef width) {
        return width.get();
    }

    // This is a hard rewrite, any conflict should result in a hard crash
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lgregtech/api/gui/ModularUI;getHeight()I"))
    private int sus$useActualHeight(ModularUI ignored, @Share("height") LocalIntRef height) {
        return height.get();
    }
}
