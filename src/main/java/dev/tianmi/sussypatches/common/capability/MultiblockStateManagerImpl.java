package dev.tianmi.sussypatches.common.capability;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import dev.tianmi.sussypatches.api.capability.IMultiblockStateManager;
import dev.tianmi.sussypatches.api.core.mixin.extension.PassiveStructureCheckExtension;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import lombok.val;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;

/// Transient per-world index of formed multiblock controllers. The forward indexes locate controllers affected by an
/// exact block change, a watched chunk load, or the controller's own chunk unload; [#structureInfos] owns the reverse
/// registration data needed to remove all of those entries again.
///
/// All access is expected to occur on the logical server thread. Neither registrations nor GTCEu pattern caches are
/// persisted with the world.
///
/// @param world             the world whose controllers are indexed
/// @param byPosition        controllers indexed by packed structure-block position
/// @param byWatchedChunk    controllers indexed by every chunk intersecting their cached structure
/// @param byControllerChunk controllers indexed by the chunk containing their own tile entity
/// @param structureInfos    stable, manager-owned registration snapshots indexed by controller identity
@NullMarked
public record MultiblockStateManagerImpl(
        World world,
        Multimap<Long, MultiblockControllerBase> byPosition,
        Multimap<Long, MultiblockControllerBase> byWatchedChunk,
        Multimap<Long, MultiblockControllerBase> byControllerChunk,
        Map<MultiblockControllerBase, StructureInfo> structureInfos
) implements IMultiblockStateManager {

    public MultiblockStateManagerImpl(World world) {
        this(
                world,
                Multimaps.newSetMultimap(new Long2ObjectOpenHashMap<>(), HashSet::new),
                Multimaps.newSetMultimap(new Long2ObjectOpenHashMap<>(), HashSet::new),
                Multimaps.newSetMultimap(new Long2ObjectOpenHashMap<>(), HashSet::new),
                new IdentityHashMap<>(1)
        );
    }

    /// Converts Minecraft 1.12.2's packed block-position layout directly to vanilla's packed [ChunkPos] key. Arithmetic
    /// shifts preserve the sign of the 26-bit X and Z coordinates while discarding their four intra-chunk bits.
    private static long packChunkFromPackedBlockPos(long position) {
        int chunkX = (int) (position >> 42);
        int chunkZ = (int) (position << 38 >> 42);
        return ChunkPos.asLong(chunkX, chunkZ);
    }

    @Override
    public void replaceRegistration(MultiblockControllerBase controller, LongCollection positions) {
        unregister(controller);
        if (!isValid(controller) || positions.isEmpty()) return;

        // The supplied collection is normally a live view of GTCEu's mutable pattern cache. Keep an owned snapshot so a
        // later cache rebuild cannot change this registration or prevent unregister() from removing its old index entries.
        val copiedPositions = new LongOpenHashSet(positions);
        val watchedChunks = new LongOpenHashSet();

        val copiedPosIterator = copiedPositions.iterator();
        while (copiedPosIterator.hasNext()) {
            long pos = copiedPosIterator.nextLong();
            byPosition.put(pos, controller);
            long chunkPos = packChunkFromPackedBlockPos(pos);
            if (watchedChunks.add(chunkPos)) {
                byWatchedChunk.put(chunkPos, controller);
            }
        }

        var controllerPos = controller.getPos();
        long controllerChunk = ChunkPos.asLong(controllerPos.getX() >> 4, controllerPos.getZ() >> 4);
        byControllerChunk.put(controllerChunk, controller);
        structureInfos.put(controller, new StructureInfo(copiedPositions, watchedChunks, controllerChunk));
    }

    @Override
    public void unregister(MultiblockControllerBase controller) {
        val structureInfo = structureInfos.remove(controller);
        if (structureInfo == null) return;

        val iterator = structureInfo.positions().iterator();
        while (iterator.hasNext()) {
            long key = iterator.nextLong();
            byPosition.remove(key, controller);
        }
        val iterator2 = structureInfo.watchedChunks().iterator();
        while (iterator2.hasNext()) {
            long key = iterator2.nextLong();
            byWatchedChunk.remove(key, controller);
        }
        byControllerChunk.remove(structureInfo.controllerChunk(), controller);
    }

    @Override
    public boolean canCheck(MultiblockControllerBase controller) {
        val structureInfo = structureInfos.get(controller);
        if (structureInfo == null) return false;
        val chunks = structureInfo.watchedChunks();
        val iterator = chunks.iterator();
        while (iterator.hasNext()) {
            long chunk = iterator.nextLong();
            int chunkX = (int) chunk;
            int chunkZ = (int) (chunk >> 32);
            if (!world.invokeIsChunkLoaded(chunkX, chunkZ, false)) return false;
        }
        return true;
    }

    @Override
    public void onBlockStateChanged(BlockPos position) {
        markDirty(byPosition.get(position.toLong()));
    }

    @Override
    public void onChunkLoad(int chunkX, int chunkZ) {
        markDirty(byWatchedChunk.get(ChunkPos.asLong(chunkX, chunkZ)));
    }

    @Override
    public void onChunkUnload(int chunkX, int chunkZ) {
        val controllers = byControllerChunk.get(ChunkPos.asLong(chunkX, chunkZ));
        if (controllers == null) return;
        // unregister() removes from this live multimap view, so iterate an immutable snapshot instead.
        List.copyOf(controllers).forEach(this::unregister);
    }

    private void markDirty(@Nullable Collection<MultiblockControllerBase> controllers) {
        if (controllers == null) return;
        // Invalid registrations are removed from the supplied live view while processing it.
        List.copyOf(controllers).forEach(it -> {
            if (isValid(it)) {
                PassiveStructureCheckExtension.cast(it).sus$markStructureDirty();
            } else {
                unregister(it);
            }
        });
    }

    private boolean isValid(MultiblockControllerBase controller) {
        if (controller.getWorld() != world || !controller.isValid() || !controller.isStructureFormed()) return false;
        val pos = controller.getPos();
        return world.isBlockLoaded(pos) && world.getTileEntity(pos) == controller.getHolder();
    }

    private record StructureInfo(LongSet positions, LongSet watchedChunks, long controllerChunk) {
    }
}
