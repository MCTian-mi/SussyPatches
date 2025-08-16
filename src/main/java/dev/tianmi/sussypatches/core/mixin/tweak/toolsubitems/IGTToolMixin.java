package dev.tianmi.sussypatches.core.mixin.tweak.toolsubitems;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.tianmi.sussypatches.api.annotation.Transformer;
import dev.tianmi.sussypatches.api.core.mixin.extension.GTToolExtension;
import dev.tianmi.sussypatches.api.util.CEuNonSense;
import dev.tianmi.sussypatches.core.asm.transformer.IGTToolTransformer;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.unification.material.Material;
import gregtech.common.items.ToolItems;

@Transformer(clazz = IGTToolTransformer.class)
@Mixin(value = IGTTool.class, remap = false)
public interface IGTToolMixin extends GTToolExtension {

    @Shadow
    boolean isElectric();

    @Shadow
    ItemStack get(Material material);

    @Shadow
    ItemStack get(Material material, long defaultMaxCharge);

    @Inject(method = "definition$getSubItems", at = @At("TAIL"))
    @SuppressWarnings("EqualsBetweenInconvertibleTypes")
    default void addAllMaterialSubtypes(@NotNull NonNullList<ItemStack> items, CallbackInfo ci) {
        // Fuck you CEu for making special cases
        if (this == ToolItems.SOFT_MALLET || this == ToolItems.PLUNGER) {
            CEuNonSense.handleToolSpecialCases((IGTTool) this, items);
            return;
        }

        final boolean electric = isElectric();
        for (Material material : sus$getMaterials()) {
            ItemStack subItem = electric ? get(material, Integer.MAX_VALUE) : get(material);
            items.add(subItem);
        }
    }
}
