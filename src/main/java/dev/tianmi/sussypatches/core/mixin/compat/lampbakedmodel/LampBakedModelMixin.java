package dev.tianmi.sussypatches.core.mixin.compat.lampbakedmodel;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.registry.IRegistry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;

import dev.tianmi.sussypatches.api.annotation.Compat;
import dev.tianmi.sussypatches.api.util.SusMods;
import gregtech.client.model.lamp.LampBakedModel;
import team.chisel.ctm.client.model.AbstractCTMBakedModel;

@Compat(mods = SusMods.VintageFix)
@Mixin(value = LampBakedModel.class, remap = false)
public abstract class LampBakedModelMixin {

    @WrapWithCondition(method = "onModelBake",
                       at = @At(value = "INVOKE",
                                target = "Lnet/minecraft/util/registry/IRegistry;putObject(Ljava/lang/Object;Ljava/lang/Object;)V",
                                remap = true))
    private static boolean skipIfCTMModel(IRegistry<ModelResourceLocation, IBakedModel> modelRegistry,
                                          Object key, Object value, @Local(name = "model") IBakedModel model) {
        return !(model instanceof AbstractCTMBakedModel);
    }
}
