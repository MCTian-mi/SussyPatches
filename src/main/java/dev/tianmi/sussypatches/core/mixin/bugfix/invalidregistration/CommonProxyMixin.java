package dev.tianmi.sussypatches.core.mixin.bugfix.invalidregistration;

import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import dev.tianmi.sussypatches.api.core.mixin.extension.MaterialPipeExtension;
import gregtech.api.pipenet.block.material.BlockMaterialPipe;
import gregtech.api.pipenet.block.material.ItemBlockMaterialPipe;
import gregtech.common.CommonProxy;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2780")
@Mixin(value = CommonProxy.class, remap = false)
public abstract class CommonProxyMixin {

    @WrapWithCondition(method = "registerBlocks",
                       at = { // spotless:off
                               @At(ordinal = 1, target = "Lnet/minecraftforge/registries/IForgeRegistry;register(Lnet/minecraftforge/registries/IForgeRegistryEntry;)V", value = "INVOKE"),
                               @At(ordinal = 2, target = "Lnet/minecraftforge/registries/IForgeRegistry;register(Lnet/minecraftforge/registries/IForgeRegistryEntry;)V", value = "INVOKE"),
                               @At(ordinal = 3, target = "Lnet/minecraftforge/registries/IForgeRegistry;register(Lnet/minecraftforge/registries/IForgeRegistryEntry;)V", value = "INVOKE"),
                       }) // spotless:on
    private static boolean registerPipeBlocksOnlyWhenNeeded(IForgeRegistry<?> registry, IForgeRegistryEntry<?> entry) {
        if (entry instanceof MaterialPipeExtension matPipe) {
            return !matPipe.sus$getEnabledMaterials().isEmpty();
        }
        throw new AssertionError("CommonProxyMixin mixed-in to a wrong target: \"" + entry.getClass() + "\"!");
    }

    @WrapWithCondition(method = "registerItems",
                       at = { // spotless:off
                               @At(ordinal = 3, target = "Lnet/minecraftforge/registries/IForgeRegistry;register(Lnet/minecraftforge/registries/IForgeRegistryEntry;)V", value = "INVOKE"),
                               @At(ordinal = 4, target = "Lnet/minecraftforge/registries/IForgeRegistry;register(Lnet/minecraftforge/registries/IForgeRegistryEntry;)V", value = "INVOKE"),
                               @At(ordinal = 5, target = "Lnet/minecraftforge/registries/IForgeRegistry;register(Lnet/minecraftforge/registries/IForgeRegistryEntry;)V", value = "INVOKE"),
                       }) // spotless:on
    private static boolean registerPipeItemsWhenNeeded(IForgeRegistry<?> registry, IForgeRegistryEntry<?> entry) {
        if (entry instanceof ItemBlockMaterialPipe<?, ?>pipeItem) {
            var pipeBlock = (BlockMaterialPipe<?, ?, ?>) pipeItem.getBlock();
            return !MaterialPipeExtension.cast(pipeBlock).sus$getEnabledMaterials().isEmpty();
        }
        throw new AssertionError("CommonProxyMixin mixed-in to a wrong target: \"" + entry.getClass() + "\"!");
    }
}
