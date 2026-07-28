package dev.tianmi.sussypatches.api.capability;

import dev.tianmi.sussypatches.Tags;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import it.unimi.dsi.fastutil.longs.LongCollection;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

/// Tracks formed multiblock controllers by their cached structure positions so world changes can request a deferred
/// structure check. Registrations are transient, belong to a server world, and are accessed from the logical server
/// thread.
public interface IMultiblockStateManager {

    ResourceLocation ID = new ResourceLocation(Tags.MOD_ID, "multiblock_state_manager");

    /// Replaces a controller's existing registration with the supplied structure positions.
    ///
    /// @param controller the formed controller being registered
    /// @param positions  packed block positions, using [BlockPos#toLong()]
    void replaceRegistration(MultiblockControllerBase controller, LongCollection positions);

    /// Removes a controller and all of its position and chunk index entries. Does nothing when the controller is not
    /// registered.
    ///
    /// @param controller the controller to remove
    void unregister(MultiblockControllerBase controller);

    /// Tests whether a deferred structure check can run without loading chunks.
    ///
    /// @param controller the controller awaiting a structure check
    /// @return `true` when the controller is registered and every chunk containing a watched position is loaded
    boolean canCheck(MultiblockControllerBase controller);

    /// Marks controllers watching an exact block position as needing a deferred structure check.
    ///
    /// @param position the changed block position
    void onBlockStateChanged(BlockPos position);

    /// Marks controllers watching positions in a newly loaded chunk as needing a deferred structure check. This
    /// revalidates structures after changes that may have occurred while the watched chunk was unloaded.
    ///
    /// @param chunkX the loaded chunk's X coordinate
    /// @param chunkZ the loaded chunk's Z coordinate
    void onChunkLoad(int chunkX, int chunkZ);

    /// Removes registrations for controllers whose own tile entities reside in an unloading chunk.
    ///
    /// @param chunkX the unloading chunk's X coordinate
    /// @param chunkZ the unloading chunk's Z coordinate
    void onChunkUnload(int chunkX, int chunkZ);
}
