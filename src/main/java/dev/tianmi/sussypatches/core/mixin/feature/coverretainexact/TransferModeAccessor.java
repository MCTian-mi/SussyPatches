package dev.tianmi.sussypatches.core.mixin.feature.coverretainexact;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import gregtech.common.covers.TransferMode;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2684")
@Mixin(value = TransferMode.class, remap = false)
public interface TransferModeAccessor {

    @Invoker("<init>")
    static TransferMode create(String enumName, int ordinal, String localeName, int maxStackSize) {
        throw new IllegalStateException("Unreachable");
    }
}
