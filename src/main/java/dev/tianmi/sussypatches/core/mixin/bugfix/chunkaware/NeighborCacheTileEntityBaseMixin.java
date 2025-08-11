package dev.tianmi.sussypatches.core.mixin.bugfix.chunkaware;

import org.spongepowered.asm.mixin.Mixin;

import dev.tianmi.sussypatches.api.core.mixin.extension.ChunkAwareExtension;
import gregtech.api.metatileentity.NeighborCacheTileEntityBase;

@Mixin(value = NeighborCacheTileEntityBase.class, remap = false)
public abstract class NeighborCacheTileEntityBaseMixin implements ChunkAwareExtension {}
