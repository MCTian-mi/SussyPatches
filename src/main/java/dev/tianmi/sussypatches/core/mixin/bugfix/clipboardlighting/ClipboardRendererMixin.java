package dev.tianmi.sussypatches.core.mixin.bugfix.clipboardlighting;

import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import dev.tianmi.sussypatches.api.annotation.Implemented;
import gregtech.client.renderer.texture.custom.ClipboardRenderer;
import gregtech.common.metatileentities.MetaTileEntityClipboard;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2845")
@Mixin(value = ClipboardRenderer.class, remap = false)
public abstract class ClipboardRendererMixin {

    @Inject(method = "renderBoard",
            at = @At(value = "INVOKE",
                     target = "Lcodechicken/lib/vec/Matrix4;rotate(DLcodechicken/lib/vec/Vector3;)Lcodechicken/lib/vec/Matrix4;"))
    private static void setBrightness(CCRenderState renderState,
                                      Matrix4 translation,
                                      IVertexOperation[] pipeline,
                                      EnumFacing rotation,
                                      MetaTileEntityClipboard clipboard,
                                      float partialTicks,
                                      CallbackInfo ci) {
        World world = clipboard.getWorld();
        if (world != null) {
            renderState.setBrightness(clipboard.getWorld(), clipboard.getPos());
        }
    }

    @WrapOperation(method = "renderGUI",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/OpenGlHelper;setLightmapTextureCoords(IFF)V",
                            remap = true,
                            ordinal = 0))
    private static void setBrightness(int target, float x, float y, Operation<Void> method,
                                      @Local(argsOnly = true) MetaTileEntityClipboard clipboard) {
        World world = clipboard.getWorld();
        if (world != null) {
            int light = clipboard.getWorld().getCombinedLight(clipboard.getPos(), 0);
            x = (float) light % 0x10000;
            y = (float) light / 0x10000;
        }
        method.call(target, x, y);
    }
}
