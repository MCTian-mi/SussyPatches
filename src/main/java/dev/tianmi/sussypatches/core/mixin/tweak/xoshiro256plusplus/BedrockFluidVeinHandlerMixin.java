package dev.tianmi.sussypatches.core.mixin.tweak.xoshiro256plusplus;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalLongRef;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import dev.tianmi.sussypatches.shadow.it.unimi.dsi.util.XoShiRo256PlusPlusRandom;
import gregtech.api.worldgen.bedrockFluids.BedrockFluidVeinHandler;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2747")
@Mixin(value = BedrockFluidVeinHandler.class, remap = false)
public abstract class BedrockFluidVeinHandlerMixin {

    // This is a hard rewrite, any conflict should result in a hard crash
    @ModifyArg(method = "getFluidVeinWorldEntry",
               at = @At(value = "INVOKE",
                        target = "Lgregtech/api/util/XSTR;<init>(J)V"))
    private static long recordSeed(long seed, @Share("seed") LocalLongRef seedRef) {
        seedRef.set(seed);
        return 0;
    }

    @ModifyReceiver(method = "getFluidVeinWorldEntry",
                    at = @At(value = "INVOKE",
                             target = "Ljava/util/Random;nextInt(I)I"))
    private static Random useXoShiRo256plusplusRandom(Random xstr, int i, @Share("seed") LocalLongRef seedRef) {
        return new XoShiRo256PlusPlusRandom(seedRef.get());
    }
}
