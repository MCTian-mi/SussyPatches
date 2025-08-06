package dev.tianmi.sussypatches.core.mixin.tweak.xoshiro256plusplus;

import java.util.Random;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalLongRef;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import dev.tianmi.sussypatches.shadow.it.unimi.dsi.util.XoShiRo256PlusPlusRandom;
import gregtech.api.worldgen.generator.CachedGridEntry;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2747")
@Mixin(value = CachedGridEntry.class, remap = false)
public abstract class CachedGridEntryMixin {

    // This is a hard rewrite, any conflict should result in a hard crash
    @ModifyArg(method = "<init>",
               at = @At(value = "INVOKE",
                        target = "Lgregtech/api/util/XSTR;<init>(J)V"))
    private long recordSeed(long seed, @Share("seed") LocalLongRef seedRef) {
        seedRef.set(seed);
        return 0;
    }

    @WrapOperation(method = "<init>",
                   at = @At(value = "FIELD",
                            target = "Lgregtech/api/worldgen/generator/CachedGridEntry;gridRandom:Ljava/util/Random;",
                            opcode = Opcodes.PUTFIELD))
    private void useXoShiRo256plusplusRandom(CachedGridEntry self, Random xstr, Operation<Void> insn,
                                             @Share("seed") LocalLongRef seedRef) {
        insn.call(self, new XoShiRo256PlusPlusRandom(seedRef.get()));
    }
}
