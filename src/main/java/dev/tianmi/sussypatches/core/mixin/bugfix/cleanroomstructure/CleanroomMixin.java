package dev.tianmi.sussypatches.core.mixin.bugfix.cleanroomstructure;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityCleanroom;

@Mixin(value = MetaTileEntityCleanroom.class, remap = false)
public abstract class CleanroomMixin {

    @WrapOperation(method = "updateStructureDimensions",
                   at = @At(value = "INVOKE",
                            target = "Lgregtech/common/metatileentities/multi/electric/MetaTileEntityCleanroom;isBlockEdge(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos$MutableBlockPos;Lnet/minecraft/util/EnumFacing;)Z"),
                   require = -1)
    private boolean allowNonCasings(MetaTileEntityCleanroom cleanroom, World world, BlockPos.MutableBlockPos pos,
                                    EnumFacing facing, Operation<Boolean> method) {
        return method.call(cleanroom, world, pos, facing) ||
                world.getBlockState(pos).getBlock() != MetaBlocks.CLEANROOM_CASING;
    }
}
