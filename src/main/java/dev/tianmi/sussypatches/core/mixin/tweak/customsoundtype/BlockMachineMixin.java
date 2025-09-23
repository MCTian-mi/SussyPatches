package dev.tianmi.sussypatches.core.mixin.tweak.customsoundtype;

import static gregtech.api.util.GTUtility.getMetaTileEntity;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import dev.tianmi.sussypatches.api.core.mixin.extension.SoundTypeExtension;
import gregtech.api.block.BlockCustomParticle;
import gregtech.api.block.machines.BlockMachine;
import gregtech.api.metatileentity.MetaTileEntity;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2853")
@Mixin(value = BlockMachine.class, remap = false)
public abstract class BlockMachineMixin extends BlockCustomParticle {

    // Dummy
    BlockMachineMixin() {
        super(null);
    }

    @Unique
    @NotNull
    @Override
    @SuppressWarnings("deprecation")
    public SoundType getSoundType(@NotNull IBlockState state, @NotNull World world, @NotNull BlockPos pos,
                                  @Nullable Entity entity) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        if (metaTileEntity == null) return this.getSoundType();
        return SoundTypeExtension.cast(metaTileEntity).getSoundType();
    }
}
