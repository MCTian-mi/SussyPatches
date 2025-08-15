package dev.tianmi.sussypatches.core.mixin.tweak.toolsubitems;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.tianmi.sussypatches.api.core.mixin.extension.GTToolExtension;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.loaders.recipe.handlers.ToolRecipeHandler;

@Mixin(value = ToolRecipeHandler.class, remap = false)
public class ToolRecipeHandlerMixin {

    @Inject(method = "addToolRecipe", at = @At("TAIL"))
    private static void registerMaterial(Material material, IGTTool tool,
                                         boolean mirrored, Object[] recipe,
                                         CallbackInfo ci) {
        GTToolExtension.addMaterial(tool, material);
    }

    @Inject(method = "addElectricToolRecipe", at = @At("TAIL"))
    private static void registerMaterial(OrePrefix toolHead, Material material,
                                         IGTTool[] toolItems, CallbackInfo ci) {
        for (IGTTool toolItem : toolItems) {
            GTToolExtension.addMaterial(toolItem, material);
        }
    }

    // Fuck you CEu
    @Inject(method = "addElectricWirecutterRecipe", at = @At("TAIL"))
    private static void registerMaterial(Material material, IGTTool[] toolItems,
                                         CallbackInfo ci) {
        for (IGTTool toolItem : toolItems) {
            GTToolExtension.addMaterial(toolItem, material);
        }
    }
}
