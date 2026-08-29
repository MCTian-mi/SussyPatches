package dev.tianmi.sussypatches.core.mixin.compat.prjrscrewdriver;

import gregtech.api.items.toolitem.IGTTool;
import mrtjp.projectred.api.IScrewdriver;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = IGTTool.class, remap = false)
public interface IGTToolMixin extends IScrewdriver {
}
