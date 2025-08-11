package dev.tianmi.sussypatches.api.core.mixin.extension;

import java.lang.ref.WeakReference;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import org.jetbrains.annotations.NotNull;

import dev.tianmi.sussypatches.api.annotation.MixinExtension;
import gregtech.api.metatileentity.NeighborCacheTileEntityBase;

@MixinExtension(NeighborCacheTileEntityBase.class)
public interface NeighborCacheExtension {

    WeakReference<TileEntity> NULL = new WeakReference<>(null);
    WeakReference<TileEntity> INVALID = new WeakReference<>(null);

    static NeighborCacheExtension cast(NeighborCacheTileEntityBase neighborCache) {
        return (NeighborCacheExtension) neighborCache;
    }

    boolean sus$invalidRef(EnumFacing facing);

    @NotNull
    WeakReference<TileEntity> sus$computeNeighbor(EnumFacing facing);

    @NotNull
    WeakReference<TileEntity> sus$getRef(EnumFacing facing);
}
