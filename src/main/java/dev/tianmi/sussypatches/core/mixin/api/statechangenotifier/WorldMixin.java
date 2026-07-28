package dev.tianmi.sussypatches.core.mixin.api.statechangenotifier;

import dev.tianmi.sussypatches.common.helper.PassiveStructureCheck;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class WorldMixin {

    @Inject(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z", at = @At("RETURN"))
    private void sus$notifyPassiveStructures(BlockPos pos, IBlockState newState, int flags,
                                             CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            PassiveStructureCheck.onBlockStateChanged((World) (Object) this, pos);
        }
    }

    @Inject(method = "setTileEntity", at = @At("RETURN"))
    private void sus$notifyPassiveStructures(BlockPos pos, TileEntity tileEntityIn, CallbackInfo ci) {
        PassiveStructureCheck.onTileEntityChanged((World) (Object) this, pos, tileEntityIn);
    }

    @Inject(method = "removeTileEntity", at = @At("RETURN"))
    private void sus$notifyPassiveStructures(BlockPos pos, CallbackInfo ci) {
        PassiveStructureCheck.onTileEntityRemoved((World) (Object) this, pos);
    }
}
