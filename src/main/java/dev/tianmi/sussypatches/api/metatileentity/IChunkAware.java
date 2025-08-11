package dev.tianmi.sussypatches.api.metatileentity;

import net.minecraft.util.EnumFacing;

import org.jetbrains.annotations.ApiStatus;

import dev.tianmi.sussypatches.api.core.mixin.extension.ChunkAwareExtension;

@ApiStatus.AvailableSince("0.8.0")
public interface IChunkAware extends ChunkAwareExtension {

    @Override
    void onNeighborChunkLoad(EnumFacing side);

    @Override
    void onNeighborChunkUnload(EnumFacing side);
}
