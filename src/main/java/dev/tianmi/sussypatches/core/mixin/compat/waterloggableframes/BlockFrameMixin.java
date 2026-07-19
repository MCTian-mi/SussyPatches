package dev.tianmi.sussypatches.core.mixin.compat.waterloggableframes;

import dev.tianmi.sussypatches.api.annotation.Compat;
import dev.tianmi.sussypatches.api.util.SusMods;
import git.jbredwards.fluidlogged_api.api.block.IFluidloggable;
import gregtech.common.blocks.BlockFrame;
import org.spongepowered.asm.mixin.Mixin;

@Compat(mods = SusMods.FluidloggedAPI_3)
@Mixin(value = BlockFrame.class, remap = false)
public abstract class BlockFrameMixin implements IFluidloggable {
    // Default implementation should be good enough
}
