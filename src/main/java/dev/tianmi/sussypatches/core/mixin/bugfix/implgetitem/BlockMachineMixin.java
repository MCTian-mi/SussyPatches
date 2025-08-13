package dev.tianmi.sussypatches.core.mixin.bugfix.implgetitem;

import static gregtech.api.util.GTUtility.getMetaTileEntity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import gregtech.api.block.BlockCustomParticle;
import gregtech.api.block.machines.BlockMachine;
import gregtech.api.metatileentity.MetaTileEntity;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2555")
@Mixin(value = BlockMachine.class, remap = false)
public abstract class BlockMachineMixin extends BlockCustomParticle {

    // Dummy
    BlockMachineMixin() {
        super(null, null);
    }

    @Unique
    @NotNull
    @Override
    @SuppressWarnings("deprecation")
    public ItemStack getItem(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        if (metaTileEntity == null)
            return ItemStack.EMPTY;
        return metaTileEntity.getStackForm();
    }
}
