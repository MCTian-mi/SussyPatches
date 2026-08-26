package dev.tianmi.sussypatches.core.mixin.tweak.optimizeworldgen;

import com.google.common.base.Predicate;
import dev.tianmi.sussypatches.common.helper.StoneTypeCache;
import gregtech.api.unification.ore.StoneType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = StoneType.class, remap = false)
public abstract class StoneTypeMixin {


    @Inject(method = "computeStoneType",
            at = @At(value = "FIELD",
                     target = "Lgregtech/api/unification/ore/StoneType;STONE_TYPE_REGISTRY:Lgregtech/api/util/GTControlledRegistry;",
                     opcode = Opcodes.GETSTATIC,
                     ordinal = 0),
            cancellable = true)
    private static void fromCache(IBlockState state, IBlockAccess world, BlockPos pos, CallbackInfoReturnable<StoneType> cir) {
        cir.setReturnValue(StoneTypeCache.get(state, ((type, it) -> state.getBlock().isReplaceableOreGen(it, world, pos, ((StoneTypeMixin) (Object) type).getPredicate()))));
    }

    @Inject(method = "computeStoneType",
            at = @At(value = "FIELD",
                     target = "Lgregtech/api/unification/ore/StoneType;STONE_TYPE_REGISTRY:Lgregtech/api/util/GTControlledRegistry;",
                     opcode = Opcodes.GETSTATIC,
                     ordinal = 1),
            cancellable = true)
    private static void alsoFromCache(IBlockState state, IBlockAccess world, BlockPos pos, CallbackInfoReturnable<StoneType> cir) {
        cir.setReturnValue(StoneTypeCache.get(state, ((type, it) -> ((StoneTypeMixin) (Object) type).getPredicate().test(it))));
    }

    @Final
    @Accessor("predicate")
    abstract Predicate<IBlockState> getPredicate();
}
