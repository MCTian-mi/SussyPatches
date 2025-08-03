package dev.tianmi.sussypatches.api.metatileentity;

import net.minecraft.block.state.IBlockState;

import org.jetbrains.annotations.Nullable;

import gregtech.api.metatileentity.multiblock.IMultiblockPart;

public interface IConnectable {

    /// Null for self, same as [MultiblockControllerBase#getBaseTexture(IMultiblockPart)]
    ///
    /// @see BlockMachineMixin
    @Nullable
    default IBlockState getVisualState(@Nullable IMultiblockPart part) {
        return null;
    }
}
