package dev.tianmi.sussypatches.core.mixin.compat.inworldpreviewcrash;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import dev.tianmi.sussypatches.api.annotation.Compat;
import dev.tianmi.sussypatches.api.util.SusMods;
import git.jbredwards.fluidlogged_api.api.asm.impl.IChunkProvider;

@Compat(mods = SusMods.FluidloggedAPI_2)
@Mixin(targets = "gregtech.client.renderer.handler.MultiblockPreviewRenderer$TargetBlockAccess", remap = false)
public class TargetBlockAccessMixin implements IChunkProvider {

    @Shadow
    @Final
    private IBlockAccess delegate;

    @Nullable
    @Override
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public Chunk getChunkFromBlockCoords(@NotNull BlockPos blockPos) {
        /// [delegate] can only be instances of [DummyWorld]
        /// According to the source code
        return ((World) delegate).getChunk(blockPos);
    }
}
