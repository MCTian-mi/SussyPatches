package dev.tianmi.sussypatches.core.mixin.feature.connectedtextures;

import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityCleaningMaintenanceHatch;
import org.spongepowered.asm.mixin.Mixin;

// I hate special cases...
@Mixin(value = MetaTileEntityCleaningMaintenanceHatch.class, remap = false)
public abstract class CleaningMaintenanceHatchMixin extends MultiblockPartsMixin {
}
