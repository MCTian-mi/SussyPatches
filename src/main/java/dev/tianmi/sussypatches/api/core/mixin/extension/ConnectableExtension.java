package dev.tianmi.sussypatches.api.core.mixin.extension;

import net.minecraft.block.state.IBlockState;

import org.jetbrains.annotations.Nullable;

import dev.tianmi.sussypatches.api.annotation.MixinExtension;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;

@MixinExtension({
        MultiblockControllerBase.class,
        MetaTileEntityMultiblockPart.class,
})
public interface ConnectableExtension {

    /// Null for self, same as [MultiblockControllerBase#getBaseTexture(IMultiblockPart)]
    ///
    /// @see BlockMachineMixin
    @Nullable
    default IBlockState sus$getVisualState(@Nullable IMultiblockPart part) {
        return null;
    }
}
