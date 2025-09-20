package dev.tianmi.sussypatches.core.mixin.feature.coverretainexact;

import static dev.tianmi.sussypatches.api.core.mixin.extension.TransferModeExtension.RETAIN_EXACT;

import java.util.Map;

import net.minecraftforge.items.IItemHandler;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import gregtech.common.covers.CoverConveyor;
import gregtech.common.covers.CoverRoboticArm;
import gregtech.common.covers.TransferMode;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2684")
@Mixin(value = CoverRoboticArm.class, remap = false)
public abstract class RoboticArmMixin extends CoverConveyor {

    @Shadow
    protected TransferMode transferMode;

    @Shadow
    protected abstract int doKeepExact(IItemHandler itemHandler, IItemHandler myItemHandler, int maxTransferAmount);

    // Dummy
    @SuppressWarnings("DataFlowIssue")
    RoboticArmMixin() {
        super(null, null, null, 0, 0);
    }

    @Inject(method = "doTransferItems",
            at = @At(value = "FIELD",
                     target = "Lgregtech/common/covers/CoverRoboticArm$1;$SwitchMap$gregtech$common$covers$TransferMode:[I",
                     opcode = Opcodes.GETSTATIC),
            cancellable = true)
    private void retainExactGuard(IItemHandler itemHandler, IItemHandler myItemHandler, int maxTransferAmount,
                                  CallbackInfoReturnable<Integer> cir) {
        if (this.transferMode == RETAIN_EXACT) {
            cir.setReturnValue(doKeepExact(itemHandler, myItemHandler, maxTransferAmount));
        }
    }

    @WrapOperation(method = "doKeepExact",
                   at = @At(value = "INVOKE",
                            target = "Ljava/util/Map;containsKey(Ljava/lang/Object;)Z"))
    private boolean retainExactItemAmount(Map<Object, GroupItemInfo> currentItemAmount, Object filterSlotIndex,
                                          Operation<Boolean> method,
                                          @Local(name = "itemAmount") LocalIntRef itemAmountRef,
                                          @Local(name = "sourceItemAmounts") Map<Object, GroupItemInfo> sourceItemAmounts) {
        if (this.transferMode == RETAIN_EXACT) {
            if (sourceItemAmounts.containsKey(filterSlotIndex)) {
                itemAmountRef.set(sourceItemAmounts.get(filterSlotIndex).totalCount);
            }
            return false; // Skip the original logic
        } else {
            return method.call(currentItemAmount, filterSlotIndex);
        }
    }

    @Definition(id = "itemAmount", local = @Local(type = int.class, name = "itemAmount"))
    @Definition(id = "itemToKeepAmount", local = @Local(type = int.class, name = "itemToKeepAmount"))
    @Expression("itemAmount < itemToKeepAmount")
    @ModifyExpressionValue(method = "doKeepExact", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean retainExactSetCount(boolean original) {
        return (this.transferMode == RETAIN_EXACT) != original;
    }

    @WrapOperation(method = "doKeepExact",
                   at = @At(value = "FIELD",
                            target = "Lgregtech/common/covers/CoverConveyor$GroupItemInfo;totalCount:I",
                            opcode = Opcodes.PUTFIELD))
    private void retainExactNegateCount(GroupItemInfo sourceInfo, int original, Operation<Void> insn) {
        insn.call(sourceInfo, this.transferMode == RETAIN_EXACT ? -original : original);
    }
}
