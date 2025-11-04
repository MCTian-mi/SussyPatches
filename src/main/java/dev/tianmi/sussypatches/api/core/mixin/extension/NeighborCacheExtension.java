package dev.tianmi.sussypatches.api.core.mixin.extension;

import java.lang.ref.WeakReference;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

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

    // Helper method from brachy
    static boolean isAdjacentChunkUnloaded(World world, BlockPos pos, EnumFacing facing) {
        int x = pos.getX(), z = pos.getZ();
        int chunkX = x >> 4, chunkZ = z >> 4;
        int nearbyChunkX = (x + facing.getXOffset()) >> 4, nearbyChunkZ = (z + facing.getZOffset()) >> 4;

        if (chunkX == nearbyChunkX && chunkZ == nearbyChunkZ) return false; // Within the same chunk, no need to check
        IChunkProvider chunkProvider = world.getChunkProvider();
        return chunkProvider.getLoadedChunk(nearbyChunkX, nearbyChunkZ) == null;
    }
}
