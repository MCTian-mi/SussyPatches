package dev.tianmi.sussypatches.core.mixin.bugfix.ldpipelinecollapse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import gregtech.api.pipenet.longdist.ILDEndpoint;
import gregtech.api.pipenet.longdist.LongDistanceNetwork;
import gregtech.api.pipenet.longdist.NetworkBuilder;

/// A long distance pipeline is rebuilt on a new thread. Until that thread finishes,
/// every block of the pipeline is missing from the lookup, so a neighbour update
/// on an endpoint in that window makes the endpoint start a network of its own
/// and orphans the rest of the line ("Incomplete Pipeline").
///
/// The rebuild now runs on the server thread, so there is no such window, and
/// no chunk loading or map edits off the server thread either.
///
/// After every rebuild the endpoints also work out input/output again, so an
/// endpoint that an earlier collapse left with no role recovers.
@Mixin(value = LongDistanceNetwork.class, remap = false)
public abstract class LongDistanceNetworkMixin {

    @Shadow
    @Final
    private List<ILDEndpoint> endpoints;

    @Redirect(method = "recalculateNetwork",
              at = @At(value = "INVOKE",
                       target = "Lgregtech/api/pipenet/longdist/NetworkBuilder;start()V"))
    private void buildOnServerThread(NetworkBuilder builder) {
        builder.run();
    }

    @Inject(method = "setData", at = @At("RETURN"))
    private void refreshEndpointRoles(Collection<BlockPos> pipes, List<ILDEndpoint> found, CallbackInfo ci) {
        for (ILDEndpoint endpoint : new ArrayList<>(this.endpoints)) {
            if (!(endpoint instanceof LDEndpointInvoker invoker)) continue;
            World world = endpoint.getWorld();
            if (world == null || world.isRemote) continue;
            // findNetworks sets the role from each side it finds a pipe on
            if (invoker.sus$findNetworks().size() != 1) {
                endpoint.setIoType(ILDEndpoint.IOType.NONE);
            }
        }
    }
}
