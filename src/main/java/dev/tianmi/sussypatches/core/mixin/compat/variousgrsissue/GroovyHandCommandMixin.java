package dev.tianmi.sussypatches.core.mixin.compat.variousgrsissue;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import dev.tianmi.sussypatches.api.annotation.Compat;
import dev.tianmi.sussypatches.api.annotation.Implemented;
import dev.tianmi.sussypatches.api.util.SusUtil;
import gregtech.api.unification.material.Material;
import gregtech.api.util.Mods;
import gregtech.integration.groovy.GroovyHandCommand;
import lombok.experimental.ExtensionMethod;

@Compat(mod = Mods.GroovyScript)
@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2785")
@Mixin(value = GroovyHandCommand.class, remap = false)
@ExtensionMethod(SusUtil.class)
public abstract class GroovyHandCommandMixin {

    @ModifyArg(method = "onHandCommand",
               at = @At(target = "Ljava/lang/StringBuilder;append(Ljava/lang/Object;)Ljava/lang/StringBuilder;",
                        value = "INVOKE",
                        ordinal = 0))
    private static Object toNameSpace(Object obj) {
        if (obj instanceof Material material) {
            return material.getPrefix() + material;
        } else throw new AssertionError("Unexpected type: " + obj.getClass());
    }
}
