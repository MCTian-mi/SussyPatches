package dev.tianmi.sussypatches.core.mixin.bugfix.relativedirection;

import net.minecraft.util.EnumFacing;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import gregtech.api.util.RelativeDirection;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2848")
@Mixin(value = RelativeDirection.class, remap = false)
public class RelativeDirectionMixin {

    @ModifyExpressionValue(method = "getRelativeFacing",
                           at = @At(value = "INVOKE",
                                    target = "Lnet/minecraft/util/EnumFacing;rotateY()Lnet/minecraft/util/EnumFacing;",
                                    remap = true),
                           slice = @Slice(from = @At(value = "FIELD",
                                                     target = "Lnet/minecraft/util/EnumFacing$Axis;Y:Lnet/minecraft/util/EnumFacing$Axis;",
                                                     opcode = Opcodes.GETSTATIC,
                                                     ordinal = 2,
                                                     remap = true),
                                          to = @At(value = "FIELD",
                                                   target = "Lgregtech/api/util/RelativeDirection$1;$SwitchMap$net$minecraft$util$EnumFacing:[I",
                                                   opcode = Opcodes.GETSTATIC,
                                                   ordinal = 2)))
    private EnumFacing distinguishUpDown(EnumFacing original, @Local(name = "frontFacing") EnumFacing frontFacing) {
        return frontFacing == EnumFacing.DOWN ? original.getOpposite() : original;
    }

    @ModifyExpressionValue(method = "getRelativeFacing",
                           at = @At(value = "INVOKE",
                                    target = "Lnet/minecraft/util/EnumFacing;rotateYCCW()Lnet/minecraft/util/EnumFacing;",
                                    remap = true),
                           slice = @Slice(from = @At(value = "FIELD",
                                                     target = "Lnet/minecraft/util/EnumFacing$Axis;Y:Lnet/minecraft/util/EnumFacing$Axis;",
                                                     opcode = Opcodes.GETSTATIC,
                                                     ordinal = 3,
                                                     remap = true),
                                          to = @At(value = "FIELD",
                                                   target = "Lgregtech/api/util/RelativeDirection$1;$SwitchMap$net$minecraft$util$EnumFacing:[I",
                                                   opcode = Opcodes.GETSTATIC,
                                                   ordinal = 3)))
    private EnumFacing distinguishDownUp(EnumFacing original, @Local(name = "frontFacing") EnumFacing frontFacing) {
        return frontFacing == EnumFacing.DOWN ? original.getOpposite() : original;
    }
}
