package dev.tianmi.sussypatches.core.mixin.feature.visiblestorage;

import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import dev.tianmi.sussypatches.api.capability.impl.FluidTankView;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumTank;

@Mixin(value = MetaTileEntityQuantumTank.class, remap = false)
public abstract class QuantumTankMixin extends MetaTileEntity {

    // Dummy
    QuantumTankMixin() {
        super(null);
    }

    @ModifyArg(method = "renderMetaTileEntity(Lcodechicken/lib/render/CCRenderState;Lcodechicken/lib/vec/Matrix4;[Lcodechicken/lib/render/pipeline/IVertexOperation;)V",
               at = @At(value = "INVOKE",
                        target = "Lgregtech/client/renderer/texture/custom/QuantumStorageRenderer;renderTankFluid(Lcodechicken/lib/render/CCRenderState;Lcodechicken/lib/vec/Matrix4;[Lcodechicken/lib/render/pipeline/IVertexOperation;Lnet/minecraftforge/fluids/FluidTank;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)V"))
    protected FluidTank fromNBT(FluidTank original) {
        if (renderContextStack == null) return original;
        var handler = FluidUtil.getFluidHandler(renderContextStack);
        if (handler != null) return sus$wrapHandler(handler);
        return original;
    }

    @Unique
    protected FluidTank sus$wrapHandler(IFluidHandler handler) {
        return FluidTankView.of(handler);
    }
}
