//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package git.jbredwards.fluidlogged_api.api.block;

import git.jbredwards.fluidlogged_api.api.util.FluidState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jspecify.annotations.NonNull;

/// Adapted and minimized from
/// [Fluidlogged API](https://github.com/jbredwards/Fluidlogged-API/blob/1.12.2-Latest/src/main/java/git/jbredwards/fluidlogged_api/api/block/IFluidloggable.java)
///
/// Wrapper class for a fluid [IBlockState] that adds many helpful functions and makes it easier to work with fluids.
/// Duplicate [FluidState]s are not allowed! Up to one [FluidState] will exist at any given time for each [IBlockState].
///
/// @author jbred
/// @since 1.7.0
public interface IFluidloggable {
    default boolean isFluidloggable(@NonNull IBlockState state, @NonNull World world, @NonNull BlockPos pos) {
        return true;
    }

    default boolean isFluidValid(@NonNull IBlockState state, @NonNull World world, @NonNull BlockPos pos, @NonNull Fluid fluid) {
        return this.isFluidloggable(state, world, pos);
    }

    default boolean canFluidFlow(@NonNull IBlockAccess world, @NonNull BlockPos pos, @NonNull IBlockState here, @NonNull EnumFacing side) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    default boolean shouldFluidRender(@NonNull IBlockAccess world, @NonNull BlockPos pos, @NonNull IBlockState here, @NonNull FluidState fluidState) {
        return true;
    }

    @NonNull
    default EnumActionResult onFluidChange(@NonNull World world, @NonNull BlockPos pos, @NonNull IBlockState here, @NonNull FluidState newFluid, int blockFlags) {
        return EnumActionResult.PASS;
    }

    @NonNull
    default EnumActionResult onFluidFill(@NonNull World world, @NonNull BlockPos pos, @NonNull IBlockState here, @NonNull FluidState newFluid, int blockFlags) {
        return EnumActionResult.PASS;
    }

    @NonNull
    default EnumActionResult onFluidDrain(@NonNull World world, @NonNull BlockPos pos, @NonNull IBlockState here, int blockFlags) {
        return EnumActionResult.PASS;
    }
}
