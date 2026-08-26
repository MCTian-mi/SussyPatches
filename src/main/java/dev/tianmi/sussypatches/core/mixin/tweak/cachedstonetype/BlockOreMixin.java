package dev.tianmi.sussypatches.core.mixin.tweak.cachedstonetype;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import gregtech.common.blocks.BlockOre;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = BlockOre.class, remap = false)
public abstract class BlockOreMixin extends Block {

    @Unique
    private boolean sus$materialInitiated = false;

    @SuppressWarnings("DataFlowIssue")
    BlockOreMixin() { // Dummy
        super(null, null);
    }

    @WrapMethod(method = "getMaterial", remap = true)
    private Material cachesResult(IBlockState state, Operation<Material> method) {
        if (!sus$materialInitiated) {
            this.material = method.call(state);
            this.sus$materialInitiated = true;
        }
        return material;
    }
}
