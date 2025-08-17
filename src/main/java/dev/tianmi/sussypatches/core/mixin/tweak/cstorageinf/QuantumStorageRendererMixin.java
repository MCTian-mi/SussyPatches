package dev.tianmi.sussypatches.core.mixin.tweak.cstorageinf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import dev.tianmi.sussypatches.common.SusConfig;
import gregtech.client.renderer.texture.custom.QuantumStorageRenderer;

@Mixin(value = QuantumStorageRenderer.class, remap = false)
public abstract class QuantumStorageRendererMixin {

    @WrapOperation(method = "renderAmountText",
                   at = @At(value = "INVOKE",
                            target = "Lgregtech/api/util/TextFormattingUtil;formatLongToCompactString(JI)Ljava/lang/String;"))
    private static String returnCustomString(long value, int precision, Operation<String> method) {
        if (value == -1) return SusConfig.TWEAK.cStorageInf;
        return method.call(value, precision);
    }
}
