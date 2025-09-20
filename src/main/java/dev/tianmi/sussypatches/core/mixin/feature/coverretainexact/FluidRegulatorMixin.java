package dev.tianmi.sussypatches.core.mixin.feature.coverretainexact;

import static dev.tianmi.sussypatches.api.core.mixin.extension.TransferModeExtension.RETAIN_EXACT;

import java.util.Map;
import java.util.function.Predicate;

import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import gregtech.common.covers.CoverFluidRegulator;
import gregtech.common.covers.CoverPump;
import gregtech.common.covers.TransferMode;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2684")
@Mixin(value = CoverFluidRegulator.class, remap = false)
public abstract class FluidRegulatorMixin extends CoverPump {

    @Shadow
    protected TransferMode transferMode;

    // Dummy
    @SuppressWarnings("DataFlowIssue")
    FluidRegulatorMixin() {
        super(null, null, null, 0, 0);
    }

    @Shadow
    protected abstract int doKeepExact(int transferLimit, IFluidHandler sourceHandler, IFluidHandler destHandler,
                                       Predicate<FluidStack> fluidFilter, int keepAmount);

    @Shadow
    protected int transferAmount;

    @Shadow
    protected abstract void setTransferAmount(int transferAmount);

    @Inject(method = "doTransferFluidsInternal",
            at = @At(value = "FIELD",
                     target = "Lgregtech/common/covers/CoverFluidRegulator$1;$SwitchMap$gregtech$common$covers$TransferMode:[I",
                     opcode = Opcodes.GETSTATIC),
            cancellable = true)
    private void retainExactGuard(IFluidHandler ignored, IFluidHandler alsoIgnored, int transferLimit,
                                  CallbackInfoReturnable<Integer> cir,
                                  @Local(name = "sourceHandler") IFluidHandler sourceHandler,
                                  @Local(name = "destHandler") IFluidHandler destHandler) {
        if (transferMode == RETAIN_EXACT) {
            cir.setReturnValue(doKeepExact(transferLimit, sourceHandler, destHandler,
                    fluidFilter::testFluidStack, transferAmount));
        }
    }

    @Definition(id = "keepAmount", local = @Local(type = int.class, name = "keepAmount"))
    @Expression("? < keepAmount")
    @ModifyExpressionValue(method = "doKeepExact", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean retainExactCheck(boolean original, @Local(name = "keepAmount") int keepAmount,
                                     @Local(name = "amountInDest") LocalIntRef amountInDestRef,
                                     @Local(name = "sourceFluids") Map<FluidStack, Integer> sourceFluids,
                                     @Local(name = "fluidStack") FluidStack fluidStack) {
        if (this.transferMode == RETAIN_EXACT) {
            int amountInDest = sourceFluids.getOrDefault(fluidStack, 0);
            amountInDestRef.set(amountInDest);
            return amountInDest > keepAmount;
        } else {
            return original;
        }
    }

    @ModifyArg(method = "doKeepExact",
               at = @At(value = "INVOKE",
                        target = "Ljava/lang/Math;min(II)I",
                        ordinal = 0),
               index = 1)
    private int retainExactMin(int original) {
        return this.transferMode == RETAIN_EXACT ? -original : original;
    }

    @Inject(method = "adjustTransferSize",
            at = @At(value = "FIELD",
                     target = "Lgregtech/common/covers/CoverFluidRegulator$1;$SwitchMap$gregtech$common$covers$TransferMode:[I",
                     opcode = Opcodes.GETSTATIC),
            cancellable = true)
    private void retainExactGuard(int amount, CallbackInfo ci) {
        if (this.transferMode == RETAIN_EXACT) {
            setTransferAmount(MathHelper.clamp(this.transferAmount + amount, 0, Integer.MAX_VALUE));
            ci.cancel();
        }
    }

    @ModifyReturnValue(method = "shouldDisplayAmountSlider", at = @At("TAIL"))
    private boolean retainExactAmountSlider(boolean original) {
        return original || this.transferMode == RETAIN_EXACT;
    }
}
