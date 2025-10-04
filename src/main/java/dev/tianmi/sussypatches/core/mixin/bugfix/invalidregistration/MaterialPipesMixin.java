package dev.tianmi.sussypatches.core.mixin.bugfix.invalidregistration;

import java.util.Collection;

import org.spongepowered.asm.mixin.*;

import dev.tianmi.sussypatches.api.core.mixin.extension.MaterialPipeExtension;
import gregtech.api.pipenet.block.material.BlockMaterialPipe;
import gregtech.api.unification.material.Material;
import gregtech.common.pipelike.cable.BlockCable;
import gregtech.common.pipelike.fluidpipe.BlockFluidPipe;
import gregtech.common.pipelike.itempipe.BlockItemPipe;

@Mixin(value = {
        BlockMaterialPipe.class,
        BlockCable.class,
        BlockFluidPipe.class,
        BlockItemPipe.class,
}, remap = false)
@Implements(@Interface(iface = MaterialPipeExtension.class, prefix = "sus$", unique = true))
public abstract class MaterialPipesMixin {

    @Intrinsic
    public abstract Collection<Material> sus$getEnabledMaterials();
}
