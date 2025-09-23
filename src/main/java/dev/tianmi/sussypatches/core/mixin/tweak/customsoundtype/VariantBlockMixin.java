package dev.tianmi.sussypatches.core.mixin.tweak.customsoundtype;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import dev.tianmi.sussypatches.api.block.IStateSoundType;
import gregtech.api.block.VariantBlock;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2853")
@Mixin(value = VariantBlock.class, remap = false)
public abstract class VariantBlockMixin<T extends Enum<T> & IStringSerializable> extends Block {

    @Shadow
    public abstract T getState(IBlockState blockState);

    // Dummy
    @SuppressWarnings("DataFlowIssue")
    VariantBlockMixin() {
        super(null);
    }

    @Unique
    @NotNull
    @Override
    public SoundType getSoundType(@NotNull IBlockState state, @NotNull World world, @NotNull BlockPos pos,
                                  @Nullable Entity entity) {
        if (getState(state) instanceof IStateSoundType stateSoundType) {
            return stateSoundType.getSoundType(state);
        }
        return super.getSoundType(state, world, pos, entity);
    }
}
