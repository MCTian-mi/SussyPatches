package team.chisel.ctm.client.state;

import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;

/// Adapted and minimized from
/// [ConnectedTexturesMod](https://github.com/Chisel-Team/ConnectedTexturesMod/blob/1.12/dev/src/main/java/team/chisel/ctm/client/state/CTMExtendedState.java)
///
/// The class inheritance here is wrong, just don't wanna impl all the abstract methods myself.
public class CTMExtendedState extends BlockStateContainer.StateImplementation {

    @SuppressWarnings("DataFlowIssue")
    public CTMExtendedState(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos) {
        super(null, null);
    }
}
