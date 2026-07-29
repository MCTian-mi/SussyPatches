package dev.tianmi.sussypatches.core.mixin.bugfix.syncpipeproperties;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import dev.tianmi.sussypatches.api.util.SusUtil;
import gregtech.api.items.metaitem.FilteredFluidStats;
import gregtech.common.items.MetaItem1;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(value = MetaItem1.class, remap = false)
public abstract class MetaItem1Mixin {

    @Redirect(method = "registerSubItems",
              at = @At(value = "NEW",
                       target = "(IIZZZZZ)Lgregtech/api/items/metaitem/FilteredFluidStats;"),
              slice = @Slice(
                      from = @At(value = "FIELD", target = "Lgregtech/common/items/MetaItems;SPRAY_SOLVENT:Lgregtech/api/items/metaitem/MetaItem$MetaValueItem;", opcode = Opcodes.PUTSTATIC),
                      to = @At(value = "FIELD", target = "Lgregtech/common/items/MetaItems;TOOL_MATCHES:Lgregtech/api/items/metaitem/MetaItem$MetaValueItem;", opcode = Opcodes.PUTSTATIC)),
              require = 8)
    private FilteredFluidStats useMaterialProperties(int capacity, int maxFluidTemperature, boolean gasProof,
                                                     boolean acidProof, boolean cryoProof, boolean plasmaProof,
                                                     boolean allowPartialFill, @Share("index") LocalIntRef index) {
        int ordinal = index.get();
        index.set(ordinal + 1);
        return SusUtil.modifyFiltersByOrdinal(capacity, maxFluidTemperature, allowPartialFill, ordinal);
    }
}
