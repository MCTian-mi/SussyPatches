package dev.tianmi.sussypatches.api.core.mixin.extension;

import net.minecraft.util.EnumFacing;

import dev.tianmi.sussypatches.api.annotation.MixinExtension;
import gregtech.api.metatileentity.NeighborCacheTileEntityBase;

/// Use [IChunkAware] for your own TEs!
@MixinExtension(NeighborCacheTileEntityBase.class)
public interface ChunkAwareExtension {

    static ChunkAwareExtension cast(NeighborCacheTileEntityBase neighborCache) {
        return (ChunkAwareExtension) neighborCache;
    }

    default void onNeighborChunkLoad(EnumFacing side) {
        ((NeighborCacheTileEntityBase) this).onNeighborChanged(side);
    }

    default void onNeighborChunkUnload(EnumFacing side) {
        ((NeighborCacheTileEntityBase) this).onNeighborChanged(side);
    }
}
