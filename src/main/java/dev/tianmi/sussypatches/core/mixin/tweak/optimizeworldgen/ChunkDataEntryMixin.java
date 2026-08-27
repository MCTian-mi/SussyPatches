package dev.tianmi.sussypatches.core.mixin.tweak.optimizeworldgen;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import gregtech.api.worldgen.generator.CachedGridEntry.ChunkDataEntry;
import lombok.val;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ChunkDataEntry.class, remap = false)
public abstract class ChunkDataEntryMixin {

    @Shadow
    @Final
    private int chunkX;

    @Shadow
    @Final
    private int chunkZ;

    @Inject(method = "populateChunk", at = @At("HEAD"))
    private void initChunkAndStorage(World world, CallbackInfoReturnable<Boolean> cir,
                                     @Share("chunk") LocalRef<Chunk> chunkRef,
                                     @Share("storage") LocalRef<ExtendedBlockStorage[]> storageRef) {
        val chunk = world.getChunk(chunkX, chunkZ);
        chunkRef.set(chunk);
        storageRef.set(chunk.getBlockStorageArray());
    }

    @SuppressWarnings("DataFlowIssue")
    @WrapOperation(method = "populateChunk",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;",
                            remap = true))
    private IBlockState fromChunkStorage(World world, BlockPos pos, Operation<IBlockState> method,
                                         @Share("storage") LocalRef<ExtendedBlockStorage[]> storageRef) {
        val storage = storageRef.get()[pos.getY() >> 4];
        return storage == Chunk.NULL_BLOCK_STORAGE
                ? Blocks.AIR.getDefaultState()
                : storage.get(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
    }

    @WrapOperation(method = "populateChunk",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z",
                            remap = true))
    private boolean setChunkStorage(World world, BlockPos pos, IBlockState newState, int flags,
                                    Operation<Boolean> method,
                                    @Share("storage") LocalRef<ExtendedBlockStorage[]> storageRef) {
        int x = pos.getX() & 15;
        int y = pos.getY();
        int z = pos.getZ() & 15;
        val storage = storageRef.get()[y >> 4];
        if (storage != Chunk.NULL_BLOCK_STORAGE) {
            val oldState = storage.get(x, y & 15, z);
            if (oldState.getLightOpacity(world, pos) == newState.getLightOpacity(world, pos)
                    && oldState.getLightValue(world, pos) == newState.getLightValue(world, pos)
                    && !oldState.getBlock().hasTileEntity(oldState)
                    && !newState.getBlock().hasTileEntity(newState)) {
                storage.set(x, y & 15, z, newState);
                return true;
            }
        }
        return method.call(world, pos, newState, flags); // fallback to World#setBlockState
    }

    @Inject(method = "populateChunk", at = @At("RETURN"))
    private void markChunkDirty(World world, CallbackInfoReturnable<Boolean> cir,
                                @Local(name = "generatedAnything") boolean generatedAnything,
                                @Share("chunk") LocalRef<Chunk> chunkRef) {
        if (generatedAnything) {
            chunkRef.get().setModified(true);
        }
    }
}
