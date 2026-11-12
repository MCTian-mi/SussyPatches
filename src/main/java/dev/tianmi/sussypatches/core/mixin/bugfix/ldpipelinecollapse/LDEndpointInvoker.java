package dev.tianmi.sussypatches.core.mixin.bugfix.ldpipelinecollapse;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import gregtech.api.pipenet.longdist.LongDistanceNetwork;
import gregtech.common.metatileentities.storage.MetaTileEntityLongDistanceEndpoint;

@Mixin(value = MetaTileEntityLongDistanceEndpoint.class, remap = false)
public interface LDEndpointInvoker {

    @Invoker("findNetworks")
    List<LongDistanceNetwork> sus$findNetworks();
}
