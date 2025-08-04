package dev.tianmi.sussypatches.api.core.mixin.extension;

import java.util.Collection;

import dev.tianmi.sussypatches.api.annotation.MixinExtension;
import gregtech.api.pipenet.block.material.BlockMaterialPipe;
import gregtech.api.unification.material.Material;
import gregtech.common.pipelike.cable.BlockCable;
import gregtech.common.pipelike.fluidpipe.BlockFluidPipe;
import gregtech.common.pipelike.itempipe.BlockItemPipe;

@MixinExtension({
        BlockMaterialPipe.class,
        BlockCable.class,
        BlockFluidPipe.class,
        BlockItemPipe.class,
})
public interface MaterialPipeExtension {

    static MaterialPipeExtension cast(BlockMaterialPipe<?, ?, ?> pipe) {
        return (MaterialPipeExtension) pipe;
    }

    Collection<Material> sus$getEnabledMaterials();
}
