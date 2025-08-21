package dev.tianmi.sussypatches.core.mixin.bugfix.implgetitem;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import gregtech.api.block.BuiltInRenderBlock;
import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.tile.IPipeTile;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2852")
@Mixin(value = BlockPipe.class, remap = false)
public abstract class BlockPipeMixin<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, NodeDataType,
        WorldPipeNetType extends WorldPipeNet<NodeDataType, ? extends PipeNet<NodeDataType>>>
                                    extends BuiltInRenderBlock {

    @Shadow
    public abstract ItemStack getDropItem(IPipeTile<PipeType, NodeDataType> pipeTile);

    // Dummy
    BlockPipeMixin() {
        super(null);
    }

    @Unique
    @NotNull
    @Override
    @SuppressWarnings({ "deprecation", "unchecked" })
    public ItemStack getItem(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state) {
        var te = world.getTileEntity(pos);
        if (!(te instanceof IPipeTile<?, ?>pipeTile)) return ItemStack.EMPTY;
        return getDropItem((IPipeTile<PipeType, NodeDataType>) pipeTile);
    }
}
