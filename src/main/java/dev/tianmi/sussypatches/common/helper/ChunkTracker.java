package dev.tianmi.sussypatches.common.helper;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Function;

@NullMarked
public final class ChunkTracker {
    private static final Registry<World, Chunk> REGISTRY = new Registry<>();

    private ChunkTracker() {
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        Chunk chunk = event.getChunk();
        REGISTRY.onLoad(chunk.getWorld(), chunk.x, chunk.z, chunk);
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        Chunk chunk = event.getChunk();
        REGISTRY.onUnload(chunk.getWorld(), chunk.x, chunk.z);
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        REGISTRY.onWorldUnload(event.getWorld());
    }

    @Nullable
    public static Snapshot<Chunk> get(World world, int x, int z) {
        return REGISTRY.get(world, x, z);
    }

    private static final class Registry<W, C> {
        private final List<WorldEntry<W, C>> worlds = new ArrayList<>();
        private final Function<W, Reference<W>> worldReferenceFactory;
        private final Function<C, Reference<C>> chunkReferenceFactory;
        private long nextGeneration;

        Registry() {
            this(WeakReference::new, WeakReference::new);
        }

        Registry(
                Function<W, Reference<W>> worldReferenceFactory,
                Function<C, Reference<C>> chunkReferenceFactory) {
            this.worldReferenceFactory = worldReferenceFactory;
            this.chunkReferenceFactory = chunkReferenceFactory;
        }

        private static long pack(int x, int z) {
            return ((long) x << 32) ^ (z & 0xffffffffL);
        }

        synchronized void onLoad(W world, int x, int z, C chunk) {
            WorldEntry<W, C> worldEntry = findWorld(world);
            if (worldEntry == null) {
                worldEntry = new WorldEntry<>(worldReferenceFactory.apply(world), new HashMap<>());
                worlds.add(worldEntry);
            }
            worldEntry.chunks.put(
                    pack(x, z), new Entry<>(++nextGeneration, chunkReferenceFactory.apply(chunk)));
        }

        synchronized void onUnload(W world, int x, int z) {
            WorldEntry<W, C> worldEntry = findWorld(world);
            if (worldEntry == null) return;
            worldEntry.chunks.remove(pack(x, z));
            if (worldEntry.chunks.isEmpty()) worlds.remove(worldEntry);
        }

        synchronized void onWorldUnload(W world) {
            Iterator<WorldEntry<W, C>> iterator = worlds.iterator();
            while (iterator.hasNext()) {
                W trackedWorld = iterator.next().world.get();
                if (trackedWorld == null || trackedWorld == world) iterator.remove();
            }
        }

        @Nullable
        synchronized Snapshot<C> get(W world, int x, int z) {
            WorldEntry<W, C> worldEntry = findWorld(world);
            if (worldEntry == null) return null;
            long key = pack(x, z);
            Entry<C> entry = worldEntry.chunks.get(key);
            if (entry == null) return null;
            C chunk = entry.chunk.get();
            if (chunk == null) {
                worldEntry.chunks.remove(key);
                if (worldEntry.chunks.isEmpty()) worlds.remove(worldEntry);
                return null;
            }
            return new Snapshot<>(entry.generation, chunk);
        }

        @Nullable
        private WorldEntry<W, C> findWorld(W world) {
            Iterator<WorldEntry<W, C>> iterator = worlds.iterator();
            while (iterator.hasNext()) {
                WorldEntry<W, C> entry = iterator.next();
                W trackedWorld = entry.world.get();
                if (trackedWorld == null) {
                    iterator.remove();
                } else if (trackedWorld == world) {
                    return entry;
                }
            }
            return null;
        }

        private record Entry<C>(long generation, Reference<C> chunk) {
        }

        private record WorldEntry<W, C>(Reference<W> world, Map<Long, Entry<C>> chunks) {
        }
    }

    public record Snapshot<C>(long generation, C chunk) {
    }
}

