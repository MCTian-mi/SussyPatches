package dev.tianmi.sussypatches.core.mixin.bugfix.fullchunkpopulator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import gregtech.api.worldgen.populator.SurfaceRockPopulator;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2875")
@Mixin(value = SurfaceRockPopulator.class, remap = false)
public abstract class SurfaceRockPopulatorMixin {

    @Definition(id = "nextInt", method = "Ljava/util/Random;nextInt(I)I")
    @Expression("?.nextInt(8)")
    @ModifyArg(method = "populateChunk", at = @At("MIXINEXTRAS:EXPRESSION"))
    private int populateFullChunk(int _8) {
        return 16;
    }
}
