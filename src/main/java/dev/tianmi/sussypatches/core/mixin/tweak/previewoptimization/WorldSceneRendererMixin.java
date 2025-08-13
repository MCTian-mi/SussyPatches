package dev.tianmi.sussypatches.core.mixin.tweak.previewoptimization;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;

import net.minecraft.util.math.BlockPos;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import dev.tianmi.sussypatches.api.core.mixin.extension.WSRExtension;
import gregtech.client.renderer.scene.ISceneRenderHook;
import gregtech.client.renderer.scene.WorldSceneRenderer;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2629")
@Mixin(value = WorldSceneRenderer.class, remap = false)
public abstract class WorldSceneRendererMixin implements WSRExtension {

    @Unique
    public final Collection<BlockPos> sus$renderedBlocks = new ObjectOpenHashSet<>();

    @Unique
    @Override
    public Collection<BlockPos> sus$getRenderedBlocks() {
        return sus$renderedBlocks;
    }

    @WrapOperation(method = "<init>",
                   at = @At(value = "FIELD",
                            target = "Lgregtech/client/renderer/scene/WorldSceneRenderer;renderedBlocksMap:Ljava/util/Map;",
                            opcode = Opcodes.PUTFIELD))
    private void clearOriginalImpl(WorldSceneRenderer self, Map<Collection<BlockPos>, ISceneRenderHook> ignored,
                                   Operation<Void> insn) {
        insn.call(self, null);
    }

    // This is a hard rewrite, any conflict should result in a hard crash
    @SuppressWarnings("unchecked")
    @Redirect(method = "addRenderedBlocks",
              at = @At(value = "INVOKE",
                       target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    private <Collect, RenderHook> RenderHook intoNewImpl(Map<Collect, RenderHook> _null, Collect blocks,
                                                         RenderHook ignored) {
        sus$renderedBlocks.addAll((Collection<BlockPos>) blocks);
        return null; // Return value ignored
    }

    // This is a hard rewrite, any conflict should result in a hard crash
    @Redirect(method = "drawWorld",
              at = @At(value = "INVOKE",
                       target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V"),
              require = 2)
    private void unwrapIterator(Map<Collection<BlockPos>, ISceneRenderHook> _null,
                                BiConsumer<Collection<BlockPos>, ISceneRenderHook> lambda) {
        lambda.accept(sus$renderedBlocks, null);
    }
}
