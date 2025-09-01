package dev.tianmi.sussypatches.core.mixin.feature.visiblestorage;

import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import dev.tianmi.sussypatches.api.capability.impl.FluidTankView;
import gregtech.common.metatileentities.storage.MetaTileEntityCreativeTank;

@Mixin(value = MetaTileEntityCreativeTank.class, remap = false)
public abstract class CreativeTankMixin extends QuantumTankMixin {

    @Override
    @ModifyArg(method = "renderMetaTileEntity(Lcodechicken/lib/render/CCRenderState;Lcodechicken/lib/vec/Matrix4;[Lcodechicken/lib/render/pipeline/IVertexOperation;)V",
               at = @At(value = "INVOKE",
                        target = "Lgregtech/client/renderer/texture/custom/QuantumStorageRenderer;renderTankFluid(Lcodechicken/lib/render/CCRenderState;Lcodechicken/lib/vec/Matrix4;[Lcodechicken/lib/render/pipeline/IVertexOperation;Lnet/minecraftforge/fluids/FluidTank;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)V"))
    protected FluidTank fromNBT(FluidTank original) {
        return super.fromNBT(original);
    }

    @Unique
    @Override
    protected FluidTank sus$wrapHandler(IFluidHandler handler) {
        return FluidTankView.full(handler);
    }
}
