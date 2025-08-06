package dev.tianmi.sussypatches.core.mixin.tweak.xoshiro256plusplus;

import java.util.Random;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import dev.tianmi.sussypatches.shadow.it.unimi.dsi.util.XoShiRo256PlusPlusRandom;
import gregtech.api.GTValues;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2747")
@Mixin(value = GTValues.class, remap = false)
public abstract class GTValuesMixin {

    @WrapOperation(method = "<clinit>",
                   at = @At(value = "FIELD",
                            target = "Lgregtech/api/GTValues;RNG:Ljava/util/Random;",
                            opcode = Opcodes.PUTSTATIC))
    private static void useXoShiRo256plusplusRandom(Random xstr, Operation<Void> insn) {
        insn.call(new XoShiRo256PlusPlusRandom());
    }
}
