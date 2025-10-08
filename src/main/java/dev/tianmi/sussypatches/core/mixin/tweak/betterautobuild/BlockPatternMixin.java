package dev.tianmi.sussypatches.core.mixin.tweak.betterautobuild;

import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import dev.tianmi.sussypatches.api.annotation.Transformer;
import dev.tianmi.sussypatches.core.asm.transformer.BlockPatternTransformer;
import gregtech.api.pattern.BlockPattern;

@Transformer(clazz = BlockPatternTransformer.class)
@Mixin(value = BlockPattern.class, remap = false)
public class BlockPatternMixin {

    @WrapOperation(method = "autoBuild",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/block/material/Material;isReplaceable()Z",
                            remap = true))
    private boolean navigateState(Material material, Operation<Boolean> method,
                                  @Local(name = "world") World world,
                                  @Local(name = "pos") BlockPos pos) {
        return world.getBlockState(pos).getBlock().isReplaceable(world, pos) || method.call(material);
    }
}
