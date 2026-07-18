package dev.tianmi.sussypatches.api.core.mixin.extension;

import dev.tianmi.sussypatches.api.annotation.MixinExtension;
import gregtech.api.metatileentity.NeighborCacheTileEntityBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.function.Supplier;

@NullMarked
@MixinExtension(NeighborCacheTileEntityBase.class)
public interface NeighborCacheExtension {

    long NO_GENERATION = Long.MIN_VALUE;
    @SuppressWarnings("DataFlowIssue")
    WeakReference<TileEntity> NULL = new WeakReference<>(null);
    @SuppressWarnings("DataFlowIssue")
    WeakReference<TileEntity> INVALID = new WeakReference<>(null);

    static NeighborCacheExtension cast(NeighborCacheTileEntityBase neighborCache) {
        return (NeighborCacheExtension) neighborCache;
    }

    @Nullable
    static TileEntity resolve(WeakReference<TileEntity> reference, Supplier<@Nullable TileEntity> supplier) {
        if (reference == INVALID) return supplier.get(); // Refetch
        if (reference == NULL) return null;
        TileEntity tileEntity = reference.get();
        if (tileEntity == null || tileEntity.isInvalid()) { // Tile unloaded
            return supplier.get();
        }
        return tileEntity;
    }

    WeakReference<TileEntity> sus$computeNeighbor(EnumFacing facing);

    WeakReference<TileEntity> sus$getRef(EnumFacing facing);

    static boolean crossesChunk(BlockPos pos, EnumFacing facing) {
        BlockPos neighbor = pos.offset(facing);
        return (pos.getX() >> 4) != (neighbor.getX() >> 4)
                || (pos.getZ() >> 4) != (neighbor.getZ() >> 4);
    }

    static WeakReference<TileEntity> asReference(@Nullable TileEntity tileEntity) {
        return tileEntity == null ? NULL : new WeakReference<>(tileEntity);
    }

    void sus$invalidate(EnumFacing facing);
}
