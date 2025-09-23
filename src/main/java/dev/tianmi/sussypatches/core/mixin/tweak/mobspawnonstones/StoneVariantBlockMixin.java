package dev.tianmi.sussypatches.core.mixin.tweak.mobspawnonstones;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import gregtech.api.block.VariantBlock;
import gregtech.common.blocks.StoneVariantBlock;
import gregtech.common.blocks.StoneVariantBlock.StoneType;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2859")
@Mixin(value = StoneVariantBlock.class, remap = false)
public abstract class StoneVariantBlockMixin extends VariantBlock<StoneType> {

    // Dummy
    StoneVariantBlockMixin() {
        super(null);
    }

    /**
     * @author Tian_mi
     * @reason This is a hard rewrite, any conflict should result in a hard crash
     */
    @Override
    @Overwrite
    public boolean canCreatureSpawn(@NotNull IBlockState state,
                                    @NotNull IBlockAccess world,
                                    @NotNull BlockPos pos,
                                    @NotNull SpawnPlacementType type) {
        return super.canCreatureSpawn(state, world, pos, type);
    }
}
