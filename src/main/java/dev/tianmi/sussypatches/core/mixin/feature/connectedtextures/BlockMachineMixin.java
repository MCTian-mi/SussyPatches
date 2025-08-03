package dev.tianmi.sussypatches.core.mixin.feature.connectedtextures;

import net.minecraft.block.state.IBlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import dev.tianmi.sussypatches.api.metatileentity.IConnectable;
import gregtech.api.block.machines.BlockMachine;
import gregtech.api.metatileentity.MetaTileEntity;

@Mixin(value = BlockMachine.class, remap = false)
public abstract class BlockMachineMixin {

    @ModifyReturnValue(method = "getFacade(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Lnet/minecraft/block/state/IBlockState;",
                       at = @At("TAIL"))
    private IBlockState injectConnectableLogic(IBlockState original,
                                               @Local(name = "metaTileEntity") MetaTileEntity mte) {
        if (mte instanceof IConnectable connectable) {
            IBlockState visualState = connectable.getVisualState(null);
            if (visualState != null) {
                return visualState;
            }
        }
        return original;
    }
}
