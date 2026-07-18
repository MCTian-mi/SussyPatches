package dev.tianmi.sussypatches.core.mixin.bugfix.weakneighborref;

import dev.tianmi.sussypatches.api.core.mixin.extension.NeighborCacheExtension;
import dev.tianmi.sussypatches.common.helper.ChunkTracker;
import gregtech.api.metatileentity.NeighborCacheTileEntityBase;
import gregtech.api.metatileentity.SyncedTileEntityBase;
import gregtech.api.metatileentity.interfaces.INeighborCache;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;

@NullMarked
@Mixin(value = NeighborCacheTileEntityBase.class, remap = false)
public abstract class NeighborCacheTileEntityBaseMixin extends SyncedTileEntityBase
                                                       implements NeighborCacheExtension, INeighborCache {

    @Mutable
    @Final
    @Shadow
    private TileEntity[] neighbors;

    @Shadow
    private boolean neighborsInvalidated;

    @Unique
    private final List<WeakReference<TileEntity>> sus$neighbors = Arrays.asList(INVALID, INVALID, INVALID,
            INVALID, INVALID, INVALID);

    @Unique
    private final long[] sus$generations = {NO_GENERATION, NO_GENERATION, NO_GENERATION,
            NO_GENERATION, NO_GENERATION, NO_GENERATION};

    @SuppressWarnings("DataFlowIssue")
    @Inject(method = "<init>", at = @At("TAIL"))
    private void clearOriginalImpl(CallbackInfo ci) {
        this.neighbors = null;
    }

    /// This is a hard rewrite, any conflict should result in a hard crash
    @Redirect(method = "invalidateNeighbors",
              at = @At(value = "INVOKE",
                       target = "Ljava/util/Arrays;fill([Ljava/lang/Object;Ljava/lang/Object;)V"))
    private void invalidateWeakRefs(Object[] $null, Object $this) {
        for (EnumFacing facing : EnumFacing.values()) sus$invalidate(facing);
    }

    /// @author Ghzdude, Tian_mi
    /// @reason This is a hard rewrite, any conflict should result in a hard crash
    @Nullable
    @Override
    @Overwrite
    public TileEntity getNeighbor(EnumFacing facing) {
        if (world == null || pos == null) return null;
        if (NeighborCacheExtension.crossesChunk(pos, facing)) return sus$getCrossChunkNeighbor(facing);
        return NeighborCacheExtension.resolve(sus$getRef(facing), () -> sus$computeNeighbor(facing).get());
    }

    /// @author Ghzdude, Tian_mi
    /// @reason This is a hard rewrite, any conflict should result in a hard crash
    @Override
    @Overwrite
    public void onNeighborChanged(EnumFacing facing) {
        sus$invalidate(facing);
    }

    /// Invalidate cache and generation
    @Unique
    @Override
    public void sus$invalidate(EnumFacing facing) {
        int index = facing.getIndex();
        sus$neighbors.set(index, INVALID);
        sus$generations[index] = NO_GENERATION;
    }

    @Unique
    @Override
    public WeakReference<TileEntity> sus$computeNeighbor(EnumFacing facing) {
        int index = facing.getIndex();
        if (!NeighborCacheExtension.crossesChunk(pos, facing)) {
            TileEntity te = super.getNeighbor(facing);
            var ref = NeighborCacheExtension.asReference(te);
            sus$neighbors.set(index, ref);
            sus$generations[index] = NO_GENERATION;
            neighborsInvalidated = false;
            return ref;
        }

        // FIXME))
        sus$getCrossChunkNeighbor(facing);
        return sus$getRef(facing);
    }

    @Unique
    @Nullable
    private TileEntity sus$getCrossChunkNeighbor(EnumFacing facing) {
        int targetChunkX = (pos.getX() + facing.getXOffset()) >> 4;
        int targetChunkZ = (pos.getZ() + facing.getZOffset()) >> 4;
        var snapshot = ChunkTracker.get(world, targetChunkX, targetChunkZ);
        if (snapshot == null) { // Chunk not loaded, marks as invalid and returns null
            sus$invalidate(facing);
            return null;
        }
        int index = facing.getIndex();
        if (sus$generations[index] != snapshot.generation()) { // Chunk unloaded between accesses, marks as invalid
            sus$neighbors.set(index, INVALID);
        }
        return NeighborCacheExtension.resolve(sus$getRef(facing), () -> sus$computeCrossChunkNeighbor(facing, snapshot));
    }

    /// Re-fetch a cross-chunk neighbor, and updates both [sus$neighbors] & [sus$generations]
    @Unique
    @Nullable
    private TileEntity sus$computeCrossChunkNeighbor(EnumFacing facing, ChunkTracker.Snapshot<Chunk> snapshot) {
        int index = facing.getIndex();
        sus$generations[index] = snapshot.generation(); // Sync generation
        var te = snapshot.chunk().getTileEntity(pos.offset(facing), Chunk.EnumCreateEntityType.CHECK);
        if (te == null) {
            sus$neighbors.set(index, NULL);
            neighborsInvalidated = false;
            return null;
        }
        if (te.isInvalid()) {
            sus$neighbors.set(index, INVALID);
            neighborsInvalidated = false;
            return null;
        }
        var ref = NeighborCacheExtension.asReference(te);
        sus$neighbors.set(index, ref);
        neighborsInvalidated = false;
        return te;
    }

    @Unique
    @Override
    public WeakReference<TileEntity> sus$getRef(EnumFacing facing) {
        return sus$neighbors.get(facing.getIndex());
    }
}
