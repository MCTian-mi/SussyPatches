package dev.tianmi.sussypatches.core.mixin.bugfix.ldpipelinecollapse;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import gregtech.api.pipenet.longdist.ILDEndpoint;
import gregtech.api.pipenet.longdist.ILDNetworkPart;
import gregtech.api.pipenet.longdist.NetworkBuilder;

/// The network walk records endpoints but never walks on from them. The network
/// keeps its endpoints as objects, so after an endpoint's chunk is unloaded and
/// loaded again (everyone logs off, server restart) the new endpoint object is
/// unknown to it, and the rebuild this triggers starts at that endpoint, finds
/// only the endpoint itself and drops every pipe ("Incomplete Pipeline").
///
/// When the start is an endpoint, the walk now continues into the pipe in front
/// of or behind it, the only sides a pipeline attaches to.
@Mixin(value = NetworkBuilder.class, remap = false)
public abstract class NetworkBuilderMixin {

    @Shadow
    @Final
    private World world;

    @Shadow
    private void checkPos(IBlockState blockState, BlockPos pos) {
        throw new AssertionError();
    }

    @Shadow
    private IBlockState getBlockState(BlockPos pos) {
        throw new AssertionError();
    }

    @Inject(method = "checkNetwork",
            at = @At(value = "INVOKE",
                     ordinal = 0,
                     shift = At.Shift.AFTER,
                     target = "Lgregtech/api/pipenet/longdist/NetworkBuilder;checkPos(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;)V"))
    private void walkPastStartEndpoint(BlockPos start, CallbackInfo ci) {
        ILDEndpoint endpoint = ILDEndpoint.tryGet(this.world, start);
        if (endpoint == null) return;
        // An endpoint with a pipeline on both sides has no role,
        // so only walk on when exactly one side has a pipeline part.
        BlockPos pipe = null;
        IBlockState pipeState = null;
        for (EnumFacing side : new EnumFacing[] { endpoint.getFrontFacing(), endpoint.getOutputFacing() }) {
            BlockPos next = start.offset(side);
            IBlockState state = getBlockState(next);
            if (!endpoint.getPipeType().isValidPart(ILDNetworkPart.tryGet(this.world, next, state))) continue;
            if (pipe != null) return;
            pipe = next;
            pipeState = state;
        }
        if (pipe != null) {
            checkPos(pipeState, pipe);
        }
    }
}
