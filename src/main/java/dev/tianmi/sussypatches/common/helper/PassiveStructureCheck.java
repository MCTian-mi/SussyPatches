package dev.tianmi.sussypatches.common.helper;

import dev.tianmi.sussypatches.api.capability.IMultiblockStateManager;
import dev.tianmi.sussypatches.api.capability.SusCapabilities;
import dev.tianmi.sussypatches.api.capability.impl.MbsManagerCapabilityProvider;
import lombok.val;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@SuppressWarnings("DataFlowIssue")
public final class PassiveStructureCheck {

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<World> event) {
        val world = event.getObject();
        if (!world.isRemote) {
            event.addCapability(IMultiblockStateManager.ID, new MbsManagerCapabilityProvider(world));
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        val chunk = event.getChunk();
        val registry = chunk.getWorld().getCapability(SusCapabilities.MULTIBLOCK_STATE_MANAGER, null);
        if (registry != null) registry.onChunkLoad(chunk.x, chunk.z);
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        val chunk = event.getChunk();
        val registry = chunk.getWorld().getCapability(SusCapabilities.MULTIBLOCK_STATE_MANAGER, null);
        if (registry != null) registry.onChunkUnload(chunk.x, chunk.z);
    }

    public static void onTileEntityChanged(World world, BlockPos position, @Nullable TileEntity requestedTileEntity) {
        if (requestedTileEntity == null || world.getTileEntity(position) != requestedTileEntity) return;
        onBlockStateChanged(world, position);
    }

    public static void onTileEntityRemoved(World world, BlockPos position) {
        onBlockStateChanged(world, position);
    }

    public static void onBlockStateChanged(World world, BlockPos position) {
        notifyRegistry(world, position);
    }

    private static void notifyRegistry(World world, BlockPos position) {
        IMultiblockStateManager registry = world.getCapability(SusCapabilities.MULTIBLOCK_STATE_MANAGER, null);
        if (registry != null) registry.onBlockStateChanged(position);
    }
}
