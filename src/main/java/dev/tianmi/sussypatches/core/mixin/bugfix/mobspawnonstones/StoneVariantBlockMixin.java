package dev.tianmi.sussypatches.core.mixin.bugfix.mobspawnonstones;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import dev.tianmi.sussypatches.api.block.IStateSpawnControl;
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

    @Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2859")
    @Mixin(value = StoneType.class, remap = false)
    public static class StoneTypeMixin implements IStateSpawnControl {

        @Unique
        private boolean sus$allowSpawn;

        @Inject(method = "<init>", at = @At("TAIL"))
        private void initAllowSpawn(String enumName, int ordinal, String name, MapColor mapColor, CallbackInfo ci) {
            this.sus$allowSpawn = !(name.equals("concrete_light") || name.equals("concrete_dark"));
        }

        @Unique
        @Override
        @SuppressWarnings("AddedMixinMembersNamePattern")
        public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                        @NotNull SpawnPlacementType type) {
            return this.sus$allowSpawn;
        }
    }
}
