package git.jbredwards.fluidlogged_api.api.asm.impl;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/// Adapted and minimized from
/// [Fluidlogged API v2](https://github.com/jbredwards/Fluidlogged-API/blob/baacfa0c189f6cb2776106cf3a53318fe7bbd0d8/src/main/java/git/jbredwards/fluidlogged_api/api/asm/impl/IChunkProvider.java)
///
/// Gives server-side IBlockAccess (non-World) instances the option to provide chunks in a way that this mod can access them.
/// The [World] and [ChunkCache] classes implement this at runtime.
///
/// @author jbred
public interface IChunkProvider {

    @Nullable
    Chunk getChunkFromBlockCoords(@Nonnull BlockPos var1);
}