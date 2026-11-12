package dev.tianmi.sussypatches.core.mixin.bugfix.buildererrorserver;

import java.util.List;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import gregtech.api.pattern.PatternError;
import gregtech.api.pattern.PatternStringError;
import gregtech.api.pattern.TraceabilityPredicate.SinglePredicateError;
import gregtech.api.util.LocalizationUtils;
import gregtech.common.items.behaviors.MultiblockBuilderBehavior;

/// The Multiblock Builder prints a structure error with `PatternError#getErrorInfo`, on the server.
/// That method is client-only (and its overrides use the client-only `I18n`), so on a dedicated server
/// the call fails right after the header: players see "Potential structure error:" and nothing else.
///
/// The same text is built here with [LocalizationUtils], which works on both sides.
@Mixin(value = MultiblockBuilderBehavior.class, remap = false)
public abstract class MultiblockBuilderBehaviorMixin {

    @Redirect(method = "onItemUseFirst",
              at = @At(value = "INVOKE",
                       target = "Lgregtech/api/pattern/PatternError;getErrorInfo()Ljava/lang/String;"))
    private String sideSafeErrorInfo(PatternError error) {
        if (error instanceof PatternStringError stringError) {
            return LocalizationUtils.format(stringError.translateKey);
        }
        if (error instanceof SinglePredicateError limited) {
            int number = switch (limited.type) {
                case 0 -> limited.predicate.maxGlobalCount;
                case 1 -> limited.predicate.minGlobalCount;
                case 2 -> limited.predicate.maxLayerCount;
                case 3 -> limited.predicate.minLayerCount;
                default -> -1;
            };
            return LocalizationUtils.format("gregtech.multiblock.pattern.error.limited." + limited.type, number);
        }
        StringBuilder builder = new StringBuilder();
        for (List<ItemStack> candidate : error.getCandidates()) {
            if (!candidate.isEmpty()) {
                builder.append(candidate.get(0).getDisplayName());
                builder.append(", ");
            }
        }
        builder.append("...");
        return LocalizationUtils.format("gregtech.multiblock.pattern.error", builder.toString(), error.getPos());
    }
}
