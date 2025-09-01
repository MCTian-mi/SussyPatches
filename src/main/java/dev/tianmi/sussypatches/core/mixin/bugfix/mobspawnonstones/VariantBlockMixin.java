package dev.tianmi.sussypatches.core.mixin.bugfix.mobspawnonstones;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import dev.tianmi.sussypatches.api.block.IStateSpawnControl;
import gregtech.api.block.VariantBlock;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2859")
@Mixin(value = VariantBlock.class, remap = false)
public abstract class VariantBlockMixin<T extends Enum<T> & IStringSerializable> extends Block {

    // Dummy
    @SuppressWarnings("DataFlowIssue")
    VariantBlockMixin() {
        super(null);
    }

    @Shadow
    public abstract T getState(IBlockState blockState);

    @Unique
    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        if (getState(state) instanceof IStateSpawnControl stateSpawnControl) {
            return stateSpawnControl.canCreatureSpawn(state, world, pos, type);
        }
        return super.canCreatureSpawn(state, world, pos, type);
    }
}
