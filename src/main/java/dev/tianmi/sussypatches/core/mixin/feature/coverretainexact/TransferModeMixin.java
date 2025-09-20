package dev.tianmi.sussypatches.core.mixin.feature.coverretainexact;

import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import dev.tianmi.sussypatches.api.core.mixin.extension.TransferModeExtension;
import gregtech.common.covers.TransferMode;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2684")
@Mixin(value = TransferMode.class, remap = false)
public abstract class TransferModeMixin implements TransferModeExtension {

    @Final
    @Mutable
    @Shadow
    private static TransferMode[] $VALUES;

    static {
        $VALUES = ArrayUtils.add($VALUES, RETAIN_EXACT);
    }
}
