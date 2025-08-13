package dev.tianmi.sussypatches.core.mixin.tweak.previewoptimization;

import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.llamalad7.mixinextras.sugar.Local;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import dev.tianmi.sussypatches.api.core.mixin.extension.WSRExtension;
import dev.tianmi.sussypatches.client.renderer.scene.VBOWorldSceneRenderer;
import gregtech.client.renderer.scene.ISceneRenderHook;
import gregtech.client.renderer.scene.ImmediateWorldSceneRenderer;
import gregtech.client.renderer.scene.WorldSceneRenderer;
import gregtech.integration.jei.multiblock.MultiblockInfoRecipeWrapper;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2629")
@Mixin(value = MultiblockInfoRecipeWrapper.class, remap = false)
public abstract class MultiblockInfoRecipeWrapperMixin {

    // This is a hard rewrite, any conflict should result in a hard crash
    @Redirect(method = "setNextLayer",
              at = @At(value = "INVOKE",
                       target = "Ljava/util/Map;clear()V"))
    private void intoNewImpl(Map<Collection<BlockPos>, ISceneRenderHook> _null,
                             @Local(name = "renderer") WorldSceneRenderer renderer) {
        WSRExtension.cast(renderer).sus$getRenderedBlocks().clear();
    }

    // This is a hard rewrite, any conflict should result in a hard crash
    @ModifyArg(method = "initializePattern",
               at = @At(value = "INVOKE",
                        target = "Lgregtech/client/utils/TrackedDummyWorld;setRenderFilter(Ljava/util/function/Predicate;)V"))
    private Predicate<BlockPos> replaceRenderFilter(Predicate<BlockPos> ignored,
                                                    @Local(name = "worldSceneRenderer") ImmediateWorldSceneRenderer wsr) {
        return WSRExtension.cast(wsr).sus$getRenderedBlocks()::contains;
    }

    // This is a hard rewrite, any conflict should result in a hard crash
    @Redirect(method = "initializePattern",
              at = @At(value = "NEW",
                       target = "gregtech/client/renderer/scene/ImmediateWorldSceneRenderer"))
    private ImmediateWorldSceneRenderer useVBORenderer(World world) {
        return new VBOWorldSceneRenderer(world);
    }
}
