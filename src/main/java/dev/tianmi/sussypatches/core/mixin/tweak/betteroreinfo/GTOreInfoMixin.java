package dev.tianmi.sussypatches.core.mixin.tweak.betteroreinfo;

import static gregtech.integration.jei.utils.JEIResourceDepositCategoryUtils.getAllRegisteredDimensions;

import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.tianmi.sussypatches.common.helper.DimDisplayRegistry;
import gregtech.api.worldgen.config.OreDepositDefinition;
import gregtech.integration.jei.basic.GTOreInfo;

@Mixin(value = GTOreInfo.class, remap = false)
public abstract class GTOreInfoMixin {

    @Final
    @Shadow
    private OreDepositDefinition definition;

    @Final
    @Shadow
    private List<List<ItemStack>> groupedInputsAsItemStacks;

    @Final
    @Shadow
    private List<List<ItemStack>> groupedOutputsAsItemStacks;

    @Inject(method = "<init>",
            at = @At(value = "INVOKE_ASSIGN",
                     target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
                     ordinal = 1))
    public void addDisplayItems(CallbackInfo ci) {
        for (int dimId : getAllRegisteredDimensions(definition.getDimensionFilter())) {
            var displayStack = DimDisplayRegistry.getDisplayItem(dimId);
            if (!displayStack.isEmpty()) {
                groupedInputsAsItemStacks.add(Collections.singletonList(displayStack));
            }
        }
    }

    @Inject(method = "createOreWeightingTooltip", at = @At("HEAD"), cancellable = true)
    public void skipForDimDisplayItems(int slotIndex, CallbackInfoReturnable<List<String>> cir) {
        if (slotIndex >= groupedOutputsAsItemStacks.size() + 2) {
            cir.setReturnValue(Collections.emptyList());
        }
    }
}
