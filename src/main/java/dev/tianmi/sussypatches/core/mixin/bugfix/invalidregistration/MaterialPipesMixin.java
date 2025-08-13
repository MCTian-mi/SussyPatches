package dev.tianmi.sussypatches.core.mixin.bugfix.invalidregistration;

import java.util.Collection;
import java.util.Collections;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import dev.tianmi.sussypatches.api.core.mixin.extension.MaterialPipeExtension;
import gregtech.api.pipenet.block.material.BlockMaterialPipe;
import gregtech.api.unification.material.Material;
import gregtech.common.pipelike.cable.BlockCable;
import gregtech.common.pipelike.fluidpipe.BlockFluidPipe;
import gregtech.common.pipelike.itempipe.BlockItemPipe;

@Mixin(value = BlockMaterialPipe.class, remap = false)
public abstract class MaterialPipesMixin implements MaterialPipeExtension {

    @Unique
    @Override
    public Collection<Material> sus$getEnabledMaterials() {
        return Collections.emptySet();
    }

    @Mixin(value = BlockCable.class, remap = false)
    public abstract static class BlockCableMixin extends MaterialPipesMixin {

        @Shadow
        public abstract Collection<Material> getEnabledMaterials();

        @Unique
        @Override
        public Collection<Material> sus$getEnabledMaterials() {
            return this.getEnabledMaterials();
        }
    }

    @Mixin(value = BlockFluidPipe.class, remap = false)
    public abstract static class BlockFluidPipeMixin extends MaterialPipesMixin {

        @Shadow
        public abstract Collection<Material> getEnabledMaterials();

        @Unique
        @Override
        public Collection<Material> sus$getEnabledMaterials() {
            return this.getEnabledMaterials();
        }
    }

    @Mixin(value = BlockItemPipe.class, remap = false)
    public abstract static class BlockItemPipeMixin extends MaterialPipesMixin {

        @Shadow
        public abstract Collection<Material> getEnabledMaterials();

        @Unique
        @Override
        public Collection<Material> sus$getEnabledMaterials() {
            return this.getEnabledMaterials();
        }
    }
}
