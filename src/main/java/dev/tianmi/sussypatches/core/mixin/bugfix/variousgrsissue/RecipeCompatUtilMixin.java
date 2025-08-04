package dev.tianmi.sussypatches.core.mixin.bugfix.variousgrsissue;

import java.util.Objects;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import dev.tianmi.sussypatches.api.util.SusUtil;
import gregtech.api.GTValues;
import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.api.unification.material.Material;
import gregtech.integration.RecipeCompatUtil;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2785")
@Mixin(value = RecipeCompatUtil.class, remap = false)
public abstract class RecipeCompatUtilMixin {

    @ModifyReturnValue(method = "getMetaItemId", at = @At(value = "RETURN", ordinal = 0))
    private static String addNameSpace(String name, @Local(name = "metaValueItem") MetaValueItem metaValueItem) {
        String namespace = Objects.requireNonNull(metaValueItem.getMetaItem().getRegistryName()).getNamespace();
        if (namespace.equals(GTValues.MODID)) {
            return name;
        }
        return namespace + ":" + name;
    }

    @WrapOperation(method = "getMetaItemId",
                   at = @At(value = "INVOKE",
                            target = "Lgregtech/api/unification/material/Material;toCamelCaseString()Ljava/lang/String;"),
                   require = 3)
    private static String addNameSpace(Material mat, Operation<String> method) {
        return SusUtil.getPrefix(mat) + method.call(mat);
    }
}
